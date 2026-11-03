package admin

import (
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net/http"
	"strconv"
	"time"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"ths-service/internal/alipay"
	"ths-service/internal/auth"
	"ths-service/internal/electricity"
	"ths-service/internal/pdfreport"
	"ths-service/internal/response"
	"ths-service/internal/tax"
)

type PDFPrepareRequest struct {
	Kind       string `json:"kind"`
	UserID     uint64 `json:"userId"`
	BillID     uint64 `json:"billId"`
	StartMonth string `json:"startMonth"`
	EndMonth   string `json:"endMonth"`
}
type PDFBillChoice struct {
	ID        uint64 `json:"id"`
	Code      string `json:"code"`
	StartDate string `json:"startDate"`
	EndDate   string `json:"endDate"`
	Amount    string `json:"amount"`
}
type PDFSourceResponse struct {
	Code    int             `json:"code"`
	Message string          `json:"message"`
	Data    []PDFBillChoice `json:"data"`
}
type PDFDraftResponse struct {
	Code    int             `json:"code"`
	Message string          `json:"message"`
	Data    pdfreport.Draft `json:"data"`
}
type PDFSchemasResponse struct {
	Code    int                `json:"code"`
	Message string             `json:"message"`
	Data    []pdfreport.Schema `json:"data"`
}
type PDFExportAudit struct {
	ID            uint64
	AdminID       uint64
	UserID        uint64
	Kind          string
	PDFSha256     string `gorm:"column:pdf_sha256"`
	AssetVersions string
	RowCount      int
	CreatedAt     time.Time
}

func (PDFExportAudit) TableName() string { return "pdf_export_audits" }

var reportSlots = map[string][]int{"work": {5}, "electricity": {3, 4, 7}, "social": {6}}
var reportCapacity = make(chan struct{}, 2)
var errPDFRange = errors.New("所选范围记录过多，请缩小月份范围")

func RegisterPDFReports(r *gin.Engine, h *Handler) {
	api := r.Group("/api/admin/pdf-reports", h.RequireLogin, sameOrigin, func(c *gin.Context) { c.Header("Cache-Control", "no-store") })
	api.GET("/schemas", h.PDFReportSchemas)
	api.GET("/bills", h.PDFReportBills)
	api.POST("/prepare", h.PDFReportPrepare)
	api.POST("/download", h.PDFReportDownload)
}
func (h *Handler) reportFail(c *gin.Context, e error) {
	if errors.Is(e, errPDFRange) {
		response.Error(c, 400, response.CodeInvalidRequest, e.Error())
		return
	}
	if errors.Is(e, gorm.ErrRecordNotFound) {
		response.Error(c, 404, response.CodeNotFound, "会员或所选数据不存在，可能已删除")
		return
	}
	h.fail(c, e)
}

// PDFReportSchemas 报表编辑字段
// @Summary 查询PDF报表类型、基本字段及可编辑明细表
// @Description 管理员Cookie鉴权。work工作信息、electricity用电账单、social社保记录。输出为明确标识的演示报表，不是单位或机构出具的证明。
// @Tags 后台 · PDF生成
// @Produce json
// @Success 200 {object} PDFSchemasResponse
// @Router /api/admin/pdf-reports/schemas [get]
func (h *Handler) PDFReportSchemas(c *gin.Context) { response.Success(c, pdfreport.Schemas) }

// PDFReportBills 选择已有账单
// @Summary 查询指定会员最近100条电费账单供PDF选择
// @Description 只读数据库，缺失不生成，不修改原业务记录。会员只能由管理员选择。
// @Tags 后台 · PDF生成
// @Produce json
// @Param userId query int true "会员ID"
// @Success 200 {object} PDFSourceResponse
// @Router /api/admin/pdf-reports/bills [get]
func (h *Handler) PDFReportBills(c *gin.Context) {
	uid, e := strconv.ParseUint(c.Query("userId"), 10, 64)
	if e != nil || uid == 0 {
		response.Error(c, 400, response.CodeInvalidRequest, "请选择会员")
		return
	}
	var user auth.User
	if e = h.db.WithContext(c.Request.Context()).First(&user, uid).Error; e != nil {
		h.reportFail(c, e)
		return
	}
	rows := []PDFBillChoice{}
	e = h.db.WithContext(c.Request.Context()).Model(&electricity.Bill{}).Select("id,code,start_date,end_date,amount").Where("user_id=?", uid).Order("start_date DESC,id DESC").Limit(100).Scan(&rows).Error
	if e != nil {
		h.reportFail(c, e)
		return
	}
	response.Success(c, rows)
}
func bindReport(c *gin.Context, out any) bool {
	c.Request.Body = http.MaxBytesReader(c.Writer, c.Request.Body, 512<<10)
	d := json.NewDecoder(c.Request.Body)
	d.DisallowUnknownFields()
	if c.ContentType() != "application/json" || d.Decode(out) != nil || d.Decode(new(any)) != io.EOF {
		response.Error(c, 400, response.CodeInvalidRequest, "请提交有效的报表JSON，最多512KiB")
		return false
	}
	return true
}

// PDFReportPrepare 读取数据
// @Summary 读取会员已有数据作为本次PDF编辑草稿
// @Description 不生成缺失业务数据，不回写。电费须选billId且属于会员，社保须填YYYY-MM起止月份（最多120个月），缺失分险种数据留空并提示，不从合计推算。draft仅供浏览器编辑。缺失工作资料可在表单补充。
// @Tags 后台 · PDF生成
// @Accept json
// @Produce json
// @Param body body PDFPrepareRequest true "类型、会员及数据范围"
// @Success 200 {object} PDFDraftResponse
// @Router /api/admin/pdf-reports/prepare [post]
func (h *Handler) PDFReportPrepare(c *gin.Context) {
	var req PDFPrepareRequest
	if !bindReport(c, &req) {
		return
	}
	schema, e := pdfreport.Definition(req.Kind)
	if e != nil || req.UserID == 0 {
		response.Error(c, 400, response.CodeInvalidRequest, "请选择类型和会员")
		return
	}
	if req.Kind == "electricity" && req.BillID == 0 {
		response.Error(c, 400, response.CodeInvalidRequest, "请选择电费账单")
		return
	}
	if req.Kind == "social" {
		a, e1 := time.Parse("2006-01", req.StartMonth)
		b, e2 := time.Parse("2006-01", req.EndMonth)
		months := (b.Year()-a.Year())*12 + int(b.Month()-a.Month()) + 1
		if e1 != nil || e2 != nil || a.Year() < 2000 || a.After(b) || months > 120 || req.EndMonth > time.Now().In(alipay.Zone).Format("2006-01") {
			response.Error(c, 400, response.CodeInvalidRequest, "社保范围须为2000年至本月，最多120个月")
			return
		}
	}
	d := pdfreport.Draft{Kind: req.Kind, UserID: req.UserID, Fields: map[string]string{}, Tables: map[string][]map[string]string{}, Warnings: []string{}}
	for _, f := range schema.Fields {
		d.Fields[f.Key] = ""
	}
	for _, t := range schema.Tables {
		d.Tables[t.Key] = []map[string]string{}
	}
	e = h.db.WithContext(c.Request.Context()).Transaction(func(tx *gorm.DB) error {
		var user auth.User
		if e := tx.First(&user, req.UserID).Error; e != nil {
			return e
		}
		d.Fields["name"] = user.RealName
		d.Fields["issuedDate"] = time.Now().In(alipay.Zone).Format("2006-01-02")
		optional := func(out any, where string, args ...any) error {
			e := tx.Where(where, args...).First(out).Error
			if errors.Is(e, gorm.ErrRecordNotFound) {
				return nil
			}
			return e
		}
		switch req.Kind {
		case "work":
			var profile tax.Profile
			if e := optional(&profile, "user_id=?", user.ID); e != nil {
				return e
			}
			var emp tax.Employment
			if e := optional(&emp, "user_id=?", user.ID); e != nil {
				return e
			}
			if profile.Name != "" {
				d.Fields["name"] = profile.Name
			}
			d.Fields["sex"] = profile.Sex
			d.Fields["idNumber"] = profile.IDCard
			d.Fields["companyName"] = emp.CompanyName
			d.Fields["post"] = emp.Post
			d.Fields["startDate"] = emp.StartDate
			d.Fields["endDate"] = emp.EndDate
			d.Warnings = append(d.Warnings, "部门、单位地址和月均收入未保存在任职资料中，请按本次报表需要填写。")
		case "electricity":
			var bill electricity.Bill
			if e := tx.Where("user_id=?", user.ID).First(&bill, req.BillID).Error; e != nil {
				return e
			}
			var p electricity.Profile
			if e := optional(&p, "user_id=?", user.ID); e != nil {
				return e
			}
			if p.DisplayName != "" {
				d.Fields["name"] = p.DisplayName
			}
			d.Fields["accountNo"] = p.UserIDNumber
			d.Fields["address"] = p.Address
			d.Fields["supplier"] = p.Supplier
			d.Fields["meterNo"] = p.MeterNo
			d.Fields["startDate"] = bill.StartDate
			d.Fields["endDate"] = bill.EndDate
			d.Fields["totalUsage"] = bill.TotalUsage
			var detail map[string]any
			json.Unmarshal([]byte(bill.Detail), &detail)
			price, _ := detail["unitPrice"].(string)
			row := map[string]string{"label": "本期电费", "quantity": bill.TotalUsage, "price": price, "amount": bill.Amount}
			if price != "" && pdfreport.Money(pdfreport.Fee(row)) != bill.Amount {
				row["price"] = ""
			}
			d.Tables["fees"] = append(d.Tables["fees"], row)
			billing := electricity.BillingForBill(bill)
			d.Fields["usageType"] = billing.UsageType
			d.Fields["dueDate"] = billing.DueDate
			d.Fields["lateFeeStartDate"] = billing.LateFeeStartDate
			d.Fields["lateFeeAsOfDate"] = billing.LateFeeAsOfDate
			d.Fields["lateFee"] = billing.LateFee
			d.Fields["arrearsPrincipal"] = billing.ArrearsPrincipal
			d.Fields["electricityFee"] = billing.ElectricityFee
			d.Fields["payableAmount"] = billing.PayableAmount
			d.Tables["fees"][0]["calculation"] = "manual"
			if len(billing.Tiers) > 0 {
				d.Tables["fees"] = []map[string]string{}
				for _, tier := range billing.Tiers {
					d.Tables["fees"] = append(d.Tables["fees"], map[string]string{"label": fmt.Sprintf("第%d档", tier.Level), "quantity": tier.Usage, "price": tier.UnitPrice, "amount": tier.Amount, "calculation": "manual"})
				}
			}
			d.Warnings = append(d.Warnings, "未保存的缴费日期留空；图表是固定示意图片。")
		case "social":
			var profile tax.Profile
			if e := optional(&profile, "user_id=?", user.ID); e != nil {
				return e
			}
			d.Fields["idNumber"] = profile.IDCard
			var meta alipay.Record
			if e := optional(&meta, "kind='insurance' AND user_id=?", user.ID); e != nil {
				return e
			}
			var p map[string]string
			json.Unmarshal(meta.Payload, &p)
			d.Fields["manageCode"] = p["manageCode"]
			d.Fields["city"] = p["cityName"]
			var rows []alipay.Record
			if e := tx.Where("kind='records' AND user_id=? AND JSON_UNQUOTE(JSON_EXTRACT(payload,'$.month')) >= ? AND JSON_UNQUOTE(JSON_EXTRACT(payload,'$.month')) <= ?", user.ID, req.StartMonth[:4]+req.StartMonth[5:], req.EndMonth[:4]+req.EndMonth[5:]).Order("JSON_UNQUOTE(JSON_EXTRACT(payload,'$.month')) ASC,id ASC").Limit(601).Find(&rows).Error; e != nil {
				return e
			}
			if len(rows) == 0 {
				return gorm.ErrRecordNotFound
			}
			if len(rows) > 599 {
				return errPDFRange
			}
			months := map[string]bool{}
			for _, r := range rows {
				var p map[string]string
				if e := json.Unmarshal(r.Payload, &p); e != nil {
					return e
				}
				m := p["month"]
				if len(m) != 6 {
					return fmt.Errorf("已存缴费月份无效")
				}
				months[m] = true
				d.Tables["payments"] = append(d.Tables["payments"], map[string]string{"month": m[:4] + "-" + m[4:], "companyNo": "", "company": p["company"], "base": p["base"], "companyPay": p["companyPay"], "personalPay": p["personalPay"]})
			}
			d.Tables["coverage"] = append(d.Tables["coverage"], map[string]string{"kind": "职工社保（合计）", "startMonth": d.Tables["payments"][0]["month"], "months": strconv.Itoa(len(months)), "status": p["status"]})
			d.Warnings = append(d.Warnings, "参保开始/月数默认按所选范围统计，不代表完整参保历史；单位编号及分险种数据未保存，可补充但不会从合计推算。")
		}
		return nil
	})
	if e != nil {
		h.reportFail(c, e)
		return
	}
	response.Success(c, d)
}

// PDFReportDownload 生成下载
// @Summary 将编辑后的本次草稿生成PDF并直接下载
// @Description 管理员Cookie及同源鉴权。不回写会员、账单、社保等业务数据，只记录导出元数据审计，不存PDF文件、不提供链接。最多600行明细，费用按计费标准重算；填写分险种明细时月合计必须一致。始终保留演示标识。图片仅使用固定位置当前approved生效版本，调用方不能指定素材ID。2个并发名额，繁忙返回429。
// @Tags 后台 · PDF生成
// @Accept json
// @Produce application/pdf
// @Param body body pdfreport.Draft true "本次导出草稿"
// @Success 200 {file} file
// @Failure 400 {object} response.ErrorEnvelope
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} response.ErrorEnvelope
// @Router /api/admin/pdf-reports/download [post]
func (h *Handler) PDFReportDownload(c *gin.Context) {
	var d pdfreport.Draft
	if !bindReport(c, &d) {
		return
	}
	if e := pdfreport.Validate(&d); e != nil {
		response.Error(c, 400, response.CodeInvalidRequest, e.Error())
		return
	}
	select {
	case reportCapacity <- struct{}{}:
		defer func() { <-reportCapacity }()
	default:
		c.Header("Retry-After", "2")
		response.Error(c, 429, response.CodeTooManyRequests, "正在生成其他报表，请稍后重试")
		return
	}
	assets := []pdfreport.Asset{}
	versions := map[int]uint64{}
	e := h.db.WithContext(c.Request.Context()).Transaction(func(tx *gorm.DB) error {
		var user auth.User
		if e := tx.First(&user, d.UserID).Error; e != nil {
			return e
		}
		for _, slot := range reportSlots[d.Kind] {
			var s PDFAssetSlot
			if e := tx.First(&s, "slot=?", slot).Error; e != nil {
				return e
			}
			if s.ActiveID == nil {
				continue
			}
			var a PDFAsset
			if e := tx.Where("slot=? AND status='approved'", slot).First(&a, *s.ActiveID).Error; e != nil {
				return e
			}
			assets = append(assets, pdfreport.Asset{Slot: slot, ID: a.ID, Data: a.ImageData})
			versions[slot] = a.ID
		}
		return nil
	})
	if e != nil {
		h.reportFail(c, e)
		return
	}
	b, e := pdfreport.Render(d, assets)
	if e != nil {
		h.fail(c, e)
		return
	}
	if c.Request.Context().Err() != nil {
		return
	}
	hash := sha256.Sum256(b)
	a, _ := c.Get("administrator")
	v, _ := json.Marshal(versions)
	rows := 0
	for _, t := range d.Tables {
		rows += len(t)
	}
	audit := PDFExportAudit{AdminID: a.(Administrator).ID, UserID: d.UserID, Kind: d.Kind, PDFSha256: hex.EncodeToString(hash[:]), AssetVersions: string(v), RowCount: rows}
	if e = h.db.WithContext(c.Request.Context()).Create(&audit).Error; e != nil {
		h.fail(c, e)
		return
	}
	c.Header("Content-Disposition", fmt.Sprintf("attachment; filename=\"%s-%d.pdf\"", d.Kind, d.UserID))
	c.Header("X-Content-Type-Options", "nosniff")
	c.Data(200, "application/pdf", b)
}

package admin

import (
	"bytes"
	"crypto/sha256"
	"encoding/hex"
	"errors"
	"image"
	_ "image/jpeg"
	"image/png"
	"io"
	"net/http"
	"path"
	"strconv"
	"strings"
	"time"
	"unicode/utf8"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"gorm.io/gorm/clause"
	"ths-service/internal/response"
)

const assetColumns = "id,slot,original_name,width,height,sha256,status,uploaded_by,reviewed_by,review_note,reviewed_at,created_at"

type PDFAssetSlot struct {
	Slot      int       `gorm:"primaryKey" json:"slot"`
	ActiveID  *uint64   `json:"activeId"`
	UpdatedAt time.Time `json:"updatedAt"`
}

func (PDFAssetSlot) TableName() string { return "pdf_asset_slots" }

type PDFAsset struct {
	ID           uint64     `json:"id"`
	Slot         int        `json:"slot"`
	OriginalName string     `json:"originalName"`
	ImageData    []byte     `json:"-"`
	Width        int        `json:"width"`
	Height       int        `json:"height"`
	SHA256       string     `json:"sha256"`
	Status       string     `json:"status"`
	UploadedBy   uint64     `json:"uploadedBy"`
	ReviewedBy   *uint64    `json:"reviewedBy"`
	ReviewNote   string     `json:"reviewNote"`
	ReviewedAt   *time.Time `json:"reviewedAt"`
	CreatedAt    time.Time  `json:"createdAt"`
}

func (PDFAsset) TableName() string { return "pdf_asset_versions" }

type PDFAssetAudit struct {
	ID               uint64
	Slot             int
	AssetID          uint64
	AdminID          uint64
	Action           string
	PreviousActiveID *uint64
	Note             string
	CreatedAt        time.Time
}

func (PDFAssetAudit) TableName() string { return "pdf_asset_audits" }

type PDFAssetSlotView struct {
	PDFAssetSlot
	Active   *PDFAsset  `json:"active"`
	Versions []PDFAsset `json:"versions"`
}
type PDFAssetList struct {
	CanReview bool               `json:"canReview"`
	Slots     []PDFAssetSlotView `json:"slots"`
}
type PDFAssetListResponse struct {
	Code    int          `json:"code"`
	Message string       `json:"message"`
	Data    PDFAssetList `json:"data"`
}
type PDFAssetResponse struct {
	Code    int      `json:"code"`
	Message string   `json:"message"`
	Data    PDFAsset `json:"data"`
}
type PDFAssetReview struct {
	Decision string `json:"decision"` // approve or reject
	Note     string `json:"note"`     // Required for rejection, at most 500 characters.
}

func assetReviewer(c *gin.Context) bool {
	a, _ := c.Get("administrator")
	u, ok := a.(Administrator)
	return ok && u.Username == "eugene" && u.IsSuperAdmin
}
func RegisterPDFAssets(r *gin.Engine, h *Handler) {
	api := r.Group("/api/admin/pdf-assets", h.RequireLogin, sameOrigin, func(c *gin.Context) { c.Header("Cache-Control", "no-store") })
	api.GET("", h.PDFAssetList)
	api.POST("/slots/:slot/upload", h.PDFAssetUpload)
	api.GET("/versions/:assetID/image", h.PDFAssetImage)
	api.POST("/versions/:assetID/review", h.PDFAssetReview)
}
func (h *Handler) assetFail(c *gin.Context, e error) {
	if errors.Is(e, gorm.ErrRecordNotFound) {
		response.Error(c, 404, response.CodeNotFound, "素材或位置不存在")
		return
	}
	if errors.Is(e, errAssetReviewed) {
		response.Error(c, 409, response.CodeAccountExists, "该版本已审核，请刷新列表")
		return
	}
	h.fail(c, e)
}

var errAssetReviewed = errors.New("asset already reviewed")

// PDFAssetList PDF素材
// @Summary 查询3至7号素材位置、生效图片及最近20个版本
// @Description 管理员Cookie鉴权。无图片的位置为空，不自动导入项目png目录。canReview仅账号eugene且为超级管理员时为true；其他管理员可上传和预览，不可审核。历史已通过版本不一定是当前生效图片，以activeId为准。
// @Tags 后台 · PDF素材
// @Produce json
// @Success 200 {object} PDFAssetListResponse
// @Router /api/admin/pdf-assets [get]
func (h *Handler) PDFAssetList(c *gin.Context) {
	out := PDFAssetList{CanReview: assetReviewer(c), Slots: []PDFAssetSlotView{}}
	e := h.db.WithContext(c.Request.Context()).Transaction(func(tx *gorm.DB) error {
		var slots []PDFAssetSlot
		if e := tx.Order("slot").Find(&slots).Error; e != nil {
			return e
		}
		for _, s := range slots {
			v := PDFAssetSlotView{PDFAssetSlot: s, Versions: []PDFAsset{}}
			if s.ActiveID != nil {
				var a PDFAsset
				if e := tx.Select(assetColumns).Where("status='approved'").First(&a, *s.ActiveID).Error; e != nil {
					return e
				}
				v.Active = &a
			}
			if e := tx.Select(assetColumns).Where("slot=?", s.Slot).Order("id DESC").Limit(20).Find(&v.Versions).Error; e != nil {
				return e
			}
			out.Slots = append(out.Slots, v)
		}
		return nil
	})
	if e != nil {
		h.assetFail(c, e)
		return
	}
	response.Success(c, out)
}

// canonicalAsset validates decoded pixels and strips metadata by re-encoding PNG.
func canonicalAsset(b []byte) ([]byte, int, int, error) {
	config, format, e := image.DecodeConfig(bytes.NewReader(b))
	if e != nil || (format != "png" && format != "jpeg") || config.Width < 1 || config.Height < 1 || config.Width > 8192 || config.Height > 8192 || int64(config.Width)*int64(config.Height) > 8000000 {
		return nil, 0, 0, errors.New("仅支持有效PNG/JPEG，边长最多8192像素、总像素最多800万")
	}
	img, _, e := image.Decode(bytes.NewReader(b))
	if e != nil {
		return nil, 0, 0, errors.New("图片损坏，无法解码")
	}
	var out bytes.Buffer
	if e = png.Encode(&out, img); e != nil {
		return nil, 0, 0, e
	}
	if out.Len() > 10<<20 {
		return nil, 0, 0, errors.New("图片解码后过大，请缩小尺寸再上传")
	}
	return out.Bytes(), config.Width, config.Height, nil
}

// PDFAssetUpload 上传素材
// @Summary 上传固定位置的PNG或JPEG待审核素材
// @Description 管理员Cookie及同源校验。slot为3至7；单文件最大5MiB，边长最多8192像素且总像素不超过800万。实际解码并转存PNG、保留透明度、移除元数据；不接受SVG/GIF或其他文件。上传仅创建待审核版本，不覆盖生效图片。图片内容需人工审核，不自动保证其真实性或授权。
// @Tags 后台 · PDF素材
// @Accept multipart/form-data
// @Produce json
// @Param slot path int true "素材位置3至7"
// @Param file formData file true "PNG/JPEG，最多5MiB"
// @Success 200 {object} PDFAssetResponse
// @Router /api/admin/pdf-assets/slots/{slot}/upload [post]
func (h *Handler) PDFAssetUpload(c *gin.Context) {
	slot, e := strconv.Atoi(c.Param("slot"))
	if e != nil || slot < 3 || slot > 7 {
		response.Error(c, 400, response.CodeInvalidRequest, "素材位置仅限3至7")
		return
	}
	c.Request.Body = http.MaxBytesReader(c.Writer, c.Request.Body, 6<<20)
	if e = c.Request.ParseMultipartForm(6 << 20); e != nil {
		response.Error(c, 400, response.CodeInvalidRequest, "请上传5MiB以内的单张图片")
		return
	}
	defer c.Request.MultipartForm.RemoveAll()
	if len(c.Request.MultipartForm.File) != 1 || len(c.Request.MultipartForm.File["file"]) != 1 || len(c.Request.MultipartForm.Value) != 0 {
		response.Error(c, 400, response.CodeInvalidRequest, "只允许一个file图片字段")
		return
	}
	file, header, e := c.Request.FormFile("file")
	if e != nil {
		response.Error(c, 400, response.CodeInvalidRequest, "缺少图片")
		return
	}
	defer file.Close()
	b, e := io.ReadAll(io.LimitReader(file, (5<<20)+1))
	if e != nil || len(b) > 5<<20 {
		response.Error(c, 400, response.CodeInvalidRequest, "图片不可超过5MiB")
		return
	}
	data, width, height, e := canonicalAsset(b)
	if e != nil {
		response.Error(c, 400, response.CodeInvalidRequest, e.Error())
		return
	}
	name := path.Base(strings.ReplaceAll(header.Filename, "\\", "/"))
	if !utf8.ValidString(name) || utf8.RuneCountInString(name) > 120 {
		name = "image.png"
	}
	hash := sha256.Sum256(data)
	actor, _ := c.Get("administrator")
	asset := PDFAsset{Slot: slot, OriginalName: name, ImageData: data, Width: width, Height: height, SHA256: hex.EncodeToString(hash[:]), Status: "pending", UploadedBy: actor.(Administrator).ID}
	e = h.db.WithContext(c.Request.Context()).Transaction(func(tx *gorm.DB) error {
		var s PDFAssetSlot
		if e := tx.Clauses(clause.Locking{Strength: "UPDATE"}).First(&s, "slot=?", slot).Error; e != nil {
			return e
		}
		if e := tx.Create(&asset).Error; e != nil {
			return e
		}
		return tx.Create(&PDFAssetAudit{Slot: slot, AssetID: asset.ID, AdminID: asset.UploadedBy, Action: "upload", PreviousActiveID: s.ActiveID}).Error
	})
	if e != nil {
		h.assetFail(c, e)
		return
	}
	response.Success(c, asset)
}
func assetID(c *gin.Context) (uint64, bool) {
	id, e := strconv.ParseUint(c.Param("assetID"), 10, 64)
	if e != nil || id == 0 {
		response.Error(c, 400, response.CodeInvalidRequest, "素材编号无效")
		return 0, false
	}
	return id, true
}

// PDFAssetImage 预览
// @Summary 管理员预览指定版本的素材图片
// @Description 管理员Cookie鉴权，包含待审核及历史版本，仅用于后台预览，不代表可用于PDF导出。固定返回PNG并禁止缓存。
// @Tags 后台 · PDF素材
// @Produce image/png
// @Param assetID path int true "素材版本ID"
// @Success 200 {file} file
// @Router /api/admin/pdf-assets/versions/{assetID}/image [get]
func (h *Handler) PDFAssetImage(c *gin.Context) {
	id, ok := assetID(c)
	if !ok {
		return
	}
	var a PDFAsset
	if e := h.db.WithContext(c.Request.Context()).Select("image_data").First(&a, id).Error; e != nil {
		h.assetFail(c, e)
		return
	}
	c.Header("X-Content-Type-Options", "nosniff")
	c.Header("Content-Security-Policy", "default-src 'none'; sandbox")
	c.Data(200, "image/png", a.ImageData)
}

// PDFAssetReview 审核素材
// @Summary eugene审核待审核图片，通过后替换对应位置的生效图片
// @Description 后端限定账号eugene且为超级管理员，其他管理员返回403。仅待审核版本可审核；approve通过、reject驳回，驳回必填理由。审核与生效指针替换、审计同事务，原生效版本保留。未通过的图片不得用于PDF生成。
// @Tags 后台 · PDF素材
// @Accept json
// @Produce json
// @Param assetID path int true "待审核素材ID"
// @Param body body PDFAssetReview true "审核决定及备注，最多500字符"
// @Success 200 {object} PDFAssetResponse
// @Failure 403 {object} response.ErrorEnvelope
// @Failure 409 {object} response.ErrorEnvelope
// @Router /api/admin/pdf-assets/versions/{assetID}/review [post]
func (h *Handler) PDFAssetReview(c *gin.Context) {
	if !assetReviewer(c) {
		response.Error(c, 403, response.CodeUnauthorized, "只有eugene可以审核素材")
		return
	}
	id, ok := assetID(c)
	if !ok {
		return
	}
	var req PDFAssetReview
	if !memberBind(c, &req) {
		return
	}
	req.Note = strings.TrimSpace(req.Note)
	if (req.Decision != "approve" && req.Decision != "reject") || utf8.RuneCountInString(req.Note) > 500 || (req.Decision == "reject" && req.Note == "") {
		response.Error(c, 400, response.CodeInvalidRequest, "审核决定无效，驳回须说明理由（最多500字）")
		return
	}
	actor, _ := c.Get("administrator")
	adminID := actor.(Administrator).ID
	var a PDFAsset
	e := h.db.WithContext(c.Request.Context()).Transaction(func(tx *gorm.DB) error {
		if e := tx.Select(assetColumns).First(&a, id).Error; e != nil {
			return e
		}
		var s PDFAssetSlot
		if e := tx.Clauses(clause.Locking{Strength: "UPDATE"}).First(&s, "slot=?", a.Slot).Error; e != nil {
			return e
		}
		if e := tx.Select(assetColumns).Clauses(clause.Locking{Strength: "UPDATE"}).First(&a, id).Error; e != nil {
			return e
		}
		if a.Status != "pending" {
			return errAssetReviewed
		}
		status := "rejected"
		if req.Decision == "approve" {
			status = "approved"
		}
		var previous *uint64
		if s.ActiveID != nil {
			v := *s.ActiveID
			previous = &v
		}
		now := time.Now()
		if e := tx.Model(&a).Updates(map[string]any{"status": status, "reviewed_by": adminID, "review_note": req.Note, "reviewed_at": now}).Error; e != nil {
			return e
		}
		if status == "approved" {
			if e := tx.Model(&s).Updates(map[string]any{"active_id": id, "updated_at": now}).Error; e != nil {
				return e
			}
		}
		if e := tx.Create(&PDFAssetAudit{Slot: a.Slot, AssetID: id, AdminID: adminID, Action: req.Decision, PreviousActiveID: previous, Note: req.Note}).Error; e != nil {
			return e
		}
		return tx.Select(assetColumns).First(&a, id).Error
	})
	if e != nil {
		h.assetFail(c, e)
		return
	}
	response.Success(c, a)
}

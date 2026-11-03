package nanwang

import (
	"encoding/json"
	"errors"
	"io"
	"log/slog"
	"net/http"
	"sort"
	"strconv"
	"strings"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"ths-service/internal/auth"
	_ "ths-service/internal/response"
)

type Request struct {
	AreaCode  string `json:"areaCode"`
	Count     string `json:"count"`
	ClassID   string `json:"classId"`
	PageNo    string `json:"pageNo"`
	PageSize  string `json:"pageSize"`
	ID        string `json:"id"`
	Longitude string `json:"longitude"`
	Latitude  string `json:"latitude"`
	EleCustID string `json:"eleCustId"`
}
type AppResponse struct {
	Sta     string `json:"sta"`
	Message string `json:"message"`
	Data    any    `json:"data"`
}
type Handler struct{ service *Service }

func NewHandler(s *Service) *Handler { return &Handler{s} }
func appError(c *gin.Context, err error) {
	status := 500
	sta := "99"
	message := "服务器内部错误"
	if errors.Is(err, ErrValidation) {
		status = 400
		sta = "01"
		message = err.Error()
	} else if IsNotFound(err) {
		status = 404
		sta = "04"
		message = "记录不存在"
	}
	if status == 500 {
		slog.Error("nanwang business", "requestId", c.GetString("requestId"), "error", err)
	}
	c.JSON(status, AppResponse{sta, message, nil})
}
func number(s string, def, max int) (int, error) {
	if s == "" {
		return def, nil
	}
	n, e := strconv.Atoi(s)
	if e != nil || n < 1 || n > max {
		return 0, invalid("分页或数量参数无效")
	}
	return n, nil
}
func (h *Handler) run(c *gin.Context, kind, mode string) {
	c.Header("Cache-Control", "no-store")
	var req Request
	c.Request.Body = http.MaxBytesReader(c.Writer, c.Request.Body, 8192)
	dec := json.NewDecoder(c.Request.Body)
	dec.DisallowUnknownFields()
	if !strings.HasPrefix(c.GetHeader("Content-Type"), "application/json") || dec.Decode(&req) != nil || dec.Decode(new(any)) != io.EOF {
		appError(c, invalid("请提交JSON对象"))
		return
	}
	for _, v := range []string{req.AreaCode, req.ClassID, req.ID, req.EleCustID, req.Longitude, req.Latitude} {
		if len(v) > 64 {
			appError(c, invalid("参数过长"))
			return
		}
	}
	limit := 10
	page := 1
	var err error
	if mode == "hot" {
		limit = 3
	}
	if mode == "hot" || mode == "latest" {
		limit, err = number(req.Count, limit, 100)
	}
	if mode == "list" {
		page, err = number(req.PageNo, 1, 1000000)
		if err == nil {
			limit, err = number(req.PageSize, 10, 100)
		}
	}
	if err != nil {
		appError(c, err)
		return
	}
	if mode == "detail" {
		if id, e := strconv.ParseUint(req.ID, 10, 64); e != nil || id == 0 {
			appError(c, invalid("id须为有效资讯编号"))
			return
		}
	}
	if kind == "halls" {
		for _, v := range []struct {
			s   string
			max float64
		}{{req.Longitude, 180}, {req.Latitude, 90}} {
			n, e := strconv.ParseFloat(v.s, 64)
			if e != nil || !(n >= -v.max && n <= v.max) {
				appError(c, invalid("经纬度无效"))
				return
			}
		}
	}
	records, err := h.service.Ensure(c.Request.Context(), kind, c.GetUint64(auth.UserIDContextKey))
	if err != nil {
		appError(c, err)
		return
	}
	rows := []map[string]any{}
	for _, r := range records {
		p := Payload(r)
		if kind == "news" {
			if mode == "detail" {
				if p["id"] == req.ID {
					c.JSON(200, AppResponse{"00", "success", p})
					return
				}
				continue
			}
			area := req.AreaCode
			if area == "" {
				area = "050200"
			}
			if p["areaCode"] != area {
				continue
			}
			if mode == "list" && req.ClassID != "" && p["classId"] != req.ClassID {
				continue
			}
		}
		if kind == "points" && req.EleCustID != "" && p["eleCustId"] != req.EleCustID {
			continue
		}
		rows = append(rows, p)
	}
	if mode == "detail" {
		appError(c, gorm.ErrRecordNotFound)
		return
	}
	if kind == "news" {
		sort.SliceStable(rows, func(i, j int) bool { return rows[i]["publishTime"].(string) > rows[j]["publishTime"].(string) })
		start := (page - 1) * limit
		if start > len(rows) {
			start = len(rows)
		}
		end := start + limit
		if end > len(rows) {
			end = len(rows)
		}
		rows = rows[start:end]
	}
	if kind == "charging" {
		if len(rows) > 0 {
			c.JSON(200, AppResponse{"00", "success", rows[0]})
			return
		}
	}
	c.JSON(200, AppResponse{"00", "success", rows})
}

// HotNews 热点资讯
// @Summary 热点资讯
// @Description 公共资讯列表。提交areaCode和count字符串，默认地区050200、3条，最多100条。按发布时间倒序；公共资讯完全缺失才生成保存，其他筛选无结果返回空数组。
// @Tags App端 · 南网业务
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param body body HotRequest true "参数按文档传入，无参数时传{}"
// @Success 200 {object} NewsResponse
// @Failure 400 {object} AppResponse
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} AppResponse
// @Failure 500 {object} AppResponse
// @Router /mp/ucs/ma/zt/content/queryHotInformations [post]
func (h *Handler) HotNews(c *gin.Context) { h.run(c, "news", "hot") }

// InformationList 分类资讯列表
// @Summary 分类资讯列表
// @Description 公共分类资讯分页。areaCode默认050200，classId初始1/2/3，可后台维护；pageNo/pageSize均为字符串，默认1/10，每页最多100。未知分类或地区返回空数组，部分删除不补齐。
// @Tags App端 · 南网业务
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param body body ListRequest true "参数按文档传入，无参数时传{}"
// @Success 200 {object} NewsResponse
// @Failure 400 {object} AppResponse
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} AppResponse
// @Failure 500 {object} AppResponse
// @Router /mp/ucs/ma/zt/content/queryInformationInfos [post]
func (h *Handler) InformationList(c *gin.Context) { h.run(c, "news", "list") }

// LatestNews 最新资讯
// @Summary 最新资讯
// @Description 公共最新资讯。areaCode默认050200，count为字符串，默认10，最多100。与热点和分类列表共用同一批数据库资讯，按发布时间倒序。
// @Tags App端 · 南网业务
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param body body LatestRequest true "参数按文档传入，无参数时传{}"
// @Success 200 {object} NewsResponse
// @Failure 400 {object} AppResponse
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} AppResponse
// @Failure 500 {object} AppResponse
// @Router /mp/ucs/ma/zt/content/queryLatestInformations [post]
func (h *Handler) LatestNews(c *gin.Context) { h.run(c, "news", "latest") }

// InformationDetail 资讯详情
// @Summary 资讯详情
// @Description 请求id为资讯列表返回的字符串编号。公共资讯集合缺失时先初始化；已存在集合中没有该id返回404，不为未知id单独制造资讯。返回标题、纯文本正文等资讯字段。
// @Tags App端 · 南网业务
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param body body DetailRequest true "参数按文档传入，无参数时传{}"
// @Success 200 {object} NewsDetailResponse
// @Failure 400 {object} AppResponse
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} AppResponse
// @Failure 500 {object} AppResponse
// @Router /mp/ucs/ma/zt/content/queryInformationInfoDetail [post]
func (h *Handler) InformationDetail(c *gin.Context) { h.run(c, "news", "detail") }

// RecommendUsers 推荐用电户号
// @Summary 推荐用电户号
// @Description 提交空对象{}。查询本人推荐用电户号；完全缺失时参考现有用电资料生成保存，已有记录复用。返回户名、户号、地址及户号记录编号；不执行真实推荐或绑定。
// @Tags App端 · 南网业务
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param body body EmptyRequest true "参数按文档传入，无参数时传{}"
// @Success 200 {object} CustomerResponse
// @Failure 400 {object} AppResponse
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} AppResponse
// @Failure 500 {object} AppResponse
// @Router /mp/w2/wx/portal/eleCustNumber/queryRecommendUsers [post]
func (h *Handler) RecommendUsers(c *gin.Context) { h.run(c, "recommended", "") }

// BindUsers 已绑定用电户号
// @Summary 已绑定用电户号
// @Description 提交空对象{}。查询本人已绑定户号的模拟展示记录，缺失生成保存，已有读库。不执行绑定或解绑操作；后台可维护多条户号。
// @Tags App端 · 南网业务
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param body body EmptyRequest true "参数按文档传入，无参数时传{}"
// @Success 200 {object} CustomerResponse
// @Failure 400 {object} AppResponse
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} AppResponse
// @Failure 500 {object} AppResponse
// @Router /mp/w2/wx/portal/eleCustNumber/queryBindEleUsers [post]
func (h *Handler) BindUsers(c *gin.Context) { h.run(c, "bound", "") }

// NearbyHalls 附近营业厅（模拟距离）
// @Summary 附近营业厅（模拟距离）
// @Description longitude/latitude必填，均为字符串，经度-180至180、纬度-90至90。缺失公共营业厅数据时生成2条并保存。distance为后台维护的模拟公里数，不根据坐标实时计算。
// @Tags App端 · 南网业务
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param body body HallRequest true "参数按文档传入，无参数时传{}"
// @Success 200 {object} HallResponse
// @Failure 400 {object} AppResponse
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} AppResponse
// @Failure 500 {object} AppResponse
// @Router /mp/ucs/ma/businesshall/businessHall/queryBusinessHallNearby [post]
func (h *Handler) NearbyHalls(c *gin.Context) { h.run(c, "halls", "") }

// UnreadMessages 未读消息
// @Summary 未读消息
// @Description 提交空对象{}。本人消息集合完全缺失时生成2条并保存；已有返回数据库记录。不执行消息已读操作，后台可维护标题。
// @Tags App端 · 南网业务
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param body body EmptyRequest true "参数按文档传入，无参数时传{}"
// @Success 200 {object} MessagesResponse
// @Failure 400 {object} AppResponse
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} AppResponse
// @Failure 500 {object} AppResponse
// @Router /mp/ucs/ma/intramsg/txhMsg/queryUnreadTitleList [post]
func (h *Handler) UnreadMessages(c *gin.Context) { h.run(c, "messages", "") }

// Arrears 绑定户号欠费列表
// @Summary 绑定户号欠费列表
// @Description 提交空对象{}；兼容客户端channelcode:4请求头。本人欠费户号记录缺失时生成保存，户号初始参考用电资料，欠费为独立模拟金额，不从账单推算或执行缴费。
// @Tags App端 · 南网业务
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param body body EmptyRequest true "参数按文档传入，无参数时传{}"
// @Param channelcode header string false "客户端渠道标识，可传4"
// @Success 200 {object} CustomerResponse
// @Failure 400 {object} AppResponse
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} AppResponse
// @Failure 500 {object} AppResponse
// @Router /mp/w2/wx/elecmanageImp/electric-fee/queryBindArrearsList [post]
func (h *Handler) Arrears(c *gin.Context) { h.run(c, "arrears", "") }

// MeteringPoints 计量点列表
// @Summary 计量点列表
// @Description 提交{}或{eleCustId:户号字符串}。本人计量点缺失时参考用电资料生成保存，eleCustId只筛选本人已有记录，不匹配返回空数组，不读取其他会员。
// @Tags App端 · 南网业务
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param body body PointRequest true "参数按文档传入，无参数时传{}"
// @Success 200 {object} PointsResponse
// @Failure 400 {object} AppResponse
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} AppResponse
// @Failure 500 {object} AppResponse
// @Router /mp/w2/wx/elecmanageImp/electric-fee/queryMeteringPointList [post]
func (h *Handler) MeteringPoints(c *gin.Context) { h.run(c, "points", "") }

// ChargingPile 充电桩状态
// @Summary 充电桩状态
// @Description 提交空对象{}。每会员一条充电桩模拟状态，缺失生成hasPile=false，已有读取数据库。data为对象；后台可修改hasPile，不查询真实设备。
// @Tags App端 · 南网业务
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param body body EmptyRequest true "参数按文档传入，无参数时传{}"
// @Success 200 {object} ChargingResponse
// @Failure 400 {object} AppResponse
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} AppResponse
// @Failure 500 {object} AppResponse
// @Router /mp/w2/wx/elecmanageImp/electric-daily/checkChargingPile [post]
func (h *Handler) ChargingPile(c *gin.Context) { h.run(c, "charging", "") }

func Register(r *gin.RouterGroup, s *Service) {
	h := NewHandler(s)
	r.POST("/mp/ucs/ma/zt/content/queryHotInformations", h.HotNews)
	r.POST("/mp/ucs/ma/zt/content/queryInformationInfos", h.InformationList)
	r.POST("/mp/ucs/ma/zt/content/queryLatestInformations", h.LatestNews)
	r.POST("/mp/ucs/ma/zt/content/queryInformationInfoDetail", h.InformationDetail)
	r.POST("/mp/w2/wx/portal/eleCustNumber/queryRecommendUsers", h.RecommendUsers)
	r.POST("/mp/w2/wx/portal/eleCustNumber/queryBindEleUsers", h.BindUsers)
	r.POST("/mp/ucs/ma/businesshall/businessHall/queryBusinessHallNearby", h.NearbyHalls)
	r.POST("/mp/ucs/ma/intramsg/txhMsg/queryUnreadTitleList", h.UnreadMessages)
	r.POST("/mp/w2/wx/elecmanageImp/electric-fee/queryBindArrearsList", h.Arrears)
	r.POST("/mp/w2/wx/elecmanageImp/electric-fee/queryMeteringPointList", h.MeteringPoints)
	r.POST("/mp/w2/wx/elecmanageImp/electric-daily/checkChargingPile", h.ChargingPile)
}

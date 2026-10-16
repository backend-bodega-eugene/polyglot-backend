package portfolio

import (
	"errors"
	"log/slog"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
	"ths-service/internal/auth"
	"ths-service/internal/market"
	"ths-service/internal/response"
)

type Handler struct{ service *Service }

func NewHandler(service *Service) *Handler { return &Handler{service: service} }

// Positions 获取模拟持仓
// @Summary 获取或生成当前用户的模拟持仓
// @Description 整个账户共持有3-5只A股，每只固定15条模拟成交。首次不传totalAssets按200000元初始化，随后复用；传金额时保留选股名单及价格快照，按新金额重建数量与成交。金额不足以让每只股票至少持有一手时返回400。旧版数据首次访问时保留总资产并一次性升级到新规则。总资产=总市值+可用现金；可取现金=可用现金。列表只返回实际持有的3-5只股票。
// @Tags 模拟持仓
// @Produce json
// @Param totalAssets query string false "重建时的总资产（元），10000至1000000000，最多2位小数，如200000.00"
// @Security BearerAuth
// @Success 200 {object} PositionResponse
// @Failure 400 {object} response.ErrorEnvelope
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 500 {object} response.ErrorEnvelope
// 已停用路由：/api/app/portfolio/positions；请使用 /api/app/market/holdings。
func (h *Handler) Positions(c *gin.Context) {
	c.Header("Cache-Control", "no-store")
	var assets *string
	if values, exists := c.Request.URL.Query()["totalAssets"]; exists {
		if len(values) != 1 {
			response.Error(c, http.StatusBadRequest, response.CodeInvalidRequest, ErrInvalidAssets.Error())
			return
		}
		assets = &values[0]
	}
	data, err := h.service.Positions(c.Request.Context(), c.GetUint64(auth.UserIDContextKey), assets)
	if err != nil {
		h.fail(c, err)
		return
	}
	response.Success(c, data)
}

// Trades 获取模拟历史成交
// @Summary 获取当前用户的模拟历史成交
// @Description 返回当前这套模拟持仓对应的买入成交，按成交时间倒序；尚未生成持仓时返回空列表；重建持仓后只展示新一套成交，旧记录保留在数据库。成交不含手续费。
// @Tags 模拟持仓
// @Produce json
// @Param page query int false "页码" default(1) minimum(1)
// @Param pageSize query int false "每页数量" default(20) minimum(1) maximum(100)
// @Security BearerAuth
// @Success 200 {object} TradeResponse
// @Failure 400 {object} response.ErrorEnvelope
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 500 {object} response.ErrorEnvelope
// @Router /api/app/portfolio/trades [get]
func (h *Handler) Trades(c *gin.Context) {
	c.Header("Cache-Control", "no-store")
	page, e1 := strconv.Atoi(c.DefaultQuery("page", "1"))
	size, e2 := strconv.Atoi(c.DefaultQuery("pageSize", "20"))
	if e1 != nil || e2 != nil || page < 1 || page > 1000000 || size < 1 || size > 100 {
		response.Error(c, http.StatusBadRequest, response.CodeInvalidRequest, "page须为1至1000000，pageSize须为1至100")
		return
	}
	data, err := h.service.Trades(c.Request.Context(), c.GetUint64(auth.UserIDContextKey), page, size)
	if err != nil {
		h.fail(c, err)
		return
	}
	response.Success(c, data)
}

func (h *Handler) fail(c *gin.Context, err error) {
	if errors.Is(err, market.ErrInvalidCategory) {
		response.Error(c, http.StatusBadRequest, response.CodeInvalidRequest, "行情分类无效")
		return
	}
	if errors.Is(err, market.ErrNoInstruments) {
		response.Error(c, http.StatusNotFound, response.CodeNotFound, "该分类暂无行情")
		return
	}
	if errors.Is(err, ErrInvalidAssets) || errors.Is(err, ErrInsufficientQuotes) {
		response.Error(c, http.StatusBadRequest, response.CodeInvalidRequest, err.Error())
		return
	}
	slog.Error("portfolio request", "requestId", c.GetString("requestId"), "error", err)
	response.Error(c, http.StatusInternalServerError, response.CodeInternalError, "服务器内部错误")
}

// Holdings 获取分类模拟持仓
// @Summary 获取分类的3-5个持仓及每个15条交易记录
// @Description 按用户和分类独立保存，不返回quote行情和零持仓。传totalAssets会物理删除当前分类旧记录并生成全新3-5个持仓，每个15条成交；不传时有记录直接查库，无记录在200000至1000000元间随机生成资产后落库。各分类独立使用各自总资产，不合并计算，不影响其他分类和原portfolio接口。金额不足以买入3个标的最小交易单位时返回400且保留旧记录。资产、市值和盈亏为人民币，成本/现价及成交为原币种；USD按7.10、HKD按0.91演示汇率折算，税费为0。id仅在当前分类generation内唯一。
// @Tags 行情
// @Produce json
// @Param category query string true "行情分类" Enums(global,a_stock,hk_stock,us_stock,etf,future,gold,convertible_bond,other)
// @Param totalAssets query string false "按指定总资产重建演示数据（人民币元），10000至1000000000，最多两位小数，如500000.00"
// @Security BearerAuth
// @Success 200 {object} HoldingsResponse
// @Failure 400 {object} response.ErrorEnvelope
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} response.ErrorEnvelope
// @Failure 500 {object} response.ErrorEnvelope
// @Router /api/app/market/holdings [get]
func (h *Handler) Holdings(c *gin.Context) {
	c.Header("Cache-Control", "no-store")
	var assets *string
	if values, exists := c.Request.URL.Query()["totalAssets"]; exists {
		if len(values) != 1 {
			response.Error(c, http.StatusBadRequest, response.CodeInvalidRequest, ErrInvalidAssets.Error())
			return
		}
		assets = &values[0]
	}
	data, err := h.service.Holdings(c.Request.Context(), c.GetUint64(auth.UserIDContextKey), c.Query("category"), assets)
	if err != nil {
		h.fail(c, err)
		return
	}
	response.Success(c, data)
}

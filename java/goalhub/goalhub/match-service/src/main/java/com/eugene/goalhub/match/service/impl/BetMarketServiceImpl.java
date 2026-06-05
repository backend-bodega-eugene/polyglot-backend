package com.eugene.goalhub.match.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eugene.goalhub.match.entity.BetMarketEntity;
import com.eugene.goalhub.match.entity.BetMarketOptionEntity;
import com.eugene.goalhub.match.mapper.BetMarketMapper;
import com.eugene.goalhub.match.mapper.BetMarketOptionMapper;
import com.eugene.goalhub.match.service.BetMarketService;
import com.eugene.goalhub.match.service.support.MatchOperationLogger;
import dto.*;
import exception.BusinessException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import response.ResultCode;

import java.util.List;

/**
 * 投注玩法管理服务实现。
 *
 * <p>负责投注玩法和投注玩法选项的分页查询、列表查询、新增、更新和删除。</p>
 */
@Service
public class BetMarketServiceImpl implements BetMarketService {

    /**
     * 业务日志模块名称。
     */
    private static final String MODULE_NAME = "投注玩法管理";

    /**
     * 默认页码。
     */
    private static final int DEFAULT_PAGE_INDEX = 1;

    /**
     * 默认和最大每页数量。
     */
    private static final int DEFAULT_PAGE_SIZE = 100;

    /**
     * 投注玩法 Mapper。
     */
    private final BetMarketMapper betMarketMapper;

    /**
     * 投注玩法选项 Mapper。
     */
    private final BetMarketOptionMapper betMarketOptionMapper;

    /**
     * 比赛服务操作日志工具。
     */
    private final MatchOperationLogger matchOperationLogger;

    /**
     * 创建投注玩法管理服务实现。
     *
     * @param betMarketMapper       投注玩法 Mapper
     * @param betMarketOptionMapper 投注玩法选项 Mapper
     */
    public BetMarketServiceImpl(
            BetMarketMapper betMarketMapper,
            BetMarketOptionMapper betMarketOptionMapper,
            MatchOperationLogger matchOperationLogger) {
        this.betMarketMapper = betMarketMapper;
        this.betMarketOptionMapper = betMarketOptionMapper;
        this.matchOperationLogger = matchOperationLogger;
    }

    /**
     * 分页查询投注玩法。
     *
     * @param request 投注玩法分页查询条件
     * @return 投注玩法分页数据
     */
    @Override
    public PageResponse<BetMarketResponse> page(
            BetMarketPageRequest request) {
        if (request == null) {
            request = new BetMarketPageRequest();
        }
        initPage(request);
        String keyword = request.getKeyword();
        String status = request.getStatus();
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasStatus = status != null && !status.isBlank();

        Page<BetMarketEntity> page = new Page<>(
                request.getPageIndex(),
                request.getPageSize()
        );

        Page<BetMarketEntity> result = betMarketMapper.selectPage(
                page,
                Wrappers.lambdaQuery(BetMarketEntity.class)
                        .and(hasKeyword,
                                wrapper -> wrapper
                                        .like(BetMarketEntity::getName, keyword)
                                        .or()
                                        .like(BetMarketEntity::getCode, keyword))
                        .eq(hasStatus,
                                BetMarketEntity::getStatus,
                                status)
                        .orderByAsc(BetMarketEntity::getSortOrder)
                        .orderByDesc(BetMarketEntity::getCreatedAt)
        );

        List<BetMarketResponse> records = result.getRecords()
                .stream()
                .map(this::toBetMarketResponse)
                .toList();

        matchOperationLogger.sysLog(
                MODULE_NAME,
                "BET_MARKET_PAGE",
                "分页查询投注玩法，pageIndex=" + request.getPageIndex()
                        + ", pageSize=" + request.getPageSize()
                        + ", keyword=" + request.getKeyword()
                        + ", status=" + request.getStatus()
                        + ", total=" + result.getTotal()
        );
        return new PageResponse<>(
                result.getTotal(),
                request.getPageIndex(),
                request.getPageSize(),
                records
        );
    }

    /**
     * 新增投注玩法。
     *
     * @param request 投注玩法新增参数
     */
    @Override
    public void add(
            AddBetMarketRequest request) {

        Long count = betMarketMapper.selectCount(
                Wrappers.lambdaQuery(BetMarketEntity.class)
                        .eq(BetMarketEntity::getCode, request.getCode())
        );

        if (count > 0) {
            throw new BusinessException(ResultCode.BET_MARKET_CODE_ALREADY_EXISTS);
        }

        BetMarketEntity entity = new BetMarketEntity();
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setStatus(defaultStatus(request.getStatus()));
        entity.setSortOrder(defaultSortOrder(request.getSortOrder()));

        betMarketMapper.insert(entity);
        matchOperationLogger.adminBizLog(
                MODULE_NAME,
                "ADD_BET_MARKET",
                "新增投注玩法成功，marketId=" + entity.getId() + ", code=" + entity.getCode()
        );
    }

    /**
     * 更新投注玩法。
     *
     * @param request 投注玩法更新参数
     */
    @Override
    public void update(
            UpdateBetMarketRequest request) {

        BetMarketEntity entity = betMarketMapper.selectById(request.getId());

        if (entity == null) {
            throw new BusinessException(ResultCode.BET_MARKET_NOT_FOUND);
        }

        Long count = betMarketMapper.selectCount(
                Wrappers.lambdaQuery(BetMarketEntity.class)
                        .eq(BetMarketEntity::getCode, request.getCode())
                        .ne(BetMarketEntity::getId, request.getId())
        );

        if (count > 0) {
            throw new BusinessException(ResultCode.BET_MARKET_CODE_ALREADY_EXISTS);
        }

        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setStatus(defaultStatus(request.getStatus()));
        entity.setSortOrder(defaultSortOrder(request.getSortOrder()));

        betMarketMapper.updateById(entity);
        matchOperationLogger.adminBizLog(
                MODULE_NAME,
                "UPDATE_BET_MARKET",
                "更新投注玩法成功，marketId=" + entity.getId() + ", code=" + entity.getCode()
        );
    }

    /**
     * 删除投注玩法。
     *
     * @param request 投注玩法删除参数
     */
    @Override
    public void delete(
            DeleteBetMarketRequest request) {

        BetMarketEntity entity = betMarketMapper.selectById(request.getId());

        if (entity == null) {
            throw new BusinessException(ResultCode.BET_MARKET_NOT_FOUND);
        }

        Long optionCount = betMarketOptionMapper.selectCount(
                Wrappers.lambdaQuery(BetMarketOptionEntity.class)
                        .eq(BetMarketOptionEntity::getMarketId, request.getId())
        );

        if (optionCount > 0) {
            throw new BusinessException(ResultCode.BET_MARKET_HAS_OPTIONS);
        }

        betMarketMapper.deleteById(request.getId());
        matchOperationLogger.adminBizLog(
                MODULE_NAME,
                "DELETE_BET_MARKET",
                "删除投注玩法成功，marketId=" + request.getId() + ", code=" + entity.getCode()
        );
    }

    /**
     * 查询投注玩法选项列表。
     *
     * @param request 投注玩法选项查询条件
     * @return 投注玩法选项列表
     */
    @Override
    public List<BetMarketOptionResponse> optionList(
            BetMarketOptionListRequest request) {

        List<BetMarketOptionEntity> list = betMarketOptionMapper.selectList(
                Wrappers.lambdaQuery(BetMarketOptionEntity.class)
                        .eq(BetMarketOptionEntity::getMarketId, request.getMarketId())
                        .eq(request.getStatus() != null && !request.getStatus().isBlank(),
                                BetMarketOptionEntity::getStatus,
                                request.getStatus())
                        .orderByAsc(BetMarketOptionEntity::getSortOrder)
                        .orderByDesc(BetMarketOptionEntity::getCreatedAt)
        );

        List<BetMarketOptionResponse> responses = list.stream()
                .map(this::toBetMarketOptionResponse)
                .toList();
        matchOperationLogger.sysLog(
                MODULE_NAME,
                "BET_MARKET_OPTION_LIST",
                "查询投注玩法选项列表，marketId=" + request.getMarketId()
                        + ", status=" + request.getStatus()
                        + ", resultCount=" + responses.size()
        );
        return responses;
    }

    /**
     * 新增投注玩法选项。
     *
     * @param request 投注玩法选项新增参数
     */
    @Override
    public void addOption(
            AddBetMarketOptionRequest request) {

        BetMarketEntity market = betMarketMapper.selectById(request.getMarketId());

        if (market == null) {
            throw new BusinessException(ResultCode.BET_MARKET_NOT_FOUND);
        }

        Long count = betMarketOptionMapper.selectCount(
                Wrappers.lambdaQuery(BetMarketOptionEntity.class)
                        .eq(BetMarketOptionEntity::getMarketId, request.getMarketId())
                        .eq(BetMarketOptionEntity::getCode, request.getCode())
        );

        if (count > 0) {
            throw new BusinessException(ResultCode.BET_MARKET_OPTION_CODE_ALREADY_EXISTS);
        }

        BetMarketOptionEntity entity = new BetMarketOptionEntity();
        entity.setMarketId(request.getMarketId());
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setStatus(defaultStatus(request.getStatus()));
        entity.setSortOrder(defaultSortOrder(request.getSortOrder()));

        betMarketOptionMapper.insert(entity);
        matchOperationLogger.adminBizLog(
                MODULE_NAME,
                "ADD_BET_MARKET_OPTION",
                "新增投注玩法选项成功，optionId=" + entity.getId()
                        + ", marketId=" + entity.getMarketId()
                        + ", code=" + entity.getCode()
        );
    }

    /**
     * 更新投注玩法选项。
     *
     * @param request 投注玩法选项更新参数
     */
    @Override
    public void updateOption(
            UpdateBetMarketOptionRequest request) {

        BetMarketOptionEntity entity =
                betMarketOptionMapper.selectById(request.getId());

        if (entity == null) {
            throw new BusinessException(ResultCode.BET_MARKET_OPTION_NOT_FOUND);
        }

        BetMarketEntity market = betMarketMapper.selectById(request.getMarketId());

        if (market == null) {
            throw new BusinessException(ResultCode.BET_MARKET_NOT_FOUND);
        }

        Long count = betMarketOptionMapper.selectCount(
                Wrappers.lambdaQuery(BetMarketOptionEntity.class)
                        .eq(BetMarketOptionEntity::getMarketId, request.getMarketId())
                        .eq(BetMarketOptionEntity::getCode, request.getCode())
                        .ne(BetMarketOptionEntity::getId, request.getId())
        );

        if (count > 0) {
            throw new BusinessException(ResultCode.BET_MARKET_OPTION_CODE_ALREADY_EXISTS);
        }

        entity.setMarketId(request.getMarketId());
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setStatus(defaultStatus(request.getStatus()));
        entity.setSortOrder(defaultSortOrder(request.getSortOrder()));

        betMarketOptionMapper.updateById(entity);
        matchOperationLogger.adminBizLog(
                MODULE_NAME,
                "UPDATE_BET_MARKET_OPTION",
                "更新投注玩法选项成功，optionId=" + entity.getId()
                        + ", marketId=" + entity.getMarketId()
                        + ", code=" + entity.getCode()
        );
    }

    /**
     * 删除投注玩法选项。
     *
     * @param request 投注玩法选项删除参数
     */
    @Override
    public void deleteOption(
            DeleteBetMarketOptionRequest request) {

        BetMarketOptionEntity entity =
                betMarketOptionMapper.selectById(request.getId());

        if (entity == null) {
            throw new BusinessException(ResultCode.BET_MARKET_OPTION_NOT_FOUND);
        }

        betMarketOptionMapper.deleteById(request.getId());
        matchOperationLogger.adminBizLog(
                MODULE_NAME,
                "DELETE_BET_MARKET_OPTION",
                "删除投注玩法选项成功，optionId=" + request.getId()
                        + ", marketId=" + entity.getMarketId()
                        + ", code=" + entity.getCode()
        );
    }

    /**
     * 转换投注玩法实体为响应对象。
     *
     * @param entity 投注玩法实体
     * @return 投注玩法响应对象
     */
    private BetMarketResponse toBetMarketResponse(
            BetMarketEntity entity) {

        BetMarketResponse response = new BetMarketResponse();
        BeanUtils.copyProperties(entity, response);
        return response;
    }

    /**
     * 转换投注玩法选项实体为响应对象。
     *
     * @param entity 投注玩法选项实体
     * @return 投注玩法选项响应对象
     */
    private BetMarketOptionResponse toBetMarketOptionResponse(
            BetMarketOptionEntity entity) {

        BetMarketOptionResponse response = new BetMarketOptionResponse();
        BeanUtils.copyProperties(entity, response);
        return response;
    }

    /**
     * 获取默认启用状态。
     *
     * @param status 请求状态
     * @return 非空状态
     */
    private String defaultStatus(
            String status) {

        if (status == null || status.isBlank()) {
            return "ENABLED";
        }

        return status;
    }

    /**
     * 获取默认排序值。
     *
     * @param sortOrder 请求排序值
     * @return 非空排序值
     */
    private Integer defaultSortOrder(
            Integer sortOrder) {

        if (sortOrder == null) {
            return 0;
        }

        return sortOrder;
    }

    /**
     * 初始化分页参数。
     *
     * @param request 分页请求
     */
    private void initPage(BetMarketPageRequest request) {
        if (request.getPageIndex() == null || request.getPageIndex() < 1) {
            request.setPageIndex(DEFAULT_PAGE_INDEX);
        }

        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(DEFAULT_PAGE_SIZE);
            return;
        }

        if (request.getPageSize() > DEFAULT_PAGE_SIZE) {
            request.setPageSize(DEFAULT_PAGE_SIZE);
        }
    }
}

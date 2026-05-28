package com.eugene.goalhub.match.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eugene.goalhub.match.entity.SoccerMatchEntity;
import com.eugene.goalhub.match.mapper.SoccerMatchMapper;
import com.eugene.goalhub.match.service.SoccerMatchService;
import dto.*;
import exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import response.ResultCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 足球比赛查询服务实现。
 */
@Service
public class SoccerMatchServiceImpl
        extends ServiceImpl<SoccerMatchMapper, SoccerMatchEntity>
        implements SoccerMatchService {

    /**
     * 未开始状态编码。
     */
    private static final String STATUS_NOT_STARTED = "NOT_STARTED";

    /**
     * 已结束状态编码。
     */
    private static final String STATUS_FINISHED = "FINISHED";

    /**
     * 数据库查询使用的 UTC 时间字符串格式。
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 分页查询足球比赛。
     *
     * @param request 分页和筛选条件
     * @return 比赛分页结果
     */
    @Override
    public PageResponse<SoccerMatchListResponse> pageMatches(SoccerMatchPageRequest request) {

        if (request.getPageIndex() == null) {
            throw new BusinessException(ResultCode.PAGEINDEX_NOT_NULL);
        }

        if (request.getPageSize() == null) {
            throw new BusinessException(ResultCode.PAGESIZE_NOT_NULL);
        }

        if (!StringUtils.hasText(request.getLangCode())) {
            request.setLangCode("en-US");
        }

        // 使用 MyBatis-Plus 分页对象承载自定义 SQL 查询结果。
        Page<SoccerMatchListResponse> page =
                new Page<>(request.getPageIndex(), request.getPageSize());

        Page<SoccerMatchListResponse> result =
                baseMapper.selectMatchPage(page, request);

        PageResponse<SoccerMatchListResponse> response = new PageResponse<>();
        response.setTotal(result.getTotal());
        response.setPageIndex(request.getPageIndex());
        response.setPageSize(request.getPageSize());
        response.setRecords(result.getRecords());

        return response;
    }

    /**
     * 查询足球比赛详情。
     *
     * @param id      赛事 ID
     * @param request 详情查询参数
     * @return 赛事详情
     */
    @Override
    public SoccerMatchDetailResponse getMatchDetail(Long id, SoccerMatchDetailRequest request) {

        String langCode = "en-US";

        if (request != null && StringUtils.hasText(request.getLangCode())) {
            langCode = request.getLangCode();
        }

        return baseMapper.selectMatchDetail(id, langCode);
    }

    /**
     * 分页查询 UTC 当天的比赛。
     *
     * @param request 分页和筛选条件
     * @return 今日比赛分页结果
     */
    @Override
    public PageResponse<SoccerMatchListResponse> pageTodayMatches(SoccerMatchPageRequest request) {
        initPageIfNecessary(request);

        LocalDate todayUtc = LocalDate.now(ZoneOffset.UTC);

        LocalDateTime start = todayUtc.atStartOfDay();
        LocalDateTime end = todayUtc.plusDays(1).atStartOfDay().minusSeconds(1);

        request.setStartTimeUtc(start.format(DATE_TIME_FORMATTER));
        request.setEndTimeUtc(end.format(DATE_TIME_FORMATTER));

        return pageMatches(request);
    }

    /**
     * 分页查询即将开始的比赛。
     * <p>
     * 查询范围为当前 UTC 时间到 UTC 明天结束。
     *
     * @param request 分页和筛选条件
     * @return 即将开始比赛分页结果
     */
    @Override
    public PageResponse<SoccerMatchListResponse> pageUpcomingMatches(SoccerMatchPageRequest request) {
        initPageIfNecessary(request);

        LocalDate todayUtc = LocalDate.now(ZoneOffset.UTC);

        LocalDateTime start = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime end = todayUtc.plusDays(2).atStartOfDay().minusSeconds(1);

        request.setStatus(STATUS_NOT_STARTED);
        request.setStartTimeUtc(start.format(DATE_TIME_FORMATTER));
        request.setEndTimeUtc(end.format(DATE_TIME_FORMATTER));

        return pageMatches(request);
    }

    /**
     * 分页查询已结束比赛。
     *
     * @param request 分页和筛选条件
     * @return 已结束比赛分页结果
     */
    @Override
    public PageResponse<SoccerMatchListResponse> pageFinishedMatches(SoccerMatchPageRequest request) {
        initPageIfNecessary(request);

        request.setStatus(STATUS_FINISHED);

        return pageMatches(request);
    }


    /**
     * 检查赛事是否存在。
     *
     * @param matchId 赛事 ID
     * @return true 表示存在
     */
    @Override
    public boolean existsById(Long matchId) {
        return lambdaQuery()
                .eq(SoccerMatchEntity::getId, matchId)
                .exists();
    }

    /**
     * 查询热门比赛。
     *
     * @param request 热门比赛查询参数
     * @return 热门比赛列表
     */
    @Override
    public List<SoccerMatchListResponse> listHotMatches(SoccerHotMatchRequest request) {

        if (request == null) {
            request = new SoccerHotMatchRequest();
        }

        if (!StringUtils.hasText(request.getLangCode())) {
            request.setLangCode("en-US");
        }

        if (request.getLimit() == null || request.getLimit() <= 0) {
            request.setLimit(10);
        }

        if (request.getLimit() > 50) {
            request.setLimit(50);
        }

        return baseMapper.selectHotMatches(request);
    }

    /**
     * 如果分页参数为空，则补充默认分页值。
     *
     * @param request 分页查询请求
     */
    private void initPageIfNecessary(SoccerMatchPageRequest request) {
        if (request.getPageIndex() == null) {
            request.setPageIndex(1);
        }

        if (request.getPageSize() == null) {
            request.setPageSize(10);
        }
    }
}

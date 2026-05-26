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

@Service
public class SoccerMatchServiceImpl
        extends ServiceImpl<SoccerMatchMapper, SoccerMatchEntity>
        implements SoccerMatchService {
    private static final String STATUS_NOT_STARTED = "NOT_STARTED";
    private static final String STATUS_FINISHED = "FINISHED";

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
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

    @Override
    public SoccerMatchDetailResponse getMatchDetail(Long id, SoccerMatchDetailRequest request) {

        String langCode = "en-US";

        if (request != null && StringUtils.hasText(request.getLangCode())) {
            langCode = request.getLangCode();
        }

        return baseMapper.selectMatchDetail(id, langCode);
    }
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

    @Override
    public PageResponse<SoccerMatchListResponse> pageFinishedMatches(SoccerMatchPageRequest request) {
        initPageIfNecessary(request);

        request.setStatus(STATUS_FINISHED);

        return pageMatches(request);
    }


    @Override
    public boolean existsById(Long matchId) {
        return lambdaQuery()
                .eq(SoccerMatchEntity::getId, matchId)
                .exists();
    }
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
    private void initPageIfNecessary(SoccerMatchPageRequest request) {
        if (request.getPageIndex() == null) {
            request.setPageIndex(1);
        }

        if (request.getPageSize() == null) {
            request.setPageSize(10);
        }
    }
}
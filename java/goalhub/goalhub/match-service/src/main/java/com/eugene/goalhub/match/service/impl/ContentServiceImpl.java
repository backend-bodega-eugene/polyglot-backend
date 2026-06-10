package com.eugene.goalhub.match.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eugene.goalhub.match.entity.content.ContentEntity;
import com.eugene.goalhub.match.entity.content.constant.ContentStatus;
import com.eugene.goalhub.match.entity.content.constant.ContentType;
import com.eugene.goalhub.match.mapper.ContentMapper;
import com.eugene.goalhub.match.service.ContentService;
import dto.*;
import exception.BusinessException;
import org.springframework.stereotype.Service;
import response.ResultCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 内容管理服务实现。
 *
 * <p>负责后台内容维护，以及 App 端文章、消息和让球教程查询。</p>
 */
@Service
public class ContentServiceImpl extends ServiceImpl<ContentMapper, ContentEntity> implements ContentService {

    /**
     * 创建内容。
     *
     * @param request 内容创建参数
     * @return 新内容 ID
     */
    @Override
    public Long create(AdminContentCreateRequest request) {
        ContentEntity entity = new ContentEntity();
        entity.setType(request.getType());
        entity.setTitle(request.getTitle());
        entity.setSummary(request.getSummary());
        entity.setCoverUrl(request.getCoverUrl());
        entity.setContentHtml(request.getContentHtml());
        entity.setStatus(request.getStatus() == null ? ContentStatus.DRAFT : request.getStatus());
        entity.setSort(request.getSort() == null ? 0 : request.getSort());
        entity.setPublishTime(request.getPublishTime());

        save(entity);
        return entity.getId();
    }

    /**
     * 更新内容。
     *
     * @param id      内容 ID
     * @param request 内容更新参数
     */
    @Override
    public void update(Long id, AdminContentUpdateRequest request) {
        checkExists(id);

        lambdaUpdate()
                .eq(ContentEntity::getId, id)
                .set(ContentEntity::getType, request.getType())
                .set(ContentEntity::getTitle, request.getTitle())
                .set(ContentEntity::getSummary, request.getSummary())
                .set(ContentEntity::getCoverUrl, request.getCoverUrl())
                .set(ContentEntity::getContentHtml, request.getContentHtml())
                .set(ContentEntity::getStatus, request.getStatus())
                .set(ContentEntity::getSort, request.getSort())
                .set(ContentEntity::getPublishTime, request.getPublishTime())
                .update();
    }

    /**
     * 删除内容。
     *
     * @param id 内容 ID
     */
    @Override
    public void delete(Long id) {
        checkExists(id);
        removeById(id);
    }

    /**
     * 查询后台内容详情。
     *
     * @param id 内容 ID
     * @return 内容详情，不存在时返回 null
     */
    @Override
    public ContentResponse getAdminDetail(Long id) {
        ContentEntity entity = getById(id);
        if (entity == null) {
            return null;
        }
        return toResponse(entity);
    }

    /**
     * 分页查询后台内容。
     *
     * @param request 后台内容分页查询参数
     * @return 内容分页数据
     */
    @Override
    public PageResponse<ContentResponse> adminPage(AdminContentPageRequest request) {
        Page<ContentEntity> page = lambdaQuery()
                .eq(request.getType() != null && !request.getType().isBlank(), ContentEntity::getType, request.getType())
                .eq(request.getStatus() != null && !request.getStatus().isBlank(), ContentEntity::getStatus, request.getStatus())
                .like(request.getKeyword() != null && !request.getKeyword().isBlank(), ContentEntity::getTitle, request.getKeyword())
                .orderByDesc(ContentEntity::getSort)
                .orderByDesc(ContentEntity::getId)
                .page(new Page<>(request.getPageIndex(), request.getPageSize()));

        return new PageResponse<ContentResponse> (
                page.getTotal(),
                Math.toIntExact(page.getCurrent()),
                Math.toIntExact(page.getSize()),
                page.getRecords().stream().map(this::toResponse).toList()
        );
    }

    /**
     * 查询 App 内容详情。
     *
     * <p>仅返回已发布且发布时间不晚于当前时间的内容。</p>
     *
     * @param id 内容 ID
     * @return 内容详情，不存在或不可见时返回 null
     */
    @Override
    public ContentResponse getAppDetail(Long id) {
        ContentEntity entity = lambdaQuery()
                .eq(ContentEntity::getId, id)
                .eq(ContentEntity::getStatus, ContentStatus.PUBLISHED)
                .le(ContentEntity::getPublishTime, LocalDateTime.now())
                .one();

        if (entity == null) {
            return null;
        }
        return toResponse(entity);
    }

    /**
     * 查询让球教程内容。
     *
     * @return 最新一条已发布的让球教程内容，不存在时返回 null
     */
    @Override
    public ContentResponse getHandicapTutorial() {
        ContentEntity entity = lambdaQuery()
                .eq(ContentEntity::getType, ContentType.HANDICAP_TUTORIAL)
                .eq(ContentEntity::getStatus, ContentStatus.PUBLISHED)
               // .le(ContentEntity::getPublishTime, LocalDateTime.now())
                .orderByDesc(ContentEntity::getSort)
                .orderByDesc(ContentEntity::getId)
                .last("LIMIT 1")
                .one();

        if (entity == null) {
            return null;
        }
        return toResponse(entity);
    }
    @Override
    public ContentResponse about() {
        ContentEntity entity = lambdaQuery()
                .eq(ContentEntity::getType, ContentType.ABOUT)
                .eq(ContentEntity::getStatus, ContentStatus.PUBLISHED)
                // .le(ContentEntity::getPublishTime, LocalDateTime.now())
                .orderByDesc(ContentEntity::getSort)
                .orderByDesc(ContentEntity::getId)
                .last("LIMIT 1")
                .one();

        if (entity == null) {
            return null;
        }
        return toResponse(entity);
    }

    /**
     * 分页查询 App 文章内容。
     *
     * @param request App 内容分页查询参数
     * @return 文章分页数据
     */
    @Override
    public PageResponse<ContentResponse> appArticlePage(AppContentPageRequest request) {
        return appPage(request, ContentType.ARTICLE);
    }

    /**
     * 分页查询 App 消息内容。
     *
     * @param request App 内容分页查询参数
     * @return 消息分页数据
     */
    @Override
    public PageResponse<ContentResponse> appMessagePage(AppContentPageRequest request) {
        return appPage(request, ContentType.MESSAGE);
    }

    /**
     * 分页查询 App 指定类型内容。
     *
     * @param request App 内容分页查询参数
     * @param type    内容类型
     * @return 内容分页数据
     */
    private PageResponse<ContentResponse> appPage(AppContentPageRequest request, String type) {
        Page<ContentEntity> page = lambdaQuery()
                .eq(ContentEntity::getType, type)
                .eq(ContentEntity::getStatus, ContentStatus.PUBLISHED)
                .le(ContentEntity::getPublishTime, LocalDateTime.now())
                .orderByDesc(ContentEntity::getSort)
                .orderByDesc(ContentEntity::getId)
                .page(new Page<>(request.getPageIndex(), request.getPageSize()));

//        List<ContentResponse> records = page.getRecords()
//                .stream()
//                .map(this::toResponse)
//                .toList();

        return new PageResponse<ContentResponse> (
                page.getTotal(),
                Math.toIntExact(page.getCurrent()),
                Math.toIntExact(page.getSize()),
                page.getRecords().stream().map(this::toResponse).toList()
        );
    }

    /**
     * 校验内容存在。
     *
     * @param id 内容 ID
     */
    private void checkExists(Long id) {
        if (getById(id) == null) {
            throw new BusinessException(ResultCode.CONTENT_NOT_FOUND);
        }
    }

    /**
     * 将内容实体转换为内容响应。
     *
     * @param entity 内容实体
     * @return 内容响应
     */
    private ContentResponse toResponse(ContentEntity entity) {
        ContentResponse response = new ContentResponse();
        response.setId(entity.getId());
        response.setType(entity.getType());
        response.setTitle(entity.getTitle());
        response.setSummary(entity.getSummary());
        response.setCoverUrl(entity.getCoverUrl());
        response.setContentHtml(entity.getContentHtml());
        response.setStatus(entity.getStatus());
        response.setSort(entity.getSort());
        response.setPublishTime(entity.getPublishTime());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}

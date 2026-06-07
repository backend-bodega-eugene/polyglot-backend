package com.eugene.goalhub.match.service;

import dto.*;

public interface ContentService {

    Long create(AdminContentCreateRequest request);

    void update(Long id, AdminContentUpdateRequest request);

    void delete(Long id);

    ContentResponse getAdminDetail(Long id);

    PageResponse<ContentResponse> adminPage(AdminContentPageRequest request);

    ContentResponse getAppDetail(Long id);

    ContentResponse getHandicapTutorial();

    PageResponse<ContentResponse> appArticlePage(AppContentPageRequest request);

    PageResponse<ContentResponse> appMessagePage(AppContentPageRequest request);
}
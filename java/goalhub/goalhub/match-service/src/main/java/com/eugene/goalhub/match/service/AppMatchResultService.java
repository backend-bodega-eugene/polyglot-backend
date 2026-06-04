package com.eugene.goalhub.match.service;

import dto.AppMatchResultPageRequest;
import dto.AppMatchResultResponse;
import dto.PageResponse;

public interface AppMatchResultService {

    PageResponse<AppMatchResultResponse> pageResult(
            AppMatchResultPageRequest request);
}
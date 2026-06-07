package com.eugene.goalhub.match.controller;


import com.eugene.goalhub.match.service.ContentService;
import dto.AppContentPageRequest;
import dto.ContentResponse;
import dto.PageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import response.Result;

@RestController
@RequestMapping("/soccer/contents")
public class AppContentController {

    private final ContentService contentService;

    public AppContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping("/articles/handicaptutorial")
    public Result<ContentResponse> handicapTutorial() {
        return Result.success(contentService.getHandicapTutorial());
    }

    @GetMapping("/articles")
    public Result<PageResponse<ContentResponse>> articles(AppContentPageRequest request) {
        return Result.success(contentService.appArticlePage(request));
    }

    @GetMapping("/messages")
    public Result<PageResponse<ContentResponse>> messages(AppContentPageRequest request) {
        return Result.success(contentService.appMessagePage(request));
    }

    @GetMapping("/{id}")
    public Result<ContentResponse> detail(@PathVariable("id") Long id) {
        return Result.success(contentService.getAppDetail(id));
    }
}
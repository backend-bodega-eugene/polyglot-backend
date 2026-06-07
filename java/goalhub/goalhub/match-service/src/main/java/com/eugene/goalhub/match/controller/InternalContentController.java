package com.eugene.goalhub.match.controller;


import com.eugene.goalhub.match.service.ContentService;
import dto.*;
import org.springframework.web.bind.annotation.*;
import response.Result;

@RestController
@RequestMapping("/internal/contents")
public class InternalContentController {

    private final ContentService contentService;

    public InternalContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @PostMapping
    public Result<Long> create(@RequestBody AdminContentCreateRequest request) {
        return Result.success(contentService.create(request));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long id,
                               @RequestBody AdminContentUpdateRequest request) {
        contentService.update(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        contentService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<ContentResponse> detail(@PathVariable("id") Long id) {
        return Result.success(contentService.getAdminDetail(id));
    }

    @PostMapping("/page")
    public Result<PageResponse<ContentResponse>> page(@RequestBody AdminContentPageRequest request) {
        return Result.success(contentService.adminPage(request));
    }
}
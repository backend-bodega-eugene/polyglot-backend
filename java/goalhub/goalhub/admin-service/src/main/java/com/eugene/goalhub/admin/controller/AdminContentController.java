package com.eugene.goalhub.admin.controller;

import com.eugene.goalhub.admin.service.AdminContentService;
import dto.AdminContentCreateRequest;
import dto.AdminContentPageRequest;
import dto.AdminContentUpdateRequest;
import dto.ContentResponse;
import dto.PageResponse;
import org.springframework.web.bind.annotation.*;
import response.Result;

@RestController
@RequestMapping("/admin/contents")
public class AdminContentController {

    private final AdminContentService adminContentService;

    public AdminContentController(AdminContentService adminContentService) {
        this.adminContentService = adminContentService;
    }

    @PostMapping
    public Result<Long> create(@RequestBody AdminContentCreateRequest request) {
        return adminContentService.create(request);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long id,
                               @RequestBody AdminContentUpdateRequest request) {
        return adminContentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        return adminContentService.delete(id);
    }

    @GetMapping("/{id}")
    public Result<ContentResponse> detail(@PathVariable("id") Long id) {
        return adminContentService.detail(id);
    }

    @PostMapping("/page")
    public Result<PageResponse<ContentResponse>> page(@RequestBody AdminContentPageRequest request) {
        return adminContentService.page(request);
    }
}
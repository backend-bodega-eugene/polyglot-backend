package com.eugene.goalhub.admin.client;

import dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * user-service 内部管理接口 Feign 客户端。
 */
@FeignClient(name = "user-service")
public interface UserServiceClient {

    /**
     * 分页查询应用用户。
     *
     * @param request 分页和筛选条件
     * @return 应用用户分页结果
     */
    @PostMapping("/internal/admin/users/page")
    Result<PageResponse<UserAdminPageResponse>> page(@RequestBody UserAdminPageRequest request);

    /**
     * 创建应用用户。
     *
     * @param request 创建参数
     * @return 新用户 ID
     */
    @PostMapping("/internal/admin/users")
    Result<Long> create(@RequestBody UserAdminCreateRequest request);

    /**
     * 更新应用用户信息。
     *
     * @param id      应用用户 ID
     * @param request 更新参数
     * @return 空结果
     */
    @PutMapping("/internal/admin/users/{id}")
    Result<Void> update(@PathVariable("id") Long id,
                        @RequestBody UserAdminUpdateRequest request);

    /**
     * 删除应用用户。
     *
     * @param id 应用用户 ID
     * @return 空结果
     */
    @DeleteMapping("/internal/admin/users/{id}")
    Result<Void> delete(@PathVariable("id") Long id);

    /**
     * 修改应用用户密码。
     *
     * @param id      应用用户 ID
     * @param request 密码更新参数
     * @return 空结果
     */
    @PutMapping("/internal/admin/users/{id}/password")
    Result<Void> updatePassword(@PathVariable("id") Long id,
                                @RequestBody UserAdminPasswordUpdateRequest request);
}

package com.eugene.goalhub.admin.service;

import dto.*;

/**
 * 后台管理员账号管理服务。
 */
public interface AdminUserService {

    /**
     * 管理员登录。
     *
     * @param request 登录参数
     * @return 登录结果，包含 token 和管理员基础信息
     */
    AdminLoginResponse login(AdminLoginRequest request);

    /**
     * 创建管理员账号。
     *
     * @param request             管理员创建参数
     * @param operatorAdminUserId 当前操作管理员 ID
     * @return 新管理员 ID
     */
    Long create(AdminUserCreateRequest request, Long operatorAdminUserId);

    /**
     * 更新管理员账号基础信息。
     *
     * @param id                  管理员 ID
     * @param operatorAdminUserId 当前操作管理员 ID
     * @param request             更新参数
     */
    void update(Long id, Long operatorAdminUserId, AdminUserUpdateRequest request);

    /**
     * 删除管理员账号。
     *
     * @param id 管理员 ID
     */
    void delete(Long id);

    /**
     * 修改管理员密码。
     *
     * @param id      管理员 ID
     * @param request 密码更新参数
     */
    void updatePassword(Long id, AdminPasswordUpdateRequest request);

    /**
     * 分页查询管理员账号。
     *
     * @param pageIndex 页码
     * @param pageSize  每页数量
     * @param username  用户名筛选条件
     * @return 管理员分页数据
     */
    PageResponse<AdminUserPageResponse> page(Integer pageIndex, Integer pageSize, String username);

    /**
     * 更新管理员启用状态。
     *
     * @param id     管理员 ID
     * @param status 状态值
     */
    void updateStatus(Long id, Integer status);
}

package com.eugene.goalhub.admin.service;

import dto.*;

/**
 * 后台应用用户管理服务。
 */
public interface AdminAppUserService {

    /**
     * 分页查询应用用户。
     *
     * @param request 分页和筛选条件
     * @return 应用用户分页数据
     */
    PageResponse<UserAdminPageResponse> page(UserAdminPageRequest request);

    /**
     * 创建应用用户。
     *
     * @param request 创建参数
     * @return 新用户 ID
     */
    Long create(UserAdminCreateRequest request);

    /**
     * 更新应用用户基础信息。
     *
     * @param id      应用用户 ID
     * @param request 更新参数
     */
    void update(Long id, UserAdminUpdateRequest request);

    /**
     * 删除应用用户。
     *
     * @param id 应用用户 ID
     */
    void delete(Long id);

    /**
     * 修改应用用户密码。
     *
     * @param id      应用用户 ID
     * @param request 密码更新参数
     */
    void updatePassword(Long id, UserAdminPasswordUpdateRequest request);
}

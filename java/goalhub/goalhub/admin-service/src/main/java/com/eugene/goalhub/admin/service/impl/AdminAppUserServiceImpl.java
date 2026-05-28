package com.eugene.goalhub.admin.service.impl;
import com.eugene.goalhub.admin.client.UserServiceClient;
import com.eugene.goalhub.admin.service.AdminAppUserService;
import dto.*;
import org.springframework.stereotype.Service;

/**
 * 后台应用用户管理服务实现。
 * <p>
 * 当前服务不直接访问用户表，而是通过 Feign 调用 user-service 的内部管理接口。
 */
@Service
public class AdminAppUserServiceImpl implements AdminAppUserService {

    /**
     * 用户服务远程调用客户端。
     */
    private final UserServiceClient userServiceClient;

    public AdminAppUserServiceImpl(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    /**
     * 分页查询应用用户列表。
     *
     * @param request 分页和筛选条件
     * @return 应用用户分页数据
     */
    @Override
    public PageResponse<UserAdminPageResponse> page(UserAdminPageRequest request) {
        return userServiceClient.page(request).getData();
    }

    /**
     * 创建应用用户。
     *
     * @param request 应用用户创建参数
     * @return 新用户 ID
     */
    @Override
    public Long create(UserAdminCreateRequest request) {
        return userServiceClient.create(request).getData();
    }

    /**
     * 更新应用用户基础信息。
     *
     * @param id      应用用户 ID
     * @param request 更新参数
     */
    @Override
    public void update(Long id, UserAdminUpdateRequest request) {
        userServiceClient.update(id, request);
    }

    /**
     * 删除应用用户。
     *
     * @param id 应用用户 ID
     */
    @Override
    public void delete(Long id) {
        userServiceClient.delete(id);
    }

    /**
     * 修改应用用户密码。
     *
     * @param id      应用用户 ID
     * @param request 密码更新参数
     */
    @Override
    public void updatePassword(Long id, UserAdminPasswordUpdateRequest request) {
        userServiceClient.updatePassword(id, request);
    }
}

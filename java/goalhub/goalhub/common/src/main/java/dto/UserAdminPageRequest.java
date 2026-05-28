package dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台应用用户分页查询请求。
 */
@Data
public class UserAdminPageRequest {

    /**
     * 页码。
     */
    private Integer pageIndex;

    /**
     * 每页数量。
     */
    private Integer pageSize;

    /**
     * 用户名筛选条件。
     */
    private String username;

    /**
     * 昵称筛选条件。
     */
    private String nickname;

    /**
     * 邮箱筛选条件。
     */
    private String email;

    /**
     * 手机号筛选条件。
     */
    private String phone;

    /**
     * 账号状态筛选条件。
     */
    private Integer status;

    /**
     * 创建时间起始范围。
     */
    private LocalDateTime createdAtStart;

    /**
     * 创建时间结束范围。
     */
    private LocalDateTime createdAtEnd;
}

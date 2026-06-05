package com.eugene.goalhub.user.controller;

import com.eugene.goalhub.user.service.UserAccountService;
import dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 内部后台用户账户管理接口。
 *
 * <p>提供 admin-service 远程调用的用户账户、账户流水、余额变更和状态维护接口。</p>
 */
@Tag(name = "内部后台用户账户管理", description = "内部后台用户账户、账户流水和余额管理接口")
@RestController
@RequestMapping("/internal/admin/account")
public class InternalAdminAccountController {

    /**
     * 用户账户服务。
     */
    private final UserAccountService userAccountService;

    /**
     * 创建内部后台用户账户管理接口。
     *
     * @param userAccountService 用户账户服务
     */
    public InternalAdminAccountController(
            UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    /**
     * 分页查询用户账户。
     *
     * @param request 用户账户分页查询条件
     * @return 用户账户分页数据
     */
    @Operation(summary = "分页查询用户账户", description = "根据分页条件和筛选条件查询用户账户列表。")
    @PostMapping("/page")
    public Result<PageResponse<AdminUserAccountResponse>> accountPage(
            @Parameter(description = "用户账户分页查询参数", required = true)
            @Valid @RequestBody AdminUserAccountPageRequest request) {

        return Result.success(
                userAccountService.adminAccountPage(request)
        );
    }

    /**
     * 分页查询账户流水。
     *
     * @param request 账户流水分页查询条件
     * @return 账户流水分页数据
     */
    @Operation(summary = "分页查询账户流水", description = "根据分页条件和筛选条件查询账户流水列表。")
    @PostMapping("/transaction/page")
    public Result<PageResponse<AdminAccountTransactionResponse>> transactionPage(
            @Parameter(description = "账户流水分页查询参数", required = true)
            @Valid @RequestBody AdminAccountTransactionPageRequest request) {

        return Result.success(
                userAccountService.adminTransactionPage(request)
        );
    }

    /**
     * 增加账户余额。
     *
     * @param request 账户余额增加参数
     * @return 空结果
     */
    @Operation(summary = "增加账户余额", description = "为指定用户账户增加余额。")
    @PostMapping("/addbalance")
    public Result<Void> addBalance(
            @Parameter(description = "账户余额增加参数", required = true)
            @Valid @RequestBody AdminAccountBalanceChangeRequest request) {

        userAccountService.adminAddBalance(request);

        return Result.success();
    }

    /**
     * 扣减账户余额。
     *
     * @param request 账户余额扣减参数
     * @return 空结果
     */
    @Operation(summary = "扣减账户余额", description = "从指定用户账户扣减余额。")
    @PostMapping("/subbalance")
    public Result<Void> subBalance(
            @Parameter(description = "账户余额扣减参数", required = true)
            @Valid @RequestBody AdminAccountBalanceChangeRequest request) {

        userAccountService.adminSubBalance(request);

        return Result.success();
    }

    /**
     * 更新账户状态。
     *
     * @param request 账户状态更新参数
     * @return 空结果
     */
    @Operation(summary = "更新账户状态", description = "更新指定用户账户的状态。")
    @PostMapping("/updatestatus")
    public Result<Void> updateStatus(
            @Parameter(description = "账户状态更新参数", required = true)
            @Valid @RequestBody AdminAccountStatusUpdateRequest request) {

        userAccountService.adminUpdateStatus(request);

        return Result.success();
    }
}

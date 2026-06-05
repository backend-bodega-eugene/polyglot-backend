package com.eugene.goalhub.user.controller;

import com.eugene.goalhub.user.service.UserAccountService;
import dto.AccountTransactionPageRequest;
import dto.AccountTransactionResponse;
import dto.PageResponse;
import dto.UserAccountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import response.Result;

import java.util.List;

/**
 * 用户账户接口。
 *
 * <p>提供前端用户查询本人账户列表和账户流水的接口。</p>
 */
@Tag(name = "用户账户", description = "用户账户相关接口")
@RestController
@RequestMapping("/user/account")
public class UserAccountController {

    /**
     * 用户账户服务。
     */
    private final UserAccountService userAccountService;

    /**
     * 创建用户账户接口。
     *
     * @param userAccountService 用户账户服务
     */
    public UserAccountController(
            UserAccountService userAccountService) {

        this.userAccountService = userAccountService;
    }

    /**
     * 查询我的账户。
     *
     * @param userId 当前登录用户 ID
     * @return 当前用户账户列表
     */
    @Operation(summary = "查询我的账户", description = "查询当前登录用户的账户列表。")
    @GetMapping("/me")
    public Result<List<UserAccountResponse>> getMyAccounts(
            @Parameter(description = "当前登录用户 ID", required = true)
            @RequestHeader("X-User-Id") Long userId) {

        return Result.success(
                userAccountService.getMyAccounts(userId)
        );
    }

    /**
     * 查询我的流水。
     *
     * @param userId  当前登录用户 ID
     * @param request 账户流水分页查询条件
     * @return 当前用户账户流水分页数据
     */
    @Operation(summary = "查询我的流水", description = "分页查询当前登录用户的账户流水。")
    @PostMapping("/me/transactions")
    public Result<PageResponse<AccountTransactionResponse>>
    pageMyTransactions(
            @Parameter(description = "当前登录用户 ID", required = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "账户流水分页查询条件", required = true)
            @Valid @RequestBody AccountTransactionPageRequest request) {

        return Result.success(
                userAccountService.pageMyTransactions(
                        userId,
                        request
                )
        );
    }
}

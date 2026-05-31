package com.eugene.goalhub.user.controller;

import com.eugene.goalhub.user.service.UserAccountService;
import dto.AccountTransactionPageRequest;
import dto.AccountTransactionResponse;
import dto.PageResponse;
import dto.UserAccountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import response.Result;

import java.util.List;

/**
 * 用户账户接口。
 */
@Tag(name = "用户账户", description = "用户账户相关接口")
@RestController
@RequestMapping("/account")
public class UserAccountController {

    private final UserAccountService userAccountService;

    public UserAccountController(
            UserAccountService userAccountService) {

        this.userAccountService = userAccountService;
    }

    /**
     * 查询我的账户。
     */
    @Operation(summary = "查询我的账户")
    @GetMapping("/me")
    public Result<List<UserAccountResponse>> getMyAccounts(
            @RequestHeader("X-User-Id") Long userId) {

        return Result.success(
                userAccountService.getMyAccounts(userId)
        );
    }

    /**
     * 查询我的流水。
     */
    @Operation(summary = "查询我的流水")
    @PostMapping("/me/transactions")
    public Result<PageResponse<AccountTransactionResponse>>
    pageMyTransactions(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody AccountTransactionPageRequest request) {

        return Result.success(
                userAccountService.pageMyTransactions(
                        userId,
                        request
                )
        );
    }
}
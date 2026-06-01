//package com.eugene.goalhub.order.controller;
//
//
//import com.eugene.goalhub.order.service.OrderUserAccountService;
//import dto.*;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import response.Result;
//
///**
// * 后台用户账户管理接口。
// */
//@Tag(name = "后台用户账户管理", description = "后台用户账户、账户流水和余额管理接口")
//@RestController
//@RequestMapping("/admin/account")
//public class OrderUserAccountController {
//
//    private final OrderUserAccountService
//            adminUserAccountService;
//
//    public OrderUserAccountController(
//            OrderUserAccountService adminUserAccountService) {
//
//        this.adminUserAccountService =
//                adminUserAccountService;
//    }
//
//    /**
//     * 增加用户账户余额。
//     *
//     * @param request 账户余额增加参数
//     * @return 空结果
//     */
//    @Operation(summary = "增加账户余额", description = "为指定用户账户增加余额。")
//    @PostMapping("/addbalance")
//    public Result<Void> addBalance(
//            @Parameter(description = "账户余额增加参数", required = true)
//            @RequestBody AdminAccountBalanceChangeRequest request) {
//
//        adminUserAccountService.addBalance(request);
//
//        return Result.success();
//    }
//
//    /**
//     * 扣减用户账户余额。
//     *
//     * @param request 账户余额扣减参数
//     * @return 空结果
//     */
//    @Operation(summary = "扣减账户余额", description = "从指定用户账户扣减余额。")
//    @PostMapping("/subbalance")
//    public Result<Void> subBalance(
//            @Parameter(description = "账户余额扣减参数", required = true)
//            @RequestBody AdminAccountBalanceChangeRequest request) {
//
//        adminUserAccountService.subBalance(request);
//
//        return Result.success();
//    }
//
//}

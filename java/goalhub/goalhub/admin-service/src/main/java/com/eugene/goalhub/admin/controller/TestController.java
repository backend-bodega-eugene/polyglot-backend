//package com.eugene.goalhub.admin.controller;
//
//import com.eugene.goalhub.boot.logs.service.GoalhubLogService;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/test")
//public class TestController {
//
//    private final GoalhubLogService logService;
//
//    public TestController(
//            GoalhubLogService logService
//    ) {
//        this.logService = logService;
//    }
//
//    @GetMapping("/log")
//    public String log() {
//
//        logService.bizLog(
//                "test",
//                "create",
//                1L,
//                "admin",
//                "测试日志"
//        );
//
//        return "ok";
//    }
//}
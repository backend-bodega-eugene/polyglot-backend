package com.main.controller;

import com.main.controller.base.BaseController;
import com.main.service.DealHashCallbackService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


import java.util.List;

@Slf4j
@Api(value = "交易哈希回调", tags = {"交易哈希回调接口"})
@RestController
@RequestMapping(value = "/main/DealHashCallback", method = {RequestMethod.POST})
public class DealHashCallbackController extends BaseController {
    @Autowired
    DealHashCallbackService dealHashCallbackService;

    @Operation(summary = "回调接口")
    @RequestMapping("/invokedealhash")
    public void InvokeDealHash() {

    }
}

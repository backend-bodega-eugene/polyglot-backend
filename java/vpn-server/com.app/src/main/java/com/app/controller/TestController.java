package com.app.controller;

import com.app.controller.dto.responseVO.PayInfoVO;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(value = "测试", tags = {"这是瞎写的 ,特使"})
@RestController
@RequestMapping(value = "/test", method = {RequestMethod.POST})
public class TestController {
    @Operation(summary = "随便乱写的测试接口")
    @RequestMapping("/test")
    public String GetallPayTypeResponseVOS() {

        return "随便乱写的 ";
    }

}

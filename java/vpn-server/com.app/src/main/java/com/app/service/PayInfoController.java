//package com.app.controller;
//
//import com.main.Entity.PayInfo;
//import com.main.manage.requestbodyvo.PayInfoRequestVO;
//import com.main.manage.responseVO.PageResultResponseVO;
//import com.main.service.PayInfoService;
//import io.swagger.annotations.Api;
//import io.swagger.v3.oas.annotations.Operation;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
//@Slf4j
//@Api(value = "支付通道", tags = {"支付通道查询 修改,删除,增加"})
//@RestController
//@RequestMapping(value = "/app/payinfo", method = {RequestMethod.POST})
//public class PayInfoController {
//    @Autowired
//    PayInfoService payInfoService;
//    @Operation(summary = "查询所有的支付方式下的所有支付通道")
//    @RequestMapping("/query")
//    public PageResultResponseVO<List<PayInfo>> GetallPayTypeResponseVOS(@RequestBody PayInfoRequestVO param){
//        return payInfoService.GetallPayTypeResponseVOS(param.getPageIndex(),param.getPageSize(),param.getPayTypeId(),param.getPayInfoName());
//    }
//    @Operation(summary = "添加")
//    @RequestMapping("/add")
//    public Boolean add(PayInfo vo){
//        return payInfoService.save(vo);
//    }
//    @Operation(summary = "修改")
//    @RequestMapping("/edit")
//    public Boolean edit(PayInfo vo){
//        return payInfoService.updateById(vo);
//    }
//    @Operation(summary = "删除")
//    @RequestMapping("/delete")
//    public Boolean delete(PayInfo vo){
//        return payInfoService.removeById(vo);
//    }
//}

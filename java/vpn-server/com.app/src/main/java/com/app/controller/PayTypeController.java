//package com.app.controller;
//
//import com.app.Entity.PayInfo;
//import com.app.Entity.PayType;
//import com.app.controller.dto.responseVO.PayInfosResonseVO;
//import com.app.controller.dto.responseVO.PayTypeResponseVO;
//import com.app.service.PayTypeService;
//import io.swagger.annotations.Api;
//import io.swagger.v3.oas.annotations.Operation;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
//@Slf4j
//@Api(value = "支付类型", tags = {"支付类型查询"})
//@RestController
//@RequestMapping(value = "/app/paytype", method = {RequestMethod.POST})
//public class PayTypeController {
//
//
//    @Autowired
//    PayTypeService payTypeService;
//    @Operation(summary = "查询所有的支付方式下的所有支付通道")
//    @RequestMapping("/query")
//    public List<PayInfosResonseVO> GetallPayTypeResponseVOS(){
//        return payTypeService.GetallPayTypeResponseVOS();
//    }
//
////    @Operation(summary = "查询所有的支付方式下的所有支付通道")
////    @RequestMapping("/query")
////    public List<PayInfo> GetallPayTypeResponseVOS(){
////        return payTypeService.GetallPayTypeResponseVOS();
////    }
////    @Operation(summary = "添加")
////    @RequestMapping("/add")
////    public Boolean add(PayType vo){
////        return payTypeService.save(vo);
////    }
////    @Operation(summary = "修改")
////    @RequestMapping("/edit")
////    public Boolean edit(PayType vo){
////        return payTypeService.updateById(vo);
////    }
////    @Operation(summary = "删除")
////    @RequestMapping("/delete")
////    public Boolean delete(PayType vo){
////        return payTypeService.removeById(vo);
////    }
////    @Operation(summary = "所有查询")
////    @RequestMapping("/delete")
////    public List<PayType> getList(){
////        return payTypeService.getallList();
////    }
//}

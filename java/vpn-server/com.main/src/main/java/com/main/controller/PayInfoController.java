package com.main.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.exception.BizException;
import com.main.Entity.PayInfo;
import com.main.Entity.PayType;
import com.main.manage.requestbodyvo.OderQuestParam;
import com.main.manage.requestbodyvo.PayInfoRequestVO;
import com.main.manage.responseVO.PageResultResponseVO;
import com.main.manage.responseVO.PayTypeResponseVO;
import com.main.service.PayInfoService;
import com.main.service.PayTypeService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Api(value = "支付通道", tags = {"支付通道查询 修改,删除,增加"})
@RestController
@RequestMapping(value = "/backend/payinfo", method = {RequestMethod.POST})
public class PayInfoController {
    @Autowired
    PayInfoService payInfoService;

    @Operation(summary = "查询所有的支付方式下的所有支付通道")
    @RequestMapping("/query")
    public PageResultResponseVO<List<PayInfo>> GetallPayTypeResponseVOS(@RequestBody PayInfoRequestVO param) {
        return payInfoService.GetallPayTypeResponseVOS(
                param.getPageIndex(),
                param.getPageSize(),
                param.getTypeId(),
                param.getPayName(),
                param.getStatus());
    }

    @Operation(summary = "添加")
    @RequestMapping("/add")
    public Boolean add(@RequestBody PayInfo vo) {
        LambdaQueryWrapper<PayInfo> wrapper = Wrappers.lambdaQuery(PayInfo.class)
                .eq(PayInfo::getTypeId, vo.getTypeId());
        Long count = payInfoService.count(wrapper);
        if (count > 0) {
            throw new BizException(1, "每个支付类型只能有一个通道");
        }
        return payInfoService.save(vo);
    }

    @Operation(summary = "修改")
    @RequestMapping("/edit")
    public Boolean edit(@RequestBody PayInfo vo) {
        return payInfoService.updateById(vo);
    }

    @Operation(summary = "删除")
    @RequestMapping("/delete")
    public Boolean delete(@RequestBody PayInfo vo) {
        return payInfoService.removeById(vo);
    }
}
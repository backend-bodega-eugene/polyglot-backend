package com.app.service;

import com.app.controller.dto.responseVO.PayInfosResonseVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.app.Entity.PayType;
import com.app.dao.PayTypeMapper;
import com.app.controller.dto.responseVO.PayInfoResponseVO;
import com.app.controller.dto.responseVO.PayTypeResponseVO;
import com.common.util.BeanCoper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PayTypeService extends ServiceImpl<PayTypeMapper, PayType> implements IService<PayType> {
}

package com.main.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.main.Entity.Recharge;
import com.main.Entity.SystemConfig;
import com.main.dao.RechargeMapper;
import com.main.dao.SystemConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RechargeService extends ServiceImpl<RechargeMapper, Recharge> {
}

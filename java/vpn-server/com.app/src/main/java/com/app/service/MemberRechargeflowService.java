package com.app.service;

import com.app.Entity.MemberRechargeflow;
import com.app.dao.MemberRechargeflowMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MemberRechargeflowService extends ServiceImpl<MemberRechargeflowMapper, MemberRechargeflow> {

    public boolean AddMemberRechargeflow(MemberRechargeflow flow) {

        return this.save(flow);
    }
}

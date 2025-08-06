package com.app.service;

import com.app.Entity.VpnLinestatus;
import com.app.dao.VpnLinestatusmapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class VpnLinestatusService extends ServiceImpl<VpnLinestatusmapper, VpnLinestatus> {

    public Integer Speedstatus(long memId, String memName, int status, String ipaddress) {

        if (memId <= 0) {
            throw new BizException(1, "会员id非法");
        }
        LambdaQueryWrapper<VpnLinestatus> queryWrapper = Wrappers.lambdaQuery(VpnLinestatus.class)
                .eq(VpnLinestatus::getMemId, memId)
                .select(VpnLinestatus::getMemId);
        List<VpnLinestatus> lst = this.list(queryWrapper);
        if (lst.size() > 0) {
            //更新状态
            LambdaUpdateWrapper<VpnLinestatus> updateWrap = Wrappers.lambdaUpdate(VpnLinestatus.class)
                    .set(VpnLinestatus::getOnlineStatus, status)
                    .set(VpnLinestatus::getLastUpdatetime, LocalDateTime.now())
                    .set(VpnLinestatus::getOnlineIpaddress, ipaddress)
                    .eq(VpnLinestatus::getMemId, memId);
            boolean isSuccess = this.update(updateWrap);
            if (false == isSuccess) {
                throw new BizException(1, "状态更改失败");
            }
            return 0;
        } else {
            //增加数据
            VpnLinestatus vpnLinestatus = new VpnLinestatus();
            vpnLinestatus.setMemId(memId);
            vpnLinestatus.setMemName(memName);
            vpnLinestatus.setOnlineIpaddress(ipaddress);
            vpnLinestatus.setOnlineStatus(status);
            vpnLinestatus.setLastUpdatetime(LocalDateTime.now());
            boolean isSuccess = this.save(vpnLinestatus);
            if (false == isSuccess) {
                throw new BizException(1, "状态更改失败");
            }
            return 0;
            //修改状态
        }
    }
}

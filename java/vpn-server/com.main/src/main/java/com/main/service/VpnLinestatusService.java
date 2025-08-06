package com.main.service;

import com.main.Entity.VpnLinestatus;
import com.main.Entity.VpnMember;
import com.main.dao.VpnLinestatusmapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.exception.BizException;
import com.main.manage.responseVO.VpnMemberVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VpnLinestatusService extends ServiceImpl<VpnLinestatusmapper, VpnLinestatus> {

    public String UserInfoStatus(Long memid) {

        LambdaQueryWrapper<VpnLinestatus> wrapper = Wrappers.lambdaQuery(VpnLinestatus.class)
                .eq(VpnLinestatus::getMemId, memid)
                .orderByDesc(VpnLinestatus::getLastUpdatetime);
        VpnLinestatus status = this.getOne(wrapper);
        if (status.getOnlineStatus() == 1) {
            return "离线";
        }
        return "在线";
    }

    public void getAllUserStatus(List<VpnMemberVO> lst) {


        LambdaQueryWrapper<VpnLinestatus> wrapper = Wrappers.lambdaQuery(VpnLinestatus.class)
                .in(VpnLinestatus::getMemId, lst.stream().map(item -> item.getId()).collect(Collectors.toList()));
        var userOnline = this.list();
        lst.forEach(vpnMember -> {
            for (int i = userOnline.size() - 1; i >= 0; i--) {
                if (vpnMember.getId() == userOnline.get(i).getMemId()) {
                    if (userOnline.get(i).getOnlineStatus() == 1) {
                        vpnMember.setUserOnlineStatus("在线");
                    } else {
                        vpnMember.setUserOnlineStatus("离线");
                    }
                    userOnline.remove(i);
                }
            }
        });
    }
}

package com.app.service;

import com.app.Entity.*;
import com.app.controller.dto.VpnLineResponseParam;
import com.app.controller.dto.VpnVO;
import com.app.controller.dto.VpnVmessVO;
import com.app.dao.VpnLineMapper;
import com.app.manage.ReturnCard;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.exception.BizException;
import com.common.util.BeanCoper;
import com.xray.NodeManager;
import com.xray.app.proxyman.command.AlterInboundResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class VpnLineLineService extends ServiceImpl<VpnLineMapper, VpnLine> {

    @Autowired
    VpnNationService vpnNationService;
    @Autowired
    AppMemberService appMemberService;

    @Autowired
    LineUserinfoService lineUserinfoService;

    public VpnVmessVO connectNode(Long id, Long memid) {
        //连接节点
        //判断用户是否会员有效
        VpnMember vpnMember = appMemberService.getById(memid);
        if (vpnMember.getExpirTime().isBefore(LocalDateTime.now())) {
            throw new BizException(1, "会员已经过期,请购买会员套餐");
        }
//        //查询用户在节点有效用户
//        LambdaQueryWrapper<LineUserinfo> wrapper = Wrappers.lambdaQuery(LineUserinfo.class)
//                .eq(LineUserinfo::getLineId,id)
//                .eq(LineUserinfo::getMemId,memid);
//        LineUserinfo luserinfo = lineUserinfoService.getOne(wrapper);
//        if(luserinfo==null){
//            luserinfo=new LineUserinfo();
//            luserinfo.setLineId(id);
//            luserinfo.setMemId(memid);
//            lineUserinfoService.save(luserinfo);
//        }
//
//        //生成用户账户
//        String password=ReturnCard.GetSingleton().VmessAESEncrypt(
//                vpnMember.getUid()+"_ligthsppeed_"+vpnMember.getDeviceId());
//        AlterInboundResponse response= NodeManager.getNodeManager().userAdd(vpnMember.getMemName(),password);

        // var linInfo = BeanCoper.clone(VpnVmessVO.class, this.getById(id));
        //  String vmessStr=linInfo.getVmess();
        //查询生成的数据
        //根据数据生成账户
        //生成密码
        //替换字符串给出到前端
        return BeanCoper.clone(VpnVmessVO.class, this.getById(id));
    }

    public List<VpnLineResponseParam> LineQuery(Long memid) {
        LambdaQueryWrapper<VpnNation> wrapper = Wrappers.lambdaQuery(VpnNation.class)
                .eq(VpnNation::getNationStatus, 0)
                .orderByDesc(VpnNation::getSort);
        List<VpnNation> lstNation = vpnNationService.list(wrapper);
        LambdaQueryWrapper<VpnLine> wrapperlines = Wrappers.lambdaQuery(VpnLine.class)
                .eq(VpnLine::getLineStatus, 0)
                .orderByDesc(VpnLine::getSort);
        List<VpnLine> lstLine = this.list(wrapperlines);
        if (lstLine.size() > 0) {
            List<VpnLineResponseParam> lstParam = new ArrayList<>();
            for (int i = lstNation.size() - 1; i >= 0; i--) {
                VpnLineResponseParam param = new VpnLineResponseParam();
                param.setNationName(lstNation.get(i).getNationName());
                param.setNationId(lstNation.get(i).getId());
                List<VpnVO> current = new ArrayList<>();
                for (int j = lstLine.size() - 1; j >= 0; j--) {
                    if (lstLine.get(j).getLineNationid() == lstNation.get(i).getId()) {
                        //在这里加密vmess字段
                        lstLine.get(j).setVmess(ReturnCard.GetSingleton().LineAESEncrypt(lstLine.get(j).getVmess()));//节点信息加密
                        current.add(BeanCoper.clone(VpnVO.class, lstLine.get(j)));
                        lstLine.remove(j);
                    }
                }
                param.setNationVpnLine(current);
                lstParam.add(param);
            }
            for (int m = lstParam.size() - 1; m > 0; m--) {
                if (lstParam.get(m).getNationVpnLine() == null || lstParam.get(m).getNationVpnLine().size() <= 0) {
                    lstParam.remove(m);
                }
            }
            return lstParam;
        } else {

            throw new BizException(1, "没有数据");
        }
    }
}
package com.main.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.exception.BizException;
import com.common.util.StringUtils;
import com.main.Entity.VpnLine;
import com.main.dao.VpnLineMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.main.manage.requestbodyvo.NodeParamVO;
import com.main.manage.requestbodyvo.PageResultRequestVOByLine;
import com.main.manage.responseVO.PageResultResponseVO;
import com.main.manage.requestbodyvo.VpnLineRequestVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class VpnLineLineService extends ServiceImpl<VpnLineMapper, VpnLine> implements IService<VpnLine> {

    @Autowired
    VpnNationService vpnNationService;

    public PageResultResponseVO<List<VpnLine>> LineQuery(PageResultRequestVOByLine query) {

        PageResultResponseVO<List<VpnLine>> response = new PageResultResponseVO();
        LambdaQueryWrapper<VpnLine> wrapper = Wrappers.lambdaQuery(VpnLine.class)
                .like(StringUtils.isNotBlank(query.getNodeName()), VpnLine::getLineName, query.getNodeName())
                .eq(query.getLineStatus() > -1, VpnLine::getLineStatus, query.getLineStatus())
                .orderByDesc(VpnLine::getSort);
        Page pagination = new Page(query.getPageIndex(), query.getPageSize());
        Page<VpnLine> page = super.page(pagination, wrapper);
        List<VpnLine> list = page.getRecords();
        response.setPageIndex(page.getCurrent());
        response.setPageSize(page.getSize());
        response.setTotal(page.getTotal());
        response.setData(list);
        return response;
    }

    public Integer addLine(VpnLineRequestVO request) {
        VpnLine vpnLine = new VpnLine();
        vpnLine.setLineName(request.getNodeName());
        vpnLine.setLineNationid(request.getLineNationid());
        vpnLine.setLineIcon(request.getLineIcon());
        vpnLine.setLineStatus(0);
        vpnLine.setSort(request.getLineSort());
        vpnLine.setVmess(request.getVmess());
        //以下是预留的属性
        boolean isSuccess = this.save(vpnLine);
        if (false == isSuccess) {
            throw new BizException(1, "添加节点失败");
        } else {
            return 0;
        }
    }

    public Integer addLineByThirdparty(NodeParamVO request) {

        VpnLine vpnLine = new VpnLine();
        vpnLine.setLineName(request.getName());
        vpnLine.setLineNationid(9L);
        vpnLine.setLineIcon("");
        vpnLine.setLineStatus(0);
        vpnLine.setSort(0);
        vpnLine.setHost(request.getIp());
        vpnLine.setPort(request.getIp());
        boolean isSuccess = this.save(vpnLine);
        if (false == isSuccess) {
            throw new BizException(1, "添加节点失败");
        } else {
            return 0;
        }
    }

    public Integer EditLine(VpnLineRequestVO request) {
        LambdaUpdateWrapper<VpnLine> wrapper = Wrappers.lambdaUpdate(VpnLine.class)
                .set(StringUtils.isNotBlank(request.getNodeName()), VpnLine::getLineName, request.getNodeName())
                .set(request.getLineNationid() > -1, VpnLine::getLineNationid, request.getLineNationid())
                .set(request.getLineSort() > -1, VpnLine::getSort, request.getLineSort())
                .set(StringUtils.isNotBlank(request.getVmess()), VpnLine::getVmess, request.getVmess())
                .eq(VpnLine::getLineId, request.getLineId());
        boolean isSuccess = this.update(wrapper);
        if (false == isSuccess) {
            throw new BizException(1, "更新节点失败");
        } else {
            return 0;
        }
    }

    public Integer EditLineStatus(long lineId, Integer status) {
        LambdaUpdateWrapper<VpnLine> wrapper = Wrappers.lambdaUpdate(VpnLine.class)
                .set(VpnLine::getLineStatus, status)
                .eq(VpnLine::getLineId, lineId);
        boolean isSuccess = this.update(wrapper);
        if (false == isSuccess) {
            throw new BizException(1, "更新节点状态失败");
        } else {
            return 0;
        }
    }

    public Integer DeleteLine(long lineId) {
        LambdaQueryWrapper<VpnLine> wrapper = Wrappers.lambdaQuery(VpnLine.class)
                .eq(VpnLine::getLineId, lineId);
        boolean isSuccess = this.remove(wrapper);
        if (false == isSuccess) {
            throw new BizException(1, "删除节点失败");
        } else {
            return 0;
        }

    }
}

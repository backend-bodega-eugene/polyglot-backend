package com.main.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.exception.BizException;
import com.common.result.Result;
import com.common.util.StringUtils;
import com.main.Entity.ClientUpgrade;
import com.main.dao.ClientUpgradeMapper;
import com.main.manage.responseVO.PageResultResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ClientUpgradeService extends ServiceImpl<ClientUpgradeMapper, ClientUpgrade> implements IService<ClientUpgrade> {
    public PageResultResponseVO<List<ClientUpgrade>> ClientUpgradeServiceQuery(String versionName,
                                                                               String client,
                                                                               Integer status,
                                                                               long pageIndex,
                                                                               long pageSize) {
        if (status == null) {
            status = -1;
        }
        PageResultResponseVO<List<ClientUpgrade>> response = new PageResultResponseVO<>();
        LambdaQueryWrapper<ClientUpgrade> wrapper = new LambdaQueryWrapper<>(ClientUpgrade.class)
                .like(StringUtils.isNotBlank(versionName), ClientUpgrade::getVersionName, versionName)
                .eq(StringUtils.isNotBlank(client), ClientUpgrade::getClientName, client)
                .eq(status > -1, ClientUpgrade::getStatus, status);

        Page pagination = new Page(pageIndex, pageSize);
        Page<ClientUpgrade> page = super.page(pagination, wrapper);
        List<ClientUpgrade> list = page.getRecords();
        response.setPageIndex(page.getCurrent());
        response.setPageSize(page.getSize());
        response.setTotal(page.getTotal());
        response.setData(list);
        return response;
    }

    public Integer AddClientUpgrade(ClientUpgrade request) {
        if (!StringUtils.isNotBlank(request.getVersionName())) {

            throw new BizException(1, "版本名称不能为空");
        }
        if (!StringUtils.isNotBlank(request.getClientName())) {

            throw new BizException(1, "客户端名称不能为空");
        }
        if (!StringUtils.isNotBlank(request.getVersionNumber())) {

            throw new BizException(1, "版本号不能为空");
        }
        request.setVersionType(request.getClientName());
        boolean isSuccess = this.save(request);
        if (false == isSuccess) {
            throw new BizException(1, "添加失败");
        } else {
            return 0;
        }

    }

    public Integer EditClientUpgrade(ClientUpgrade request) {
        if (!StringUtils.isNotBlank(request.getVersionName())) {

            throw new BizException(1, "版本名称不能为空");
        }
        if (!StringUtils.isNotBlank(request.getClientName())) {

            throw new BizException(1, "客户端名称不能为空");
        }
        if (!StringUtils.isNotBlank(request.getVersionNumber())) {

            throw new BizException(1, "版本号不能为空");
        }
        if (null == request.getForceUpdate()) {

            throw new BizException(1, "是否强制更新字段 不能为空 值:0,1");
        }
        if (null == request.getId()) {

            throw new BizException(1, "id不能为空");
        }
        LambdaUpdateWrapper<ClientUpgrade> wrapper = Wrappers.lambdaUpdate(ClientUpgrade.class)
                .set(StringUtils.isNotBlank(request.getVersionName()), ClientUpgrade::getVersionName, request.getVersionName())
                .set(StringUtils.isNotBlank(request.getClientName()), ClientUpgrade::getClientName, request.getClientName())
                .set(StringUtils.isNotBlank(request.getVersionNumber()), ClientUpgrade::getVersionNumber, request.getVersionNumber())
                .set(StringUtils.isNotBlank(request.getDownloadAddress()), ClientUpgrade::getDownloadAddress, request.getDownloadAddress())
                .set(request.getForceUpdate() > -1, ClientUpgrade::getForceUpdate, request.getForceUpdate())
                .set(request.getForceUpdate() > -1, ClientUpgrade::getVersionType, request.getClientName())
                .set(StringUtils.isNotBlank(request.getRemark()), ClientUpgrade::getRemark, request.getRemark())
                .eq(ClientUpgrade::getId, request.getId());
        boolean isSuccess = this.update(wrapper);
        if (false == isSuccess) {
            throw new BizException(1, "修改失败");
        } else {
            return 0;
        }
    }

    public Integer EditClientUpgradeStatus(long applicationId, int status) {
        if (applicationId <= 0) {

            throw new BizException(1, "id不正确");
        }
        if (status < 0) {

            throw new BizException(1, "状态不正确 值:0,1");
        }
        LambdaUpdateWrapper<ClientUpgrade> wrapper = Wrappers.lambdaUpdate(ClientUpgrade.class)
                .set(ClientUpgrade::getStatus, status)
                .eq(ClientUpgrade::getId, applicationId);
        boolean isSuccess = this.update(wrapper);
        if (false == isSuccess) {
            throw new BizException(1, "更新状态失败");
        } else {
            return 0;
        }

    }

    public Integer DeleteClientUpgrade(long applicationId) {
        if (applicationId <= 0) {

            throw new BizException(1, "id不正确");
        }
        Result<Integer> result = new Result<>();
        LambdaUpdateWrapper<ClientUpgrade> wrapper = Wrappers.lambdaUpdate(ClientUpgrade.class)
                .eq(ClientUpgrade::getId, applicationId);
        boolean isSuccess = this.remove(wrapper);
        if (false == isSuccess) {
            throw new BizException(1, "删除失败");
        } else {
            return 0;
        }
    }
}

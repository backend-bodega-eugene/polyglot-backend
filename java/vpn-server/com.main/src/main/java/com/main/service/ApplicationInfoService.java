package com.main.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.common.exception.BizException;
import com.main.Entity.ApplicationInfo;
import com.main.dao.ApplicationInfoMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.result.Result;
import com.main.manage.requestbodyvo.ApplicationInfoRequestVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ApplicationInfoService extends ServiceImpl<ApplicationInfoMapper, ApplicationInfo> implements IService<ApplicationInfo> {

    public List<ApplicationInfo> ApplicationInfoServiceQuery() {

        LambdaQueryWrapper<ApplicationInfo> wrapper = new LambdaQueryWrapper<>(ApplicationInfo.class);
        return this.list(wrapper);
    }

    public Integer AddApplication(ApplicationInfoRequestVO request) {
        if (!StringUtils.isNotBlank(request.getApplicationName())) {

            throw new BizException(1, "客户端名称为空");
        }
        if (!StringUtils.isNotBlank(request.getApplicationAddress())) {

            throw new BizException(1, "地址不能为空");
        }
        if (null == request.getApplicationStatus()) {

            throw new BizException(1, "状态不能为空");
        }
        ApplicationInfo info = new ApplicationInfo();
        info.setApplicationIcon(request.getApplicationIcon());
        info.setApplicationName(request.getApplicationName());
        info.setApplicationAddress(request.getApplicationAddress());
        info.setApplicationStatus(request.getApplicationStatus());
        //下面是其他属性
        info.setApplicationUrl("");
        info.setRemark("备注");
        info.setCreateTime(LocalDateTime.now());
        info.setApplicationVersion("10.0.0");
        info.setApplicationForceupdate(0);
        info.setApplicationStatus(0);
        info.setApplicationType(0);
        boolean isSuccess = this.save(info);
        if (false == isSuccess) {
            throw new BizException(1, "添加失败");
        } else {
            return 0;
        }
    }

    public Integer EditApplication(ApplicationInfoRequestVO request) {
        if (!StringUtils.isNotBlank(request.getApplicationName())) {

            throw new BizException(1, "客户端名称为空");
        }
        if (!StringUtils.isNotBlank(request.getApplicationAddress())) {

            throw new BizException(1, "地址不能为空");
        }
        if (null == request.getApplicationStatus()) {

            throw new BizException(1, "状态不能为空");
        }
        if (request.getId() <= 0) {

            throw new BizException(1, "id不正确");
        }
        LambdaUpdateWrapper<ApplicationInfo> wrapper = Wrappers.lambdaUpdate(ApplicationInfo.class)
                .set(StringUtils.isNotBlank(request.getApplicationIcon()), ApplicationInfo::getApplicationIcon, request.getApplicationIcon())
                .set(StringUtils.isNotBlank(request.getApplicationName()), ApplicationInfo::getApplicationName, request.getApplicationName())
                .set(StringUtils.isNotBlank(request.getApplicationAddress()), ApplicationInfo::getApplicationAddress, request.getApplicationAddress())
                .set(request.getApplicationStatus() > -1, ApplicationInfo::getApplicationStatus, request.getApplicationStatus())
                .eq(ApplicationInfo::getId, request.getId());
        boolean isSuccess = this.update(wrapper);
        if (false == isSuccess) {
            throw new BizException(1, "修改失败");

        } else {
            return 0;
        }
    }

    public Integer EditApplicationStatus(long applicationId, int status) {
        if (applicationId < 0) {

            throw new BizException(1, "客户端id不正确");
        }
        if (status < 0 || status > 1) {

            throw new BizException(1, "状态只能为0和1");
        }
        Result<Integer> result = new Result<>();
        LambdaUpdateWrapper<ApplicationInfo> wrapper = Wrappers.lambdaUpdate(ApplicationInfo.class)
                .set(ApplicationInfo::getApplicationStatus, status)
                .eq(ApplicationInfo::getId, applicationId);
        boolean isSuccess = this.update(wrapper);
        if (false == isSuccess) {
            throw new BizException(1, "修改状态失败");

        } else {
            return 0;
        }

    }

    public Integer DeleteApplication(long applicationId) {
        if (applicationId <= 0) {

            throw new BizException(1, "客户端id不正确");
        }
        LambdaUpdateWrapper<ApplicationInfo> wrapper = Wrappers.lambdaUpdate(ApplicationInfo.class)
                .eq(ApplicationInfo::getId, applicationId);
        boolean isSuccess = this.remove(wrapper);
        if (false == isSuccess) {
            throw new BizException(1, "删除失败");

        } else {
            return 0;
        }

    }
}

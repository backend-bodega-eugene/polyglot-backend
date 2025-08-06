package com.main.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.cookie.LoginContext;
import com.common.exception.BizException;
import com.main.Entity.SystemConfig;
import com.main.Entity.VpnAdmin;
import com.main.config.PubMsgTask;
import com.main.dao.VpnAdminMapper;
import com.main.manage.AdminLoginInfo;
import com.main.manage.ReturnCard;
import com.main.manage.requestbodyvo.MemberStatusVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @author langy
 */
@Slf4j
@Service
public class AdminService extends ServiceImpl<VpnAdminMapper, VpnAdmin> implements IService<VpnAdmin> {

    @Autowired
    SystemConfigService systemConfigService;

    public VpnAdmin AdminLogin(String adminName, String adminPassword, String vCode, String ip) {
        //检查ip是否合法
        try {
            SystemConfig configip = systemConfigService.GetSystemConfig("SecuritySettings");
            String securitysettings = configip.getSystemValue();
            if (StringUtils.isNotBlank(securitysettings)) {
                String[] ips = securitysettings.split(",");
                boolean iscontain = false;
                if (ips != null || ips.length > 0) {
                    if (ips.length <= 1 && !com.common.util.StringUtils.isNotBlank(ips[0])) {
                        iscontain = true;
                    } else {
                        for (String temp : ips) {
                            if (StringUtils.equals(temp, ip)) {
                                iscontain = true;
                                break;
                            }
                        }
                    }
                    if (iscontain == false) {
                        throw new BizException(1, "安全设置不正确");
                    }
                }
            }
        } catch (Exception e) {
            throw new BizException(1, e.getMessage());
        }
        if (!StringUtils.isNotBlank(adminName)) {
            throw new BizException(1, "用户名为空");
        }
        if (!StringUtils.isNotBlank(adminPassword)) {
            throw new BizException(1, "密码为空");
        }
        if (!StringUtils.isNotBlank(vCode)) {
            throw new BizException(1, "验证码为空");
        }
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        HttpSession session = request.getSession();
        Object sessionObject = session.getAttribute("verifyCode");
        if (null == sessionObject) {
            throw new BizException(1, "验证码并未生成");
        } else {
            String code = session.getAttribute("verifyCode").toString();
            if (code.equalsIgnoreCase(vCode)) {
                LambdaQueryWrapper<VpnAdmin> vpnAdminWrapper = Wrappers.lambdaQuery(VpnAdmin.class)
                        .eq(StringUtils.isNotBlank(adminName), VpnAdmin::getAdminName, adminName)
                        .eq(StringUtils.isNotBlank(adminPassword), VpnAdmin::getAdminPassword, ReturnCard.LineAESEncrypt(adminPassword));
                List<VpnAdmin> lst = list(vpnAdminWrapper);
                if (lst.size() > 0) {
                    VpnAdmin admin = lst.get(0);
                    LoginContext<VpnAdmin> loginInfo = new LoginContext<>();
                    loginInfo.setLoginName(admin.getAdminName());
                    loginInfo.setCreateTime(LocalDateTime.now());
                    loginInfo.setTimeout(30 * 60 * 1000);
                    loginInfo.setVpnAdmin(admin);
                    LoginContext.setTicket(loginInfo);
                    String adminInfo = AdminLoginInfo.NewAdminLogin(admin.getAdminName());
                    session.setAttribute(admin.getAdminName(), adminInfo);
                    //修改最后登录时间
                    LambdaUpdateWrapper<VpnAdmin> updateWrapper = Wrappers.lambdaUpdate(VpnAdmin.class)
                            .set(VpnAdmin::getLastLogintime, LocalDateTime.now())
                            .eq(VpnAdmin::getId, admin.getId());
                    this.update(updateWrapper);

                    return admin;
                } else {
                    throw new BizException(1, "密码不正确");
                }
            } else {
                throw new BizException(1, "验证码不匹配");
            }
        }
    }

    public Integer LoginOut() {
        try {
            LoginContext.RemoveTicket();
            return 0;
        } catch (Exception e) {
            throw new BizException(1, "登出失败");
        }
    }

    public Integer EditPassword(String oldPassword, String newPassword, long adminId) {
        if (!StringUtils.isNotBlank(oldPassword)) {

            throw new BizException(1, "旧密码是空");
        }
        if (!StringUtils.isNotBlank(newPassword)) {

            throw new BizException(1, "新密码不能为空");
        }
        LambdaUpdateWrapper<VpnAdmin> wrapper = Wrappers.lambdaUpdate(VpnAdmin.class)
                .set(StringUtils.isNotBlank(newPassword), VpnAdmin::getAdminPassword, ReturnCard.LineAESEncrypt(newPassword))
                .eq(VpnAdmin::getAdminPassword, ReturnCard.LineAESEncrypt(oldPassword))
                .eq(VpnAdmin::getId, adminId);
        boolean isSuccess = this.update(wrapper);
        if (false == isSuccess) {
            //添加管理员
            throw new BizException(1, "修改失败!");
        }
        return 0;
    }
}

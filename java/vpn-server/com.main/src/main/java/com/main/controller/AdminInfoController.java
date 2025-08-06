package com.main.controller;

import com.common.exception.BizException;
import com.main.Entity.VpnAdmin;
import com.main.config.PubMsgTask;
import com.main.controller.base.BaseController;
import com.main.manage.RequestLimit;
import com.main.manage.VerifyCodeUtil;
import com.main.manage.requestbodyvo.AdminLoginVO;
import com.main.manage.requestbodyvo.EditpPasswordVO;
import com.main.manage.requestbodyvo.MemberStatusVO;
import com.main.service.AdminService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
@Api(value = "管理员中心", tags = {"管理员查询,登录,添加,修改,禁用"})
@RestController
@RequestMapping(value = "/backend/admin", method = {RequestMethod.POST})
public class AdminInfoController extends BaseController {

    @Autowired
    AdminService adminService;

    @Operation(summary = "管理员登录")
    @RequestMapping("/login")
    public VpnAdmin AdminLogin(@RequestBody AdminLoginVO loginVO) {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        try {
            servletRequestAttributes.getRequest().getSession().setAttribute("logintime", "1");
            String ip = getIpAddress(servletRequestAttributes.getRequest());
            VpnAdmin admin = adminService.AdminLogin(loginVO.getAdminName(), loginVO.getAdminPassword(), loginVO.getVCode(), ip);
            servletRequestAttributes.getRequest().getSession().setAttribute("logintime", "0");
            return admin;
        } catch (Exception e) {
            throw new BizException(1, e.getMessage());
        } finally {
            servletRequestAttributes.getRequest().getSession().setAttribute("logintime", "0");
        }
    }

    @RequestLimit(count = 5, time = 5000)
    @Operation(summary = "验证码生成 调用方式post: http://domain:port/backend/admin/getVerifyCodeImg?time=1000")
    @RequestMapping(method = RequestMethod.GET, path = "/getVerifyCodeImg")
    public void getVerifyCodeImg(HttpServletResponse response, HttpSession session) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        //将验证码文本直接存放到session中
        try {
            ServletOutputStream out = response.getOutputStream();
            VerifyCodeUtil.drawImage(output, session);
            output.writeTo(out);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Operation(summary = "修改密码")
    @RequestMapping("/editpassword")
    public Integer EditpPassword(@RequestBody EditpPasswordVO passVO) {
        return adminService.EditPassword(passVO.getOldPassword(), passVO.getNewPassword(), passVO.getAdminId());
    }

    @Operation(summary = "退出登录")
    @RequestMapping("/loginout")
    public Integer LoginOut() {
        return adminService.LoginOut();
    }
}

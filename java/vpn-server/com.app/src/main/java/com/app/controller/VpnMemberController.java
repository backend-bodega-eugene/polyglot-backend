package com.app.controller;

import com.app.controller.base.BaseController;
import com.app.controller.dto.*;
import com.app.controller.dto.responsedto.BindUserInfoVO;
import com.app.controller.dto.responsedto.SpeedstatusVO;
import com.app.controller.dto.responsedto.VpnMemberResponseVO;
import com.app.controller.dto.responsedto.VpnMemberResponseVOextend;
import com.app.service.AppMemberService;
import com.app.service.VpnLinestatusService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.validation.Valid;
import java.beans.Transient;

@Slf4j
@Api(value = "会员", tags = {"会员登录,注册,退出"})
@RestController
@RequestMapping(value = "/app/member", method = {RequestMethod.POST})
public class VpnMemberController extends BaseController {
    @Autowired
    AppMemberService appMemberService;
    @Autowired
    VpnLinestatusService vpnLinestatusService;


    @Operation(summary = "会员登录 参数:登录名和密码")
    @RequestMapping("/login")
    public VpnMemberResponseVO Login(String memberName, String memberPassword) {
        return appMemberService.MemberLogin(memberName, memberPassword);
    }

    @Operation(summary = "会员登录 参数:登录名和密码")
    @RequestMapping("/memlogin")
    public VpnMemberResponseVO memLogin(@RequestBody @Valid LoginVO vo) {
        return appMemberService.MemberLogin(vo.getMemberName(), vo.getMemberPassword(), vo.getDeviceId());
    }

    @Operation(summary = "会员注册")
    @RequestMapping("/register")
    public VpnMemberResponseVO Register(@RequestBody MemberRequestParam param) {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return appMemberService.MemberRegister(param, getIpAddress(servletRequestAttributes.getRequest()));
    }

    @Operation(summary = "会员注册(新)")
    @RequestMapping("/registerv2")
    public VpnMemberResponseVO Registerv2(@RequestBody MemberRequestParamv2 param) {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return appMemberService.MemberRegisterV2(param, getIpAddress(servletRequestAttributes.getRequest()));
    }

    @Operation(summary = "根据密保,修改密码,无需登录")
    @RequestMapping("/changepassword")
    public Integer ChangePassword(@RequestBody @Valid MemberChangePasswordParam param) {
        return appMemberService.MemberChangePassword(param);
    }

    @Operation(summary = "根据旧密码,修改密码,需要登录")
    @RequestMapping("/changepasswordv2")
    public Integer ChangePasswordv2(@RequestBody @Valid ChangePasswordRequestVO param) {
        //param.setId(getSession());
        return appMemberService.MemberChangePasswordv2(param, getSession());
    }

    @Operation(summary = "加速状态 0,开启加速,1,关闭加速  (memid:用户id,memname :用户账号:加速状态)")
    @RequestMapping("/Speedstatus")
    public Integer Speedstatus(@RequestBody SpeedstatusVO vo) {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String ip = getIpAddress(servletRequestAttributes.getRequest());
        return vpnLinestatusService.Speedstatus(vo.getMemid(), vo.getMemName(), vo.getStatus(), ip);
    }

    @Operation(summary = "扫码创建用户推荐关系")
    @RequestMapping("/scanqrbyicode")
    public Integer scanQrbyicode(long memid, String invitationCode) {

        return appMemberService.scanQrbyicode(memid, invitationCode);
    }

    @Operation(summary = "扫码创建用户推荐关系(新)")
    @RequestMapping("/scanqrbyicodev2")
    public Integer scanQrbyicodeV2(@RequestBody ScanQrbyicodeVO vo) {

        return appMemberService.scanQrbyicode(vo.getMemid(), vo.getInvitationCode(), vo.getDeviceId());
    }

    @Operation(summary = "游客进入")
    @RequestMapping("/visiterenter")

    public VpnMemberResponseVO VisiterEnter(String deviceid) {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String ip = getIpAddress(servletRequestAttributes.getRequest());
        return appMemberService.VisiterEnter(deviceid, ip);
    }

    @Operation(summary = "游客绑定账户")
    @RequestMapping("/binduserInfo")
    @Transient
    public VpnMemberResponseVO BindUserInfo(@RequestBody BindUserInfoVO vo) {
        return appMemberService.BindUserInfo(vo);
    }

    @Operation(summary = "获取用户 根据id")
    @RequestMapping("/getuser")
    public VpnMemberResponseVOextend GetuserInfo() {
        return appMemberService.GetuserInfo(getSession());
    }

    @Operation(summary = "删除设备id")
    @RequestMapping("/deletedevice")
    public void deleteDevice(@RequestBody DeviceIdVO vo) {
        appMemberService.deleteDevice(getSession(), vo.getDeviceId());
    }
}
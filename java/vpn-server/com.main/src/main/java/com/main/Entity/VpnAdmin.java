package com.main.Entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mybatisplus.AbstractEntity;
import lombok.Data;

import java.util.Date;

@Data
@TableName("vpn_admin")
public class VpnAdmin extends AbstractEntity {

    private String adminName;
    private String adminPassword;
    private String contract;
    private String pAdminid;
    private Integer memStatus;
    private Date createTime;
    private Date lastLogintime;

}

package com.app.service;

import com.app.manage.ReturnCard;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.app.Entity.ClientUpgrade;
import com.app.dao.ClientUpgradeMapper;
import com.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bytecode.Throw;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ClientUpgradeService extends ServiceImpl<ClientUpgradeMapper, ClientUpgrade> implements IService<ClientUpgrade> {
    public ClientUpgrade CheckVersion(int versionType) {
        String versionStr = ReturnCard.GetSingleton().GetMap(versionType);
        LambdaQueryWrapper<ClientUpgrade> wrapper = new LambdaQueryWrapper<>(ClientUpgrade.class)
                .like(ClientUpgrade::getVersionType, versionStr)
                .eq(ClientUpgrade::getStatus, 0)
                .orderByDesc(ClientUpgrade::getVersionNumber);
        List<ClientUpgrade> lst = this.list(wrapper);
        if (lst.size() > 0) {
            return lst.get(0);
        } else {
            throw new BizException(1, "没有更新的数据");
        }
    }
}

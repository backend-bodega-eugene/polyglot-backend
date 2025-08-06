package com.app.service;

import com.app.Entity.ClientUpgrade;
import com.app.Entity.LineUserinfo;
import com.app.dao.ClientUpgradeMapper;
import com.app.dao.LineUserinfoMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LineUserinfoService extends ServiceImpl<LineUserinfoMapper, LineUserinfo> implements IService<LineUserinfo> {
}

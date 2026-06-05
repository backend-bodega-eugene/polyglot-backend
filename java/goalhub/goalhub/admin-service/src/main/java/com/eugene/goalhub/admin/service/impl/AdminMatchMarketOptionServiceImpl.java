package com.eugene.goalhub.admin.service.impl;


import com.eugene.goalhub.admin.client.AdminMatchMarketOptionFeignClient;
import com.eugene.goalhub.admin.service.AdminMatchMarketOptionService;
import com.eugene.goalhub.admin.service.support.FeignResultSupport;
import dto.*;
import org.springframework.stereotype.Service;

/**
 * 后台赛事玩法赔率管理服务实现。
 * <p>
 * 当前服务通过 Feign 调用 match-service 的内部赛事玩法赔率接口。
 * 赔率数据的实际持久化和业务校验由 match-service 完成。
 */
@Service
public class AdminMatchMarketOptionServiceImpl implements AdminMatchMarketOptionService {

    /**
     * 后台赛事玩法赔率远程调用客户端。
     */
    private final AdminMatchMarketOptionFeignClient adminMatchMarketOptionFeignClient;

    /**
     * 创建后台赛事玩法赔率管理服务实现。
     *
     * @param adminMatchMarketOptionFeignClient 后台赛事玩法赔率远程调用客户端
     */
    public AdminMatchMarketOptionServiceImpl(
            AdminMatchMarketOptionFeignClient adminMatchMarketOptionFeignClient) {
        this.adminMatchMarketOptionFeignClient = adminMatchMarketOptionFeignClient;
    }

    /**
     * 分页查询赛事玩法赔率。
     *
     * @param request 赛事玩法赔率分页查询条件
     * @return 赛事玩法赔率分页数据
     */
    @Override
    public PageResponse<MatchMarketOptionResponse> page(
            MatchMarketOptionPageRequest request) {

        return FeignResultSupport.data(adminMatchMarketOptionFeignClient.page(request));
    }

    /**
     * 新增赛事玩法赔率。
     *
     * @param request 赛事玩法赔率新增参数
     */
    @Override
    public void add(
            AddMatchMarketOptionRequest request) {

        FeignResultSupport.checkSuccess(adminMatchMarketOptionFeignClient.add(request));
    }

    /**
     * 更新赛事玩法赔率。
     *
     * @param request 赛事玩法赔率更新参数
     */
    @Override
    public void update(
            UpdateMatchMarketOptionRequest request) {

        FeignResultSupport.checkSuccess(adminMatchMarketOptionFeignClient.update(request));
    }

    /**
     * 删除赛事玩法赔率。
     *
     * @param request 赛事玩法赔率删除参数
     */
    @Override
    public void delete(
            DeleteMatchMarketOptionRequest request) {

        FeignResultSupport.checkSuccess(adminMatchMarketOptionFeignClient.delete(request));
    }
}

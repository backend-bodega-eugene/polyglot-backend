package com.app.controller;

import com.app.Entity.VpnArticle;
import com.app.controller.base.BaseController;
import com.app.service.VPNArticleService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Api(value = "资讯", tags = {"资讯查询和每个资讯查询"})
@RestController
@RequestMapping(value = "/app/article", method = {RequestMethod.POST})
public class ArticleController extends BaseController {
    @Autowired
    VPNArticleService vpnArticleService;

    @Operation(summary = "所有资讯查询")
    @RequestMapping("/query")
    public List<VpnArticle> ArticleQuery() {

        return vpnArticleService.ArticleQuery();
    }

    @Operation(summary = "资讯搜索 参数keyword 标题包含文字")
    @RequestMapping("/querybycondition")
    public List<VpnArticle> ArticleQueryByCondition(String keyword) {

        return vpnArticleService.ArticleQueryByCondition(keyword);
    }

    @Operation(summary = "奖励规则")
    @RequestMapping("/querybyreward")
    public List<VpnArticle> ArticleQueryByReward() {

        return vpnArticleService.ArticleQueryByReward();
    }

    @Operation(summary = "用户注册协议")
    @RequestMapping("/querybyprotocol")
    public List<VpnArticle> ArticleQueryByProtocol() {

        return vpnArticleService.ArticleQueryByProtocol();

    }

    @Operation(summary = "隐私政策")
    @RequestMapping("/querybyprivate")
    public List<VpnArticle> ArticleQueryByPrivate() {

        return vpnArticleService.ArticleQueryByPrivate();
    }

    @Operation(summary = "单个资讯详情,访问会增加阅读次数,id参数是文章id")
    @RequestMapping("/queryone")
    public VpnArticle ArticleQueryOne(long articleid) {

        return vpnArticleService.ArticleQueryOne(articleid);

    }
}

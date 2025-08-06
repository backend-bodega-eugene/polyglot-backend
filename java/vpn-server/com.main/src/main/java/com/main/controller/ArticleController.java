package com.main.controller;

import com.main.Entity.VpnArticle;
import com.main.manage.responseVO.PageResultResponseVO;
import com.main.manage.requestbodyvo.VpnArticleRequestVO;
import com.main.manage.requestbodyvo.ArticleRequestVO;
import com.main.manage.requestbodyvo.EditArticleProtocolVO;
import com.main.service.VPNArticleService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Api(value = "资讯管理", tags = {"资讯添加,删除,修改,查询"})
@RestController
@RequestMapping(value = "/backend/article", method = {RequestMethod.POST})
public class ArticleController {
    @Autowired
    VPNArticleService vpnArticleService;

    @Operation(summary = "所有资讯查询 只能传入title进行查询")
    @RequestMapping("/query")
    public PageResultResponseVO<List<VpnArticle>> ArticleQuery(@RequestBody ArticleRequestVO request) {
        return vpnArticleService.ArticleQuery(request);
    }

    @Operation(summary = "添加资讯")
    @RequestMapping("/add")
    public Integer AddArticle(@RequestBody VpnArticleRequestVO request) {
        return vpnArticleService.AddArticle(request);
    }

    @Operation(summary = "编辑资讯")
    @RequestMapping("/edit")
    public Integer EditArticle(@RequestBody VpnArticleRequestVO request) {
        return vpnArticleService.EditArticle(request);
    }

    @Operation(summary = "删除资讯 传入id即可")
    @RequestMapping("/delete")
    public Integer DeleteArticle(@RequestBody VpnArticleRequestVO request) {
        return vpnArticleService.DeleteArticle(request.getId());
    }

    @Operation(summary = "协议管理查询")
    @RequestMapping("/queryprotocol")
    public EditArticleProtocolVO ArticleQueryProtocol() {
        return vpnArticleService.ArticleQueryProtocol();
    }

    @Operation(summary = "协议管理修改")
    @RequestMapping("/editprotocol")
    public Integer EditArticleProtocol(@RequestBody EditArticleProtocolVO vo) {
        return vpnArticleService.EditArticleProtocol(vo.getUserProtocal(), vo.getUserPrivate(), vo.getRewardRules());
    }
}

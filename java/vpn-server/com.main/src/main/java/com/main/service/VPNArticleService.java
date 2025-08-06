package com.main.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.exception.BizException;
import com.main.Entity.VpnArticle;
import com.main.dao.VpnArticleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.result.Result;
import com.common.util.StringUtils;
import com.main.manage.responseVO.PageResultResponseVO;
import com.main.manage.requestbodyvo.VpnArticleRequestVO;
import com.main.manage.requestbodyvo.ArticleRequestVO;
import com.main.manage.requestbodyvo.EditArticleProtocolVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class VPNArticleService extends ServiceImpl<VpnArticleMapper, VpnArticle> implements IService<VpnArticle> {
    public PageResultResponseVO<List<VpnArticle>> ArticleQuery(ArticleRequestVO request) {
        PageResultResponseVO<List<VpnArticle>> response = new PageResultResponseVO<>();
        LambdaQueryWrapper<VpnArticle> wrapper = new LambdaQueryWrapper<>(VpnArticle.class)
                .like(StringUtils.isNotBlank(request.getArticleTitle()), VpnArticle::getArticleTitle, request.getArticleTitle())
                .ne(VpnArticle::getArticleTitle, "奖励规则")
                .ne(VpnArticle::getArticleTitle, "用户注册协议")
                .ne(VpnArticle::getArticleTitle, "隐私政策")
                .orderByDesc(VpnArticle::getSort);
//                .select(VpnArticle::getId,VpnArticle::getArticleTitle,VpnArticle::getArticleContent,
//                        VpnArticle::getAriticleImgurl,VpnArticle::getArticleReleasetime);
        Page pagination = new Page(request.getPageIndex(), request.getPageSize());
        Page<VpnArticle> page = super.page(pagination, wrapper);
        List<VpnArticle> list = page.getRecords();
        response.setPageIndex(page.getCurrent());
        response.setPageSize(page.getSize());
        response.setTotal(page.getTotal());
        response.setData(list);
        return response;
    }
    public EditArticleProtocolVO ArticleQueryProtocol() {
        EditArticleProtocolVO vo = new EditArticleProtocolVO();
        LambdaQueryWrapper<VpnArticle> wrapper = new LambdaQueryWrapper<>(VpnArticle.class)
                .eq(VpnArticle::getRemark, "100");
        // .select(VpnArticle::getId,VpnArticle::getArticleUrl,VpnArticle::getArticleContent);
        List<VpnArticle> lst = this.list(wrapper);
        for (VpnArticle t : lst) {
            if (t.getArticleTitle().equals("奖励规则")) {
                vo.setRewardRules(t.getArticleContent());
            } else if (t.getArticleTitle().equals("用户注册协议")) {
                vo.setUserProtocal(t.getArticleContent());
            } else {
                vo.setUserPrivate(t.getArticleContent());
            }
        }
        return vo;
    }

    public Integer AddArticle(VpnArticleRequestVO request) {
        Result<Integer> result = new Result<>();
        VpnArticle article = new VpnArticle();
        //  article.setAriticleImgurl(request.getAriticleImgurl());
        article.setArticleTitle(request.getArticleTitle());
        //  article.setArticleContent(request.getArticleContent());
        article.setArticleUrl(request.getArticleUrl());
        article.setSort(request.getSort());
        //下面是其他属性
        article.setArticleContentextend("");
        article.setArticleReleasetime(LocalDateTime.now());
        //  article.setAdminId(1);
        // article.setAdminName("admin");
        article.setExpireTime(LocalDateTime.now());
        article.setVisitedCount(1);
        article.setArticleStatus(0);
        article.setRemark("备注");
        boolean isSuccess = this.save(article);
        if (false == isSuccess) {
            throw new BizException(1, "添加失败");

        } else {
            return 0;
        }
    }

    public Integer EditArticle(VpnArticleRequestVO request) {

        Result<Integer> result = new Result<>();
        LambdaUpdateWrapper<VpnArticle> wrapper = Wrappers.lambdaUpdate(VpnArticle.class)
                //  .set(VpnArticle::getAriticleImgurl,request.getAriticleImgurl())
                .set(VpnArticle::getArticleTitle, request.getArticleTitle())
                // .set(VpnArticle::getArticleContent,request.getArticleContent())
                .set(VpnArticle::getArticleUrl, request.getArticleUrl())
                .set(VpnArticle::getSort, request.getSort())
                .eq(VpnArticle::getId, request.getId());
        boolean isSuccess = this.update(wrapper);
        if (false == isSuccess) {
            throw new BizException(1, "修改失败");
        } else {
            return 0;
        }
    }

    public Integer DeleteArticle(long articleid) {

        LambdaQueryWrapper<VpnArticle> wrapper = Wrappers.lambdaQuery(VpnArticle.class)
                .eq(VpnArticle::getId, articleid);
        boolean isSuccess = this.remove(wrapper);
        if (false == isSuccess) {
            throw new BizException(1, "删除失败");
        } else {
            return 0;
        }
    }

    public Integer EditArticleProtocol(String userProtocal, String userPrivate, String rewardRules) {
        try {
            Result<Integer> result = new Result<>();
            LambdaUpdateWrapper<VpnArticle> wrapper = Wrappers.lambdaUpdate(VpnArticle.class)
                    .set(VpnArticle::getArticleContent, userProtocal)
                    .eq(VpnArticle::getArticleTitle, "用户注册协议");
            this.update(wrapper);
            LambdaUpdateWrapper<VpnArticle> wrapper2 = Wrappers.lambdaUpdate(VpnArticle.class)
                    .set(VpnArticle::getArticleContent, userPrivate)
                    .eq(VpnArticle::getArticleTitle, "隐私政策");
            this.update(wrapper2);
            LambdaUpdateWrapper<VpnArticle> wrapper3 = Wrappers.lambdaUpdate(VpnArticle.class)
                    .set(VpnArticle::getArticleContent, rewardRules)
                    .eq(VpnArticle::getArticleTitle, "奖励规则");
            this.update(wrapper3);
            return 0;
        } catch (Exception e) {
            log.info("保存失败",e);
            throw new BizException(1, "保存失败"+e.getMessage());
        }
    }
}
package com.app.service;

import com.app.Entity.VpnArticle;
import com.app.dao.VpnArticleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.exception.BizException;
import com.common.result.Result;
import com.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class VPNArticleService extends ServiceImpl<VpnArticleMapper, VpnArticle> {
    public List<VpnArticle> ArticleQuery() {

        LambdaQueryWrapper<VpnArticle> wrapper = new LambdaQueryWrapper<>(VpnArticle.class)
                .eq(VpnArticle::getArticleStatus, 0)
                .ne(VpnArticle::getArticleTitle, "奖励规则")
                .ne(VpnArticle::getArticleTitle, "用户注册协议")
                .ne(VpnArticle::getArticleTitle, "隐私政策")
                .orderByDesc(VpnArticle::getSort);
        return this.list(wrapper);
    }

    public List<VpnArticle> ArticleQueryByCondition(String keyword) {
        LambdaQueryWrapper<VpnArticle> wrapper = new LambdaQueryWrapper<>(VpnArticle.class)
                .eq(VpnArticle::getArticleStatus, 0)
                .ne(VpnArticle::getArticleTitle, "奖励规则")
                .ne(VpnArticle::getArticleTitle, "用户注册协议")
                .ne(VpnArticle::getArticleTitle, "隐私政策")
                .like(StringUtils.isNotBlank(keyword), VpnArticle::getArticleTitle, keyword)
                .orderByDesc(VpnArticle::getSort);
        return this.list(wrapper);
    }

    public List<VpnArticle> ArticleQueryByReward() {

        LambdaQueryWrapper<VpnArticle> wrapper = new LambdaQueryWrapper<>(VpnArticle.class)
                .eq(VpnArticle::getArticleTitle, "奖励规则");
        return this.list(wrapper);
    }

    public List<VpnArticle> ArticleQueryByProtocol() {
        Result<List<VpnArticle>> result = new Result<>();
        LambdaQueryWrapper<VpnArticle> wrapper = new LambdaQueryWrapper<>(VpnArticle.class)
                .eq(VpnArticle::getArticleTitle, "用户注册协议");
        return this.list(wrapper);
    }

    public List<VpnArticle> ArticleQueryByPrivate() {
        LambdaQueryWrapper<VpnArticle> wrapper = new LambdaQueryWrapper<>(VpnArticle.class)
                .eq(VpnArticle::getArticleTitle, "隐私政策");
        return this.list(wrapper);
    }

    public VpnArticle ArticleQueryOne(long id) {

        LambdaQueryWrapper<VpnArticle> wrapper = new LambdaQueryWrapper<>(VpnArticle.class)
                .eq(VpnArticle::getArticleStatus, 0)
                .eq(VpnArticle::getId, id);
        List<VpnArticle> articles = this.list(wrapper);
        if (articles.size() > 0) {
            LambdaUpdateWrapper<VpnArticle> wrapperUpdate = Wrappers.lambdaUpdate(VpnArticle.class)
                    .set(VpnArticle::getVisitedCount, articles.get(0).getVisitedCount() + 1)
                    .eq(VpnArticle::getId, id);
            this.update(wrapperUpdate);
            return articles.get(0);
        } else {
            throw new BizException(1, "出现错误");
        }
    }
}

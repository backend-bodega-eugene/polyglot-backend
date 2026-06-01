package com.eugene.goalhub.match.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eugene.goalhub.match.entity.UserMatchFollow;
import com.eugene.goalhub.match.mapper.UserMatchFollowMapper;
import com.eugene.goalhub.match.service.SoccerMatchService;
import com.eugene.goalhub.match.service.UserMatchFollowService;
import dto.UserMatchFollowResponse;
import exception.BusinessException;
import org.springframework.stereotype.Service;
import response.ResultCode;

import java.util.List;

/**
 * 用户赛事关注服务实现。
 */
@Service
public class UserMatchFollowServiceImpl
        extends ServiceImpl<UserMatchFollowMapper, UserMatchFollow>
        implements UserMatchFollowService {

    /**
     * 赛事查询服务，用于校验赛事是否存在。
     */
    private final SoccerMatchService soccerMatchService;

    /**
     * 创建用户赛事关注服务实现。
     *
     * @param soccerMatchService 赛事查询服务
     */
    public UserMatchFollowServiceImpl(SoccerMatchService soccerMatchService) {
        this.soccerMatchService = soccerMatchService;
    }

    /**
     * 关注赛事。
     * <p>
     * 如果用户已关注该赛事，则直接返回，避免重复插入。
     *
     * @param userId  用户 ID
     * @param matchId 赛事 ID
     */
    @Override
    public void follow(Long userId, Long matchId) {
        checkMatchExists(matchId);

        boolean exists = lambdaQuery()
                .eq(UserMatchFollow::getUserId, userId)
                .eq(UserMatchFollow::getMatchId, matchId)
                .exists();

        if (exists) {
            return;
        }

        UserMatchFollow follow = new UserMatchFollow();
        follow.setUserId(userId);
        follow.setMatchId(matchId);

        save(follow);
    }

    /**
     * 取消关注赛事。
     *
     * @param userId  用户 ID
     * @param matchId 赛事 ID
     */
    @Override
    public void unfollow(Long userId, Long matchId) {
        checkMatchExists(matchId);

//        boolean exists = lambdaQuery()
//                .eq(UserMatchFollow::getUserId, userId)
//                .eq(UserMatchFollow::getMatchId, matchId)
//                .exists();
//
//        if (!exists) {
//            throw new BusinessException("还没有关注该赛事");
//        }

        lambdaUpdate()
                .eq(UserMatchFollow::getUserId, userId)
                .eq(UserMatchFollow::getMatchId, matchId)
                .remove();
    }

    /**
     * 判断用户是否已关注赛事。
     *
     * @param userId  用户 ID
     * @param matchId 赛事 ID
     * @return true 表示已关注
     */
    @Override
    public Boolean isFollowed(Long userId, Long matchId) {
        checkMatchExists(matchId);

        return lambdaQuery()
                .eq(UserMatchFollow::getUserId, userId)
                .eq(UserMatchFollow::getMatchId, matchId)
                .exists();
    }

    /**
     * 查询用户关注记录。
     *
     * @param userId 用户 ID
     * @return 关注记录列表
     */
    @Override
    public List<UserMatchFollowResponse> listMyFollows(Long userId) {
        return lambdaQuery()
                .eq(UserMatchFollow::getUserId, userId)
                .orderByDesc(UserMatchFollow::getCreatedAt)
                .list()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * 将关注实体转换为响应对象。
     *
     * @param follow 关注实体
     * @return 用户赛事关注响应
     */
    private UserMatchFollowResponse toResponse(UserMatchFollow follow) {
        UserMatchFollowResponse response = new UserMatchFollowResponse();
        response.setId(follow.getId());
        response.setUserId(follow.getUserId());
        response.setMatchId(follow.getMatchId());
        response.setCreatedAt(follow.getCreatedAt());
        response.setUpdatedAt(follow.getUpdatedAt());
        return response;
    }

    /**
     * 校验赛事是否存在。
     *
     * @param matchId 赛事 ID
     */
    private void checkMatchExists(Long matchId) {
        boolean matchExists = soccerMatchService.existsById(matchId);

        if (!matchExists) {
            throw new BusinessException(ResultCode.SOCCER_NOT_EXISTS);
        }
    }
}

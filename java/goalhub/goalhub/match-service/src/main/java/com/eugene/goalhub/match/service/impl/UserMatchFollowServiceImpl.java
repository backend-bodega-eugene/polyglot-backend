package com.eugene.goalhub.match.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eugene.goalhub.match.entity.UserMatchFollow;
import com.eugene.goalhub.match.mapper.UserMatchFollowMapper;
import com.eugene.goalhub.match.service.SoccerMatchService;
import com.eugene.goalhub.match.service.UserMatchFollowService;
import com.eugene.goalhub.match.service.support.MatchOperationLogger;
import dto.UserMatchFollowResponse;
import exception.BusinessException;
import org.springframework.stereotype.Service;
import response.ResultCode;

import java.util.List;

/**
 * 用户赛事关注服务实现。
 *
 * <p>负责用户关注赛事、取消关注、关注状态判断和关注列表查询。</p>
 */
@Service
public class UserMatchFollowServiceImpl
        extends ServiceImpl<UserMatchFollowMapper, UserMatchFollow>
        implements UserMatchFollowService {

    /**
     * 业务日志模块名称。
     */
    private static final String MODULE_NAME = "用户赛事关注";

    /**
     * 赛事查询服务，用于校验赛事是否存在。
     */
    private final SoccerMatchService soccerMatchService;

    /**
     * 比赛服务操作日志工具。
     */
    private final MatchOperationLogger matchOperationLogger;

    /**
     * 创建用户赛事关注服务实现。
     *
     * @param soccerMatchService 赛事查询服务
     */
    public UserMatchFollowServiceImpl(SoccerMatchService soccerMatchService,
                                      MatchOperationLogger matchOperationLogger) {
        this.soccerMatchService = soccerMatchService;
        this.matchOperationLogger = matchOperationLogger;
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
        matchOperationLogger.userBizLog(
                MODULE_NAME,
                "FOLLOW_MATCH",
                "关注赛事成功，userId=" + userId + ", matchId=" + matchId
        );
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
        matchOperationLogger.userBizLog(
                MODULE_NAME,
                "UNFOLLOW_MATCH",
                "取消关注赛事成功，userId=" + userId + ", matchId=" + matchId
        );
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

        boolean followed = lambdaQuery()
                .eq(UserMatchFollow::getUserId, userId)
                .eq(UserMatchFollow::getMatchId, matchId)
                .exists();
        matchOperationLogger.sysLog(
                MODULE_NAME,
                "IS_MATCH_FOLLOWED",
                "查询赛事关注状态，userId=" + userId
                        + ", matchId=" + matchId
                        + ", followed=" + followed
        );
        return followed;
    }

    /**
     * 查询用户关注记录。
     *
     * @param userId 用户 ID
     * @return 关注记录列表
     */
    @Override
    public List<UserMatchFollowResponse> listMyFollows(Long userId) {
        List<UserMatchFollowResponse> responses = lambdaQuery()
                .eq(UserMatchFollow::getUserId, userId)
                .orderByDesc(UserMatchFollow::getCreatedAt)
                .list()
                .stream()
                .map(this::toResponse)
                .toList();
        matchOperationLogger.sysLog(
                MODULE_NAME,
                "LIST_MY_FOLLOWS",
                "查询用户赛事关注列表，userId=" + userId
                        + ", resultCount=" + responses.size()
        );
        return responses;
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

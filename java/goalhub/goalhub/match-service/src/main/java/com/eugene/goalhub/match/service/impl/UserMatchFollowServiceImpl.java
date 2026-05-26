package com.eugene.goalhub.match.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eugene.goalhub.match.entity.UserMatchFollow;
import com.eugene.goalhub.match.mapper.UserMatchFollowMapper;
import com.eugene.goalhub.match.service.SoccerMatchService;
import com.eugene.goalhub.match.service.UserMatchFollowService;
import exception.BusinessException;
import org.springframework.stereotype.Service;
import response.ResultCode;

import java.util.List;

@Service
public class UserMatchFollowServiceImpl
        extends ServiceImpl<UserMatchFollowMapper, UserMatchFollow>
        implements UserMatchFollowService {

    private final SoccerMatchService soccerMatchService;

    public UserMatchFollowServiceImpl(SoccerMatchService soccerMatchService) {
        this.soccerMatchService = soccerMatchService;
    }

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

    @Override
    public Boolean isFollowed(Long userId, Long matchId) {
        checkMatchExists(matchId);

        return lambdaQuery()
                .eq(UserMatchFollow::getUserId, userId)
                .eq(UserMatchFollow::getMatchId, matchId)
                .exists();
    }

    @Override
    public List<UserMatchFollow> listMyFollows(Long userId) {
        return lambdaQuery()
                .eq(UserMatchFollow::getUserId, userId)
                .orderByDesc(UserMatchFollow::getCreatedAt)
                .list();
    }

    private void checkMatchExists(Long matchId) {
        boolean matchExists = soccerMatchService.existsById(matchId);

        if (!matchExists) {
            throw new BusinessException(ResultCode.SOCCER_NOT_EXISTS);
        }
    }
}
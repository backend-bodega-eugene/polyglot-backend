package com.eugene.goalhub.match.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.eugene.goalhub.match.entity.UserMatchFollow;
import dto.UserMatchFollowResponse;

import java.util.List;

/**
 * 用户赛事关注服务。
 */
public interface UserMatchFollowService extends IService<UserMatchFollow> {

    /**
     * 关注赛事。
     *
     * @param userId  用户 ID
     * @param matchId 赛事 ID
     */
    void follow(Long userId, Long matchId);

    /**
     * 取消关注赛事。
     *
     * @param userId  用户 ID
     * @param matchId 赛事 ID
     */
    void unfollow(Long userId, Long matchId);

    /**
     * 判断用户是否已关注赛事。
     *
     * @param userId  用户 ID
     * @param matchId 赛事 ID
     * @return true 表示已关注
     */
    Boolean isFollowed(Long userId, Long matchId);

    /**
     * 查询用户关注的赛事记录。
     *
     * @param userId 用户 ID
     * @return 关注记录列表
     */
    List<UserMatchFollowResponse> listMyFollows(Long userId);
}

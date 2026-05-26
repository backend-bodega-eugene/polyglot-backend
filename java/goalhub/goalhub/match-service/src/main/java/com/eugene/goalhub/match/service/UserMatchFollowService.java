package com.eugene.goalhub.match.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.eugene.goalhub.match.entity.UserMatchFollow;

import java.util.List;

public interface UserMatchFollowService extends IService<UserMatchFollow> {

    void follow(Long userId, Long matchId);

    void unfollow(Long userId, Long matchId);

    Boolean isFollowed(Long userId, Long matchId);

    List<UserMatchFollow> listMyFollows(Long userId);
}
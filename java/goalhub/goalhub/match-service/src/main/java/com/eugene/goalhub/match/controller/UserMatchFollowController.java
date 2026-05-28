package com.eugene.goalhub.match.controller;

import com.eugene.goalhub.match.entity.UserMatchFollow;
import com.eugene.goalhub.match.service.UserMatchFollowService;
import org.springframework.web.bind.annotation.*;
import response.Result;

import java.util.List;

/**
 * 用户赛事关注接口。
 */
@RestController
@RequestMapping("/soccer/follow")
public class UserMatchFollowController {

    /**
     * 用户赛事关注服务。
     */
    private final UserMatchFollowService userMatchFollowService;

    public UserMatchFollowController(UserMatchFollowService userMatchFollowService) {
        this.userMatchFollowService = userMatchFollowService;
    }

    /**
     * 关注指定赛事。
     *
     * @param userId  当前登录用户 ID，由网关写入请求头
     * @param matchId 赛事 ID
     * @return 空结果
     */
    @PostMapping("/{matchId}")
    public Result<Void> follow(@RequestHeader("X-User-Id") Long userId,
                               @PathVariable("matchId") Long matchId) {
        userMatchFollowService.follow(userId, matchId);
        return Result.success();
    }

    /**
     * 取消关注指定赛事。
     *
     * @param userId  当前登录用户 ID，由网关写入请求头
     * @param matchId 赛事 ID
     * @return 空结果
     */
    @DeleteMapping("/{matchId}")
    public Result<Void> unfollow(@RequestHeader("X-User-Id") Long userId,
                                 @PathVariable("matchId") Long matchId) {
        userMatchFollowService.unfollow(userId, matchId);
        return Result.success();
    }

    /**
     * 检查当前用户是否已关注指定赛事。
     *
     * @param userId  当前登录用户 ID，由网关写入请求头
     * @param matchId 赛事 ID
     * @return true 表示已关注
     */
    @GetMapping("/check/{matchId}")
    public Result<Boolean> check(@RequestHeader("X-User-Id") Long userId,
                                 @PathVariable("matchId") Long matchId) {
        return Result.success(userMatchFollowService.isFollowed(userId, matchId));
    }

    /**
     * 查询当前用户的关注列表。
     *
     * @param userId 当前登录用户 ID，由网关写入请求头
     * @return 用户关注记录列表
     */
    @GetMapping("/my")
    public Result<List<UserMatchFollow>> myFollows(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(userMatchFollowService.listMyFollows(userId));
    }
}

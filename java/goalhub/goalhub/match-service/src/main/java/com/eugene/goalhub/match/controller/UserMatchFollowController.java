package com.eugene.goalhub.match.controller;

import com.eugene.goalhub.match.service.UserMatchFollowService;
import dto.UserMatchFollowResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import response.Result;

import java.util.List;

/**
 * 用户赛事关注接口。
 */
@Tag(name = "用户赛事关注", description = "用户关注、取消关注和查询赛事关注状态接口")
@RestController
@RequestMapping("/soccer/follow")
public class UserMatchFollowController {

    /**
     * 用户赛事关注服务。
     */
    private final UserMatchFollowService userMatchFollowService;

    /**
     * 创建用户赛事关注接口实例。
     *
     * @param userMatchFollowService 用户赛事关注服务
     */
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
    @Operation(summary = "关注指定赛事", description = "当前登录用户关注指定足球赛事。")
    @PostMapping("/{matchId}")
    public Result<Void> follow(@Parameter(description = "当前登录用户 ID，由网关写入请求头", required = true)
                               @RequestHeader("X-User-Id") Long userId,
                               @Parameter(description = "赛事 ID", required = true)
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
    @Operation(summary = "取消关注指定赛事", description = "当前登录用户取消关注指定足球赛事。")
    @DeleteMapping("/{matchId}")
    public Result<Void> unfollow(@Parameter(description = "当前登录用户 ID，由网关写入请求头", required = true)
                                 @RequestHeader("X-User-Id") Long userId,
                                 @Parameter(description = "赛事 ID", required = true)
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
    @Operation(summary = "检查赛事关注状态", description = "检查当前登录用户是否已关注指定足球赛事。")
    @GetMapping("/check/{matchId}")
    public Result<Boolean> check(@Parameter(description = "当前登录用户 ID，由网关写入请求头", required = true)
                                 @RequestHeader("X-User-Id") Long userId,
                                 @Parameter(description = "赛事 ID", required = true)
                                 @PathVariable("matchId") Long matchId) {
        return Result.success(userMatchFollowService.isFollowed(userId, matchId));
    }

    /**
     * 查询当前用户的关注列表。
     *
     * @param userId 当前登录用户 ID，由网关写入请求头
     * @return 用户关注记录列表
     */
    @Operation(summary = "查询我的赛事关注列表", description = "查询当前登录用户关注的足球赛事记录列表。")
    @GetMapping("/my")
    public Result<List<UserMatchFollowResponse>> myFollows(
            @Parameter(description = "当前登录用户 ID，由网关写入请求头", required = true)
            @RequestHeader("X-User-Id") Long userId) {
        return Result.success(userMatchFollowService.listMyFollows(userId));
    }
}

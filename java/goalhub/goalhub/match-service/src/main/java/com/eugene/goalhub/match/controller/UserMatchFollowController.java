package com.eugene.goalhub.match.controller;

import com.eugene.goalhub.match.entity.UserMatchFollow;
import com.eugene.goalhub.match.service.UserMatchFollowService;
import org.springframework.web.bind.annotation.*;
import response.Result;

import java.util.List;

@RestController
@RequestMapping("/soccer/follow")
public class UserMatchFollowController {

    private final UserMatchFollowService userMatchFollowService;

    public UserMatchFollowController(UserMatchFollowService userMatchFollowService) {
        this.userMatchFollowService = userMatchFollowService;
    }

    @PostMapping("/{matchId}")
    public Result<Void> follow(@RequestHeader("X-User-Id") Long userId,
                               @PathVariable("matchId") Long matchId) {
        userMatchFollowService.follow(userId, matchId);
        return Result.success();
    }

    @DeleteMapping("/{matchId}")
    public Result<Void> unfollow(@RequestHeader("X-User-Id") Long userId,
                                 @PathVariable("matchId") Long matchId) {
        userMatchFollowService.unfollow(userId, matchId);
        return Result.success();
    }

    @GetMapping("/check/{matchId}")
    public Result<Boolean> check(@RequestHeader("X-User-Id") Long userId,
                                 @PathVariable("matchId") Long matchId) {
        return Result.success(userMatchFollowService.isFollowed(userId, matchId));
    }

    @GetMapping("/my")
    public Result<List<UserMatchFollow>> myFollows(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(userMatchFollowService.listMyFollows(userId));
    }
}
package com.eugene.goalhub.admin.service.impl;

import com.eugene.goalhub.admin.client.AdminMatchFeignClient;
import com.eugene.goalhub.admin.service.AdminMatchService;
import dto.*;
import org.springframework.stereotype.Service;
import response.Result;

@Service
public class AdminMatchServiceImpl implements AdminMatchService {

    private final AdminMatchFeignClient adminMatchFeignClient;

    public AdminMatchServiceImpl(
            AdminMatchFeignClient adminMatchFeignClient) {
        this.adminMatchFeignClient = adminMatchFeignClient;
    }

    @Override
    public PageResponse<AdminLeagueResponse> leaguePage(
            LeaguePageRequest request) {

        Result<PageResponse<AdminLeagueResponse>> result =
                adminMatchFeignClient.leaguePage(request);

        return result.getData();
    }

    @Override
    public void addLeague(
            AddLeagueRequest request) {

        adminMatchFeignClient.addLeague(request);
    }

    @Override
    public void updateLeague(
            UpdateLeagueRequest request) {

        adminMatchFeignClient.updateLeague(request);
    }

    @Override
    public void deleteLeague(
            DeleteLeagueRequest request) {

        adminMatchFeignClient.deleteLeague(request);
    }

    @Override
    public PageResponse<AdminMatchResponse> matchPage(
            MatchPageRequest request) {

        Result<PageResponse<AdminMatchResponse>> result =
                adminMatchFeignClient.matchPage(request);

        return result.getData();
    }

    @Override
    public void addMatch(
            AddMatchRequest request) {

        adminMatchFeignClient.addMatch(request);
    }

    @Override
    public void updateMatch(
            UpdateMatchRequest request) {

        adminMatchFeignClient.updateMatch(request);
    }

    @Override
    public void deleteMatch(
            DeleteMatchRequest request) {

        adminMatchFeignClient.deleteMatch(request);
    }
    @Override
    public PageResponse<AdminTeamResponse> teamPage(TeamPageRequest request) {
        Result<PageResponse<AdminTeamResponse>> result =
                adminMatchFeignClient.teamPage(request);

        return result.getData();
    }

    @Override
    public void addTeam(AddTeamRequest request) {
        adminMatchFeignClient.addTeam(request);
    }

    @Override
    public void updateTeam(UpdateTeamRequest request) {
        adminMatchFeignClient.updateTeam(request);
    }

    @Override
    public void deleteTeam(DeleteTeamRequest request) {
        adminMatchFeignClient.deleteTeam(request);
    }
}
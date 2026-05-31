package com.eugene.goalhub.admin.service.impl;

import com.eugene.goalhub.admin.client.AdminMatchI18nFeignClient;
import com.eugene.goalhub.admin.service.AdminMatchI18nService;
import dto.*;
import org.springframework.stereotype.Service;
import response.Result;

import java.util.List;

@Service
public class AdminMatchI18nServiceImpl implements AdminMatchI18nService {

    private final AdminMatchI18nFeignClient adminMatchI18nFeignClient;

    public AdminMatchI18nServiceImpl(AdminMatchI18nFeignClient adminMatchI18nFeignClient) {
        this.adminMatchI18nFeignClient = adminMatchI18nFeignClient;
    }

    @Override
    public List<LeagueI18nResponse> listLeagueI18n(LeagueI18nListRequest request) {
        Result<List<LeagueI18nResponse>> result =
                adminMatchI18nFeignClient.listLeagueI18n(request);

        return result.getData();
    }

    @Override
    public void addLeagueI18n(AddLeagueI18nRequest request) {
        adminMatchI18nFeignClient.addLeagueI18n(request);
    }

    @Override
    public void updateLeagueI18n(UpdateLeagueI18nRequest request) {
        adminMatchI18nFeignClient.updateLeagueI18n(request);
    }

    @Override
    public void deleteLeagueI18n(DeleteLeagueI18nRequest request) {
        adminMatchI18nFeignClient.deleteLeagueI18n(request);
    }

    @Override
    public List<MatchI18nResponse> listMatchI18n(MatchI18nListRequest request) {
        Result<List<MatchI18nResponse>> result =
                adminMatchI18nFeignClient.listMatchI18n(request);

        return result.getData();
    }

    @Override
    public void addMatchI18n(AddMatchI18nRequest request) {
        adminMatchI18nFeignClient.addMatchI18n(request);
    }

    @Override
    public void updateMatchI18n(UpdateMatchI18nRequest request) {
        adminMatchI18nFeignClient.updateMatchI18n(request);
    }

    @Override
    public void deleteMatchI18n(DeleteMatchI18nRequest request) {
        adminMatchI18nFeignClient.deleteMatchI18n(request);
    }

    @Override
    public List<TeamI18nResponse> listTeamI18n(TeamI18nListRequest request) {
        Result<List<TeamI18nResponse>> result =
                adminMatchI18nFeignClient.listTeamI18n(request);

        return result.getData();
    }

    @Override
    public void addTeamI18n(AddTeamI18nRequest request) {
        adminMatchI18nFeignClient.addTeamI18n(request);
    }

    @Override
    public void updateTeamI18n(UpdateTeamI18nRequest request) {
        adminMatchI18nFeignClient.updateTeamI18n(request);
    }

    @Override
    public void deleteTeamI18n(DeleteTeamI18nRequest request) {
        adminMatchI18nFeignClient.deleteTeamI18n(request);
    }
}
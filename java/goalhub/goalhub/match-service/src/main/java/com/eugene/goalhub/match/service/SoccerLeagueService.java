package com.eugene.goalhub.match.service;

import dto.SoccerLeagueResponse;

import java.util.List;

public interface SoccerLeagueService {

    List<SoccerLeagueResponse> listLeagues(String keyword, String langCode);
}
package com.eugene.goalhub.match.service;

import dto.AppMatchOddsResponse;

public interface AppMatchOddsService {

    AppMatchOddsResponse getMatchOdds(
            Long matchId);
}
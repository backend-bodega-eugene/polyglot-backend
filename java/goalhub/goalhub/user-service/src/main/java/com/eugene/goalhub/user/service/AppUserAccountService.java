package com.eugene.goalhub.user.service;

import dto.AppUserBalanceResponse;

public interface AppUserAccountService {

    AppUserBalanceResponse getDefaultBalance(
            Long userId);
}
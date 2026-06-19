package com.eugene.goalhub.order.judge;

/**
 * 投注系统预判结果。
 */
public enum BetSystemResult {

    /**
     * 赢。
     */
    WIN,

    /**
     * 输。
     */
    LOSE,

    /**
     * 走水/退本金。
     */
    PUSH,

    /**
     * 取消。
     */
    CANCELLED
}
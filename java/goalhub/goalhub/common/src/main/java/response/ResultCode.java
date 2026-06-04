package response;

public enum ResultCode {
    SUCCESS(200, "success"),
    BAD_REQUEST(400, "bad.request"),
    UNAUTHORIZED(401, "unauthorized"),
    FORBIDDEN(403, "forbidden"),
    NOT_FOUND(404, "not.found"),
    INTERNAL_ERROR(500, "internal.error"),
    USERNAME_EXISTS(10001, "username.already.exists"),
    USER_NOT_FOUND(10002, "user.not.found"),
    PASSWORD_ERROR(10003, "password.error"),
    USERNAME_NOT_NULL(10004, "username.cannot.be.empty"),
    PASSWORD_NOT_NULL(10005, "password.cannot.be.empty"),
    PAGESIZE_NOT_NULL(10006,"pageSize.not.null"),
    PAGEINDEX_NOT_NULL(10007,"pageIndex.not.null"),
    SOCCER_NOT_EXISTS(10008,"match.not.exists"),
    MENU_NOT_EXISTS(10009,"menu.not.exists"),
    MENU_HAVE_CHILDREN(10010,"menu.has.children"),
    MENU_TYPE_NOT_NULL(10011,"menu.type.not.null"),
    MENU_WRONG_TYPE(10012,"menu.wrong.type"),
    USERNAME_PASSWORD_WRONG(10013,"username.password.wrong"),
    USERNAME__NOT_EXISTS(10014,"username.not.exists"),
    EUGENE_NOT_DELETE(10015,"eugene.not.delete"),
    MISS_TOKEN(10016,"missing.token"),
    ACCOUNT_NOT_FOUND(10017,"account.not.found"),
    BALANCE_NOT_ENOUGH(10018,"balance.not.enough"),
    PARAM_ERROR(10019,"parameter.error"),
    MATCH_RESULT_APPROVED(10020,"match.result.approved"),
    MATCH_RESULT_NOT_FOUND(10021,"match.result.not.found"),
    FAIL(10022, "fail"),
    FATHER_NOT_CHILD_CODE(10023,"father.not.child.code"),
    FEIGN_RESULT_FAIL(10024,"feign.result.fail"),
    SUPER_ADMIN_STATUS_NOT_ALLOW_UPDATE_CODE(10025,"super.admin.status.not.already.exists"),
    BET_MARKET_CODE_ALREADY_EXISTS(10026,"bet.market.code.already.exists"),
    BET_MARKET_NOT_FOUND(10027,"bet.market.not.found"),
    BET_MARKET_HAS_OPTIONS(10028, "bet.market.has.options"),
    BET_MARKET_OPTION_CODE_ALREADY_EXISTS(10029,"bet.market.option.code.already.exists"),
    BET_MARKET_OPTION_NOT_FOUND(10030, "bet.market.option.not.found"),
    MATCH_MARKET_OPTION_NOT_FOUND(10031, "match.market.option.not.found"),
    BET_ORDER_ONLY_PENDING_CAN_REVIEW(10032,"bet.order.only.pending.can.review" ),
    BET_ORDER_ONLY_PENDING_CAN_FREEZE(10033,"bet.order.only.pending.can.freeze"),
    BET_ORDER_ALREADY_SETTLED(10034,"bet.order.already.already.settled"),
    BET_ORDER_SETTLE_AMOUNT_INVALID(10035, "bet.order.settle.amount.invalid"),
    BET_ORDER_STATUS_NOT_ALLOW_SETTLE(10036,"bet.order.status.not.already.exists"),
    BET_ORDER_ID_NOT_NULL(10037,"bet.order.id.not.null"),
    BET_ORDER_NOT_FOUND(10038,"bet.order.not.found"),
    BET_ORDER_REMARK_NOT_NULL(10039,"bet.order.remark.not.null"),
    BET_ORDER_REVIEW_RESULT_NOT_NULL(10040,"bet.order.review.result.not.null"),
    BET_ORDER_FREEZE_USE_FREEZE_API(10041,"bet.order.freeze.use.freeze"),
    BET_ORDER_SYSTEM_RESULT_NOT_EXISTS(10042,"bet.order.system.result.not.exists"),
    BET_ORDER_SYSTEM_WIN_REVIEW_RESULT_INVALID(10043,"bet.order.system.win.result.invalid"),
    BET_ORDER_SYSTEM_LOSE_REVIEW_RESULT_INVALID(10044,"bet.order.system.lose.result.invalid"),
    BET_ORDER_SYSTEM_REFUNDED_REVIEW_RESULT_INVALID(10045,"bet.order.system.refunded.result.invalid"),
    BET_ORDER_SYSTEM_CANCELLED_REVIEW_RESULT_INVALID(10046,"bet.order.system.cancelled.result.invalid"),
    BET_ORDER_SYSTEM_RESULT_UNKNOWN(10047,"bet.order.system.result.unknown"),
    ORDER_USER_ACCOUNT_FEIGN_RESULT_NULL(10048,"order.user.account.feign.result.null"),
    ORDER_USER_ACCOUNT_FEIGN_RESULT_FAIL(10049,"order.user.account.feign.result.fail"),
    ACCOUNT_CHANGE_AMOUNT_NOT_NULL(10050,"account.change.amount.not.null"),
    USER_DISABLED(10051,"user.disabled"),
    CAPTCHA_NOT_NULL(10052,"captcha.not.null"),
    CAPTCHA_EXPIRED(10053,"captcha.expired"),
    CAPTCHA_ERROR(10054,"captcha.error"),
    REGISTER_TOO_FREQUENT(10055,"register.too.frequent"),
    LOGIN_TOO_FREQUENT(10056,"login.too.frequent"),
    LOGIN_ACCOUNT_LOCKED(10057,"login.account.locked"),
    USER_ID_NOT_NULL(10058,"user.id.not.null"),
    BET_ORDER_REQUEST_NOT_NULL(10059,"bet.order.request.not.null"),
    MATCH_MARKET_OPTION_ID_NOT_NULL(10060,"match.market.option.id.not.null"),
    BET_AMOUNT_INVALID(10061,"bet.amount.invalid"),
    ORDER_MATCH_FEIGN_RESULT_NULL(10062,"order.match.feign.result.null"),
    ORDER_MATCH_FEIGN_RESULT_FAIL(10063,"order.match.feign.result.fail"),
    ORDER_MATCH_SNAPSHOT_NOT_FOUND(10064,"order.match.snapshot.not.found"),
    BET_OPTION_NOT_VISIBLE(10065,"bet.option.not.visible"),
    BET_OPTION_NOT_OPEN(10066,"bet.option.not.open"),
    BET_ODDS_INVALID(10067,"bet.odds.invalid"),
    MATCH_NOT_ALLOW_BET(10068,"match.not.allow.bet"),
    ORDER_ACCOUNT_FEIGN_RESULT_NULL(10069,"order.account.feign.result.null"),
    ORDER_ACCOUNT_FEIGN_RESULT_FAIL(10070,"order.account.feign.result.fail"),
    ORDER_ACCOUNT_DEDUCT_RESULT_NULL(10071,"order.account.deduct.result.null"),

    FATHER_NOT_OWN(10100,"father.not.own");

   // SOCCER_NOT_EXISTS(10009,"没有关注");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() { return code; }

    public String getMessage() { return message; }
}
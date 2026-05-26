package response;

public enum ResultCode {
    SUCCESS(200, "success"),
    BAD_REQUEST(400, "bad request"),
    UNAUTHORIZED(401, "unauthorized"),
    FORBIDDEN(403, "forbidden"),
    NOT_FOUND(404, "not found"),
    INTERNAL_ERROR(500, "internal error"),

    USERNAME_EXISTS(10001, "username already exists"),
    USER_NOT_FOUND(10002, "user not found"),
    PASSWORD_ERROR(10003, "password error"),
    USERNAME_NOT_NULL(10004, "username cannot be empty"),
    PASSWORD_NOT_NULL(10005, "password cannot be empty"),
    PAGESIZE_NOT_NULL(10006,"pageSize不能为空"),
    PAGEINDEX_NOT_NULL(10007,"pageIndex不能为空"),
    SOCCER_NOT_EXISTS(10008,"赛事不存在");
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
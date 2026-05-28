package response;

public class Result<T> {

    private int code;
    private String message;
    private T data;
    public Result(){

    }
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> fail(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }
    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null);
    }
    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public int getCode() { return code; }

    public String getMessage() { return message; }

    public T getData() { return data; }
}
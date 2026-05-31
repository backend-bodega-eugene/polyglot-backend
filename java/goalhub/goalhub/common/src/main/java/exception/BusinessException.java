package exception;

import lombok.Data;
import response.ResultCode;

/**
 * 业务异常。
 */
@Data
public class BusinessException extends RuntimeException {

    /**
     * 业务错误码。
     */
    private Integer code;

    /**
     * 根据结果码创建业务异常。
     *
     * @param resultCode 结果码
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    /**
     * 根据错误码和错误消息创建业务异常。
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}

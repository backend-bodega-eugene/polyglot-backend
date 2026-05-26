package exception;

import lombok.Data;
import response.ResultCode;

@Data
public class BusinessException extends RuntimeException {

    private Integer code;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
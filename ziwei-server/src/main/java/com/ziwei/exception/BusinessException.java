package com.ziwei.exception;

import com.ziwei.enums.ErrorCode;
import lombok.Getter;

/**
 * Business logic exception
 *
 * @author JTWORLD
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String detail) {
        super(errorCode.getMessage() + ": " + detail);
        this.code = errorCode.getCode();
    }

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

}

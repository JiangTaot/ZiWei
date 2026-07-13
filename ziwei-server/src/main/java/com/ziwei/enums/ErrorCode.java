package com.ziwei.enums;

/**
 * 错误码类（替代 YunDao 框架 ErrorCode）
 *
 * @author JTWORLD
 */
public class ErrorCode {

    private final int code;
    private final String message;

    public ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}

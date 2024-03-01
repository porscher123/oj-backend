package com.wxc.oj.common;

/**
 * 自定义错误码
 */
public enum ErrorCode {

    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "param error"),
    NOT_LOGIN_ERROR(40100, "not login"),
    NO_AUTH_ERROR(40101, "no auth"),
    NOT_FOUND_ERROR(40400, "resource not found"),
    FORBIDDEN_ERROR(40300, "banned to access"),
    SYSTEM_ERROR(50000, "inner error"),
    OPERATION_ERROR(50001, "operation failed");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
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

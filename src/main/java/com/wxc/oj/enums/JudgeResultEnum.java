package com.wxc.oj.enums;


/**
 * 判题结果的枚举值
 */
public enum JudgeResultEnum {
    /**
     * +   Accepted成功
     * +   Wrong Answer答案错误
     * +   Compile Error编译错误
     * +   Memory Limit Exceeded 内存溢出.
     * +   Time Limit Exceeded 超时
     * +   Presentation Error展示错误.
     * +   Output Limit Exceeded输出溢出.
     * +   Waiting等待中
     * +   Dangerous Operation危险操作
     * +   Runtime Error运行错误(用户程序的问题).
     * +   System Error系统错误（做系统人的问题)
     */
    ACCEPTED("ACCEPTED"),
    WRONG_ANSWER("WRONG_ANSWER"),
    COMPILE_ERROR("COMPILE_ERROR"),
    MEMORY_LIMIT_EXCEEDED("MEMORY_LIMIT_EXCEEDED"),
    TIME_LIMIT_EXCEEDED ("TIME_LIMIT_EXCEEDED"),
    PRESENTATION_ERROR("PRESENTATION_ERROR"),
    OUTPUT_LIMIT_EXCEEDED("OUTPUT_LIMIT_EXCEEDED"),
    RUNTIME_ERROR("RUNTIME_ERROR"),
    WAITING("accepted"),
    DANGEROUS_OPERATION("DANGEROUS_OPERATION"),
    SYSTEM_ERROR("SYSTEM_ERROR");

    private String value;
    JudgeResultEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}

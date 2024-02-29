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
    ACCEPTED("accepted"),
    WRONG_ANSWER("accepted"),
    COMPILE_ERROR("accepted"),
    MEMORY_LIMIT_EXCEEDED("accepted"),
    TIME_LIMIT_EXCEEDED ("accepted"),
    PRESENTATION_ERROR("accepted"),
    OUTPUT_LIMIT_EXCEEDED("accepted"),
    RUNTIME_ERROR("accepted"),
    WAITING("accepted"),
    DANGEROUS_OPERATION("accepted"),
    SYSTEM_ERROR("accepted");

    private String value;
    JudgeResultEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}

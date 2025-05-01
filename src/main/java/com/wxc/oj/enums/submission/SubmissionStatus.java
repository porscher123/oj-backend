package com.wxc.oj.enums.submission;

/**
 * @Description 提交评测结果的状态码
 * @Since 2021/1/1
 */
public enum SubmissionStatus {
    // 提交失败,


    SUBMITTED(0, "Submitted"),

    PENDING(1, "Pending"),

    COMPILING(2, "Compiling"),
    COMPILE_ERROR(3, "Compile Error"),
    JUDGING(4, "Judging"),
    ACCEPTED(5, "Accepted"),
    WRONG_ANSWER(6, "Wrong Answer"),
    RUNTIME_ERROR(11, "Runtime Error"),
    SYSTEM_ERROR(12, "System Error");

    private final Integer status;
    private final String description;

    SubmissionStatus(Integer status, String description) {
        this.status = status;
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public static String getDescriptionByStatus(int status) {
        for (SubmissionStatus judge : SubmissionStatus.values()) {
            if (judge.getStatus() == status) {
                return judge.getDescription();
            }
        }
        return "NULL";
    }
}
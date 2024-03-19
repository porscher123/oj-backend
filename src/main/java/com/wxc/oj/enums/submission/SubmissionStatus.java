package com.wxc.oj.enums.submission;

/**
 * @Description 提交评测结果的状态码
 * @Since 2021/1/1
 */
public enum SubmissionStatus {
    // 提交失败,
    NOT_SUBMITTED(-10, "Not Submitted"),
    PENDING(5, "Pending"),
    COMPILING(6, "Compiling"),
    COMPILE_ERROR(-2, "Compile Error"),
    JUDGING(7, "Judging"),
    ACCEPTED(0, "Accepted"),
    WRONG_ANSWER(-1, "Wrong Answer"),
    TIME_LIMIT_EXCEEDED(1, "Time Limit Exceeded"),
    MEMORY_LIMIT_EXCEEDED(2, "Memory Limit Exceeded"),
    RUNTIME_ERROR(3, "Runtime Error"),
    PRESENTATION_ERROR(-3, "Presentation Error"),
    CANCELLED(-4, "Cancelled"),
    SYSTEM_ERROR(4, "System Error"),
    // 正在等待结果,
    PARTIAL_ACCEPTED(8, "Partial Accepted"),
    SUBMITTING(9, "Submitting"),
    SUBMITTED_FAILED(10, "Submitted Failed"),
    NULL(15, "No Status");

    private final Integer status;
    private final String name;

    private SubmissionStatus(Integer status, String name) {
        this.status = status;
        this.name = name;
    }

    public Integer getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public static SubmissionStatus getTypeByStatus(int status) {
        for (SubmissionStatus judge : SubmissionStatus.values()) {
            if (judge.getStatus() == status) {
                return judge;
            }
        }
        return NULL;
    }
}
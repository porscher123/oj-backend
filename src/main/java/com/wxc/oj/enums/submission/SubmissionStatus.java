package com.wxc.oj.enums.submission;

/**
 * @Description 提交评测结果的状态码
 * @Since 2021/1/1
 */
public enum SubmissionStatus {
    // 提交失败,
    NOT_SUBMITTED(1, "Not Submitted"),
    PENDING(2, "Pending"),
    COMPILING(3, "Compiling"),
    COMPILE_ERROR(4, "Compile Error"),
    JUDGING(5, "Judging"),
    SYSTEM_ERROR(6, "System Error"),
    // 正在等待结果,
    PARTIAL_ACCEPTED(7, "Partial Accepted"),
    SUBMITTING(8, "Submitting"),
    SUBMITTED_FAILED(9, "Submitted Failed"),
    JUDGED(10, "Judged");

    private final Integer status;
    private final String value;

    SubmissionStatus(Integer status, String value) {
        this.status = status;
        this.value = value;
    }

    public Integer getStatus() {
        return status;
    }

    public String getValue() {
        return value;
    }

    public static String getTypeByStatus(int status) {
        for (SubmissionStatus judge : SubmissionStatus.values()) {
            if (judge.getStatus() == status) {
                return judge.getValue();
            }
        }
        return "NULL";
    }
}
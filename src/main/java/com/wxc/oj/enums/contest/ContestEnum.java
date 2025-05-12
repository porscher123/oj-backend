package com.wxc.oj.enums.contest;

public enum ContestEnum {
    NOT_STARTED(0, "未开始"),
    RUNNING(1, "进行中"),
    ENDED(2, "已结束");

    private final Integer code;
    private final String description;

    ContestEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}

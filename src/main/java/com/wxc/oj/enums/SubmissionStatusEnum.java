package com.wxc.oj.enums;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据库中提交状态设置的是整形
 * 所以枚举也要保存为整形
 */
public enum SubmissionStatusEnum {
    WAITING("waiting", 0),
    RUNNING("running", 1),
    SUCCEED("succeed", 2),
    FAILED("error", 3);

    private final String text;

    private final Integer value;

    SubmissionStatusEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     * @return
     */
    public static List<Integer> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }
    /**
     * 通过value获取枚举值
     * @param value
     * @return
     */
    public static SubmissionStatusEnum getEnumByValue(Integer value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (var anEnum : SubmissionStatusEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public Integer getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}

package com.wxc.oj.enums.submission;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 可供提交的编程语言枚举
 */
public enum SubmissionLanguageEnum {
    JAVA("java"),
    CPP("cpp"),
    C("c"),
    PYTHON("python");

    private final String value;


    SubmissionLanguageEnum(String value) {
        this.value = value;
    }


    public String getValue() {
        return value;
    }

    /**
     * 获取值列表
     * @return
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

}

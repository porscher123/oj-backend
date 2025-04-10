package com.wxc.oj.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.swagger.v3.oas.models.security.SecurityScheme;
import org.apache.commons.lang3.ObjectUtils;

/**
 * 用户角色枚举
 */
public enum UserRoleEnum {

    USER("user", 0),
    ADMIN("admin", 1),
    BAN("banned", 2);

    private final String text;
    private final Integer value;

    UserRoleEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return
     */
//    public static List<String> getValues() {
//        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
//    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static UserRoleEnum getEnumByValue(Integer value) {
        for (UserRoleEnum anEnum : UserRoleEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
    public String getText() {
        return text;
    }
    public Integer getValue() {
        return value;
    }

//    public String getText() {
//        return text;
//    }
}

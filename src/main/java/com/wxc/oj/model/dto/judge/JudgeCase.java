package com.wxc.oj.model.dto.judge;

import lombok.Data;

/**
 * 测试用例
 * 一个题目对应多个测试用例
 */
@Data
public class JudgeCase {
    private String input;
    private String output;
}

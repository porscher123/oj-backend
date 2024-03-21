package com.wxc.oj.model.judge;

import lombok.Data;

/**
 * 表示一个判题的结果
 * {
 *     "message":"Accepted"，
 *     "time": 10，//单位为ms
 *     "memory": 20，//单位为KB
 * }
 */
@Data
public class JudgeCaseResult {
    /**
     * 这次提交的通过情况
     */
    /**
     * 后续测试点展示
     */
    private String input;
    private String output;
    private String ans;

    private String message;
    /**
     * 这次提交的时间消耗
     */
    private String timeCost;
    /**
     * 使用的内存
     */
    private String memoryUsed;
}

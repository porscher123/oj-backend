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
public class JudgeInfo {
    /**
     * 这次提交的通过情况
     */
    private String message;
    /**
     * 这次提交的时间消耗
     */
    private Long timeCost;
    /**
     * 使用的内存
     */
    private Long memoryUsed;
}

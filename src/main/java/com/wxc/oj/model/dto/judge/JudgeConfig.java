package com.wxc.oj.model.dto.judge;

import lombok.Data;

/**
 * {
 * 	"time limit": 1000ms,
 *  "memory limit": 512MB,
 *   "stack limit": 1000
 * }
 */
@Data
public class JudgeConfig {
    /**
     * 时间限制
     */
    private Integer timeLimit;
    /**
     * 内存限制
     */
    private Integer memoryLimit;
    /**
     * 堆栈限制
     */
    private Integer stackLimit;
}

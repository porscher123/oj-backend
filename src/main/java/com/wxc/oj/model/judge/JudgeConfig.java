package com.wxc.oj.model.judge;

import lombok.Data;

/**
 * {
 * 	"time limit": 1000ms,
 *  "memory limit": 512MB,
 * }
 */
@Data
public class JudgeConfig {
    /**
     * 时间限制
     */
    private Long timeLimit;
    /**
     * 内存限制
     */
    private Long memoryLimit;
}

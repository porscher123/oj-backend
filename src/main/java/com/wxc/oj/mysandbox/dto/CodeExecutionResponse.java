package com.wxc.oj.mysandbox.dto;

import lombok.Data;

@Data
public class CodeExecutionResponse {
    private String output;
    private long executionTime; // 执行时间，单位为毫秒
    private long memoryUsage; // 内存使用量，单位为字节
    private String status; // 执行状态，如 SUCCESS、TIMEOUT、ERROR
}    
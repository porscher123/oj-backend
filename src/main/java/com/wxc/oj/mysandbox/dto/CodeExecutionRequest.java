package com.wxc.oj.mysandbox.dto;

import lombok.Data;

@Data
public class CodeExecutionRequest {
    private String sourceCode;
    private String sampleFileContent;
    private String runtimeEnvironment;
    private String compileCommand;
    private String runCommand;
    private long memoryLimit; // 内存限制，单位为字节
    private long timeLimit; // 时间限制，单位为秒
}    
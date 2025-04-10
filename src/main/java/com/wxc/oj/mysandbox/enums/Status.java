package com.wxc.oj.mysandbox.enums;

// 定义执行状态
public enum Status {
    ACCEPTED,
    MEMORY_LIMIT_EXCEEDED,
    TIME_LIMIT_EXCEEDED,
    OUTPUT_LIMIT_EXCEEDED,
    FILE_ERROR,
    NON_ZERO_EXIT_STATUS,
    SIGNALLED,
    DANGEROUS_SYSCALL,
    INTERNAL_ERROR
}
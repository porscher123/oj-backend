package com.wxc.oj.enums;

/**
 *
 */
public enum JudgeDir {

    /**
     * 可执行程序和用户程序输出的文件夹
     */
    RUN_WORKPLACE_DIR("/data/run"),

    /**
     * 测试样例存放的文件夹
     */
    TEST_CASE_DIR("/data/test_case");


    private final String content;

    JudgeDir(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
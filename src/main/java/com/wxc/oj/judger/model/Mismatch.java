package com.wxc.oj.judger.model;

import lombok.Data;

// 定义一个类来记录不匹配的行信息
@Data
public class Mismatch {
    int lineNumber;
    String userLine;
    String standardLine;

    public Mismatch(int lineNumber, String userLine, String standardLine) {
        this.lineNumber = lineNumber;
        this.userLine = userLine;
        this.standardLine = standardLine;
    }

    @Override
    public String toString() {
        return "Mismatch{" +
                "lineNumber=" + lineNumber +
                ", userLine='" + userLine + '\'' +
                ", standardLine='" + standardLine + '\'' +
                '}';
    }
}
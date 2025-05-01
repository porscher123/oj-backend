package com.wxc.oj.judger.model;

import lombok.Data;

@Data
public class TestCase {
    private int index;
    private String input;
    private String output;
    private int fullScore;
}

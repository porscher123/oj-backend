package com.wxc.oj.enums.problem;

public enum ProblemLevel {
    UNRATED(0, "暂未评级"),
    BEGINNER(1, "入门"),
    EASY(2, "普及"),
    MEDIUM(3, "提高"),
    HARD(4, "省选"),
    ADVANCED(5, "NOI / NOI+");

    private final int value;
    private final String label;

    ProblemLevel(int value, String label) {
        this.value = value;
        this.label = label;
    }

    // Getters
    public int getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    // 根据value查找枚举
    public static Boolean fromValue(int value) {
        for (ProblemLevel level : ProblemLevel.values()) {
            if (level.value == value) {
                return true;
            }
        }
        return false;
    }
}
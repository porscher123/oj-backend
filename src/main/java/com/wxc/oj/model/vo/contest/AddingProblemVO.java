package com.wxc.oj.model.vo.contest;


import lombok.Data;

/**
 * 前端为给比赛添加题目时，查询题目列表时返回的VO
 */
@Data
public class AddingProblemVO {
    private Long problemId;
    private String title;
    private Integer fullScore;
}
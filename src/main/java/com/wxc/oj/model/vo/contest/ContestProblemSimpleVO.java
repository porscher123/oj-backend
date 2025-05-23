package com.wxc.oj.model.vo.contest;

import lombok.Data;

import java.util.Date;

@Data
public class ContestProblemSimpleVO {
    private Long problemId;
    private Integer problemIndex;
    private Integer fullScore;
    private String title;
    private String publisherName;
    private Long publisherId;
    private Date createTime;
    private Integer isPublic;
}
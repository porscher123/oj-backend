package com.wxc.oj.model.vo.problem;

import lombok.Data;

import java.util.List;

@Data
public class ProblemStatisticsVO {
    private Long problemId;
    private Integer submittedCount;
    private List<SubmissionStatusCount> resultDistributions;

    private List<Integer> timeCount;
}

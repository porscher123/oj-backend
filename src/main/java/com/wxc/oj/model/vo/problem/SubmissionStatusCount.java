package com.wxc.oj.model.vo.problem;

import lombok.Data;

@Data
public class SubmissionStatusCount {
    private String status;  // 对应status_description
    private Integer count;  // 对应COUNT(*)
}

package com.wxc.oj.dto.submission;

import lombok.Data;


/**
 * 用户提交代码所需的基础信息
 */
@Data
public class SubmissionAddRequest {

    private Long problemId;

    private String sourceCode;

    private String language;

}

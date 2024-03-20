package com.wxc.oj.model.submission;


import com.wxc.oj.model.judge.JudgeCaseResult;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.Data;

import java.util.List;

/**
 * 整个提交的结果
 * 包含多组样例的结果
 * 和其它信息
 */
@Data
public class SubmissionResult {

    private Integer score;

    private String message;

    private List<JudgeCaseResult> judgeCaseResults;
}

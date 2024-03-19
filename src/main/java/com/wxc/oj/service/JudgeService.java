package com.wxc.oj.service;

import com.wxc.oj.enums.submission.SubmissionStatus;
import com.wxc.oj.model.judge.JudgeCaseResult;
import com.wxc.oj.model.judge.JudgeInfo;
import com.wxc.oj.model.submission.SubmissionResult;
import com.wxc.oj.sandbox.dto.SandBoxRequest;
import com.wxc.oj.sandbox.dto.SandBoxResponse;

import java.io.IOException;
import java.util.List;

public interface JudgeService {
    SubmissionResult doJudge(Long submissionId) throws IOException;

    SandBoxResponse runSandBox(SandBoxRequest request);

    SandBoxResponse compileCode(String sourceCode) throws IOException;

    int getScore(List<JudgeCaseResult> judgeInfos);
}

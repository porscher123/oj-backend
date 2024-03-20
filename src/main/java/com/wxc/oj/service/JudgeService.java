package com.wxc.oj.service;

import com.wxc.oj.model.judge.JudgeCaseResult;
import com.wxc.oj.model.submission.SubmissionResult;
import com.wxc.oj.sandbox.dto.SandBoxResponse;
import com.wxc.oj.sandbox.model.LanguageConfig;

import java.io.IOException;
import java.util.List;

public interface JudgeService {
    SubmissionResult doJudge(Long submissionId) throws IOException;



    SandBoxResponse compileCode(String sourceCode, LanguageConfig languageConfig) throws IOException;

    Integer getScore(List<JudgeCaseResult> judgeInfos);
}

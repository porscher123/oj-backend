package com.wxc.oj.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.wxc.oj.common.ErrorCode;
import com.wxc.oj.constant.LanguageConfigs;
import com.wxc.oj.enums.JudgeResultEnum;
import com.wxc.oj.enums.submission.SubmissionLanguageEnum;
import com.wxc.oj.enums.submission.SubmissionStatus;
import com.wxc.oj.exception.BusinessException;
import com.wxc.oj.model.entity.Problem;
import com.wxc.oj.model.entity.Submission;
import com.wxc.oj.model.judge.JudgeCase;
import com.wxc.oj.model.judge.JudgeCaseResult;
import com.wxc.oj.model.judge.JudgeConfig;
import com.wxc.oj.model.submission.SubmissionResult;
import com.wxc.oj.sandbox.SandboxRun;
import com.wxc.oj.sandbox.dto.Cmd;
import com.wxc.oj.sandbox.dto.SandBoxRequest;
import com.wxc.oj.sandbox.dto.SandBoxResponse;
import com.wxc.oj.sandbox.enums.SandBoxResponseStatus;
import com.wxc.oj.sandbox.model.LanguageConfig;
import com.wxc.oj.service.JudgeService;
import com.wxc.oj.service.ProblemService;
import com.wxc.oj.service.SubmissionService;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * todo:
 *  一个样例是用一个字符串还是一组字符串呢???
 */
@Service
@Slf4j(topic = "✔✔✔✔JudgeServiceImpl✔✔✔✔")
public class JudgeServiceImpl implements JudgeService {


    @Resource
    private SandboxRun sandboxRun;


    @Resource
    private SubmissionService submissionService;

    @Resource
    private ProblemService problemService;

    public static final Long CPU_LIMIT = 10000000000L;
    public static final Long MEMORY_LIMIT = 104857600L;
    public static final Integer PROC_LIMIT = 50;


    public SubmissionResult cppJudge(Submission submission, Problem problem) throws IOException {
        Long submissionId = submission.getId();
        // 更新数据库中的submission的status字段, 以便前端即时查看到submission的状态
        Submission submissionUpd = new Submission();
        submissionUpd.setId(submissionId);
        submissionUpd.setStatus(SubmissionStatus.COMPILING.getStatus());
        submissionService.updateById(submissionUpd);

        // 封装传入代码沙箱的请求
        String sourceCode = submission.getSourceCode();
        SubmissionResult submissionResult = new SubmissionResult();

        byte[] bytes = sourceCode.getBytes();
        int codeLength = bytes.length;
        submissionResult.setCodeLength(codeLength);

        // 编译代码
        SandBoxResponse sandBoxResponse = compileCode(sourceCode, LanguageConfigs.CPP);
        log.info(sandBoxResponse.toString());
        // 获取返回得文件id
        Map<String, String> fileIds = sandBoxResponse.getFileIds();

        if (!sandBoxResponse.getStatus().equals(SandBoxResponseStatus.ACCEPTED.getValue())) {
            log.info("编译失败");
            log.info(sandBoxResponse.getStatus());
            log.info(sandBoxResponse.getError());
            // 返回编译错误
            submissionResult.setStatus(SubmissionStatus.COMPILE_ERROR.getValue());
            submissionResult.setScore(0);
            return submissionResult;
        }
        log.info("编译成功");
        String exeId = fileIds.get("main");
        log.info("可执行文件id = " + exeId);

        String judgeCaseStr = problem.getJudgeCase();
        List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        List<String> inputs = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());
        List<String> outputs = new ArrayList<>();
        List<JudgeCaseResult> judgeCaseResults = new ArrayList<>();
        // 读取判题配置
        String judgeConfigStr = problem.getJudgeConfig();
        JudgeConfig judgeConfig = JSONUtil.toBean(judgeConfigStr, JudgeConfig.class);
        Long totalTime = 0L;
        Long memoryUsed = 0L;

        // 测试每组样例, 获得输出
        for (String input : inputs) {
            SandBoxResponse runResponse = runCode(exeId, input, LanguageConfigs.CPP);
            String status = runResponse.getStatus();
            JudgeCaseResult judgeCaseResult = new JudgeCaseResult();
            judgeCaseResult.setInput(input);
            judgeCaseResult.setOutput(runResponse.getFiles().getStdout());
            Long timeCost = runResponse.getRunTime() / 1000_000;
            totalTime += timeCost;
            memoryUsed = runResponse.getMemory();
            if (memoryUsed / 1024 / 1024 == 0) {
                log.info("mem : " + memoryUsed + "KB");
                judgeCaseResult.setMemoryUsed(memoryUsed / 1024  + "KB");
            } else {
                judgeCaseResult.setMemoryUsed(memoryUsed / 1024 / 1024 + "MB");
            }
            log.info("time : " + timeCost + "ms");
            log.info("mem : " + memoryUsed + "MB");
            judgeCaseResult.setTimeCost(timeCost + "ms");
            judgeCaseResult.setMessage(JudgeResultEnum.ACCEPTED.getValue());
            // 执行成功
            if (status.equals(SandBoxResponseStatus.ACCEPTED.getValue())) {
                outputs.add(runResponse.getFiles().getStdout());
                // 判断超时
                if (timeCost > judgeConfig.getTimeLimit()) {
                    judgeCaseResult.setMessage(JudgeResultEnum.TIME_LIMIT_EXCEEDED.getValue());
                }
                // 判断超内存
                if (memoryUsed / 1024 / 1024 > judgeConfig.getMemoryLimit()) {
                    judgeCaseResult.setMessage(JudgeResultEnum.MEMORY_LIMIT_EXCEEDED.getValue());
                }
            } else {
                judgeCaseResult.setMessage(JudgeResultEnum.WRONG_ANSWER.getValue());
            }
            judgeCaseResults.add(judgeCaseResult);
        }
        // 设置程序的总运行
        submissionResult.setMemoryUsed(memoryUsed);
        submissionResult.setTotalTime(totalTime);

        log.info("inputs: "+ inputs);
        log.info("outputs: " + outputs);
        // 获取答案样例输出
        List<String> answerOutputs = judgeCaseList.stream().map(JudgeCase::getOutput).collect(Collectors.toList());
        log.info("ans: " + answerOutputs);
        // 判断输出样例和答案对比
        for (int i = 0; i < answerOutputs.size(); i++) {
            JudgeCaseResult judgeCaseResult1 = judgeCaseResults.get(i);
            judgeCaseResult1.setAns(answerOutputs.get(i));
            // 出去首位空格
            outputs.get(i).trim();
            if (!answerOutputs.get(i).equals(outputs.get(i))) {
                JudgeCaseResult judgeCaseResult = judgeCaseResults.get(i);
                judgeCaseResult.setMessage(JudgeResultEnum.WRONG_ANSWER.getValue());
            }
        }



        // 计算得分
        int score = getScore(judgeCaseResults);

        if (exeId != null) {
            sandboxRun.delFile(exeId);
        }
        submissionResult.setScore(score);
        submissionResult.setJudgeCaseResults(judgeCaseResults);

        // 判题结束后, 修改数据库中的submission的信息
        submissionUpd.setId(submissionId);
        submissionUpd.setStatus(SubmissionStatus.JUDGED.getStatus());
        submissionUpd.setSubmissionResult(JSONUtil.toJsonStr(submissionResult));

        if (score == 100) {
            submissionResult.setStatus(JudgeResultEnum.ACCEPTED.getValue());
        }
        boolean updated = submissionService.updateById(submissionUpd);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "submission更新失败");
        }
        return submissionResult;
    }

    /**
     * @param submissionId
     * @return 返回每个测试用例的判题信息
     * 根据不同得语言选择不同得判题逻辑
     */
    @Override
    public SubmissionResult doJudge(Long submissionId) throws IOException {
        Submission submission = submissionService.getById(submissionId);
        // 获取提交
        if (submission == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交不存在");
        }
        // 获取题目信息
        Long problemId = submission.getProblemId();
        Problem problem = problemService.getById(problemId);
        if (problem == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        // 获取语言信息, todo: 后续会根据语言执行不同参数的运行
        String language = submission.getLanguage();



        if (language.equals(SubmissionLanguageEnum.CPP.getValue())) {
            return cppJudge(submission, problem);
        } else if (language.equals(SubmissionLanguageEnum.CPP.getValue())) {

        } else if (language.equals(SubmissionLanguageEnum.CPP.getValue())) {

        } else if (language.equals(SubmissionLanguageEnum.CPP.getValue())) {

        }
        SubmissionResult submissionResult = new SubmissionResult();
        submissionResult.setStatus("编程语言不支持");
        return submissionResult;
    }



    /**
     * 运行代码
     * 一次运行结束后不删除exe文件
     * 等待调用方测试完多组数据后删除
     * @param fileId
     * @return
     */
    public SandBoxResponse runCode(String fileId, String input, LanguageConfig languageConfig) {
        Cmd cmd = new Cmd();
        // args
        List<String> args = languageConfig.getExeArgs();
        cmd.setArgs(args);
        // envs
        List<String> envs = languageConfig.getEnvs();
        cmd.setEnv(envs);
        // files
        JSONArray files = new JSONArray();
        files.add(new JSONObject().set("content", input));
        files.add(new JSONObject().set("name","stdout").set("max", 10240));
        files.add(new JSONObject().set("name","stderr").set("max", 10240));
        cmd.setFiles(files);
        // limit
        cmd.setCpuLimit(CPU_LIMIT);
        cmd.setMemoryLimit(MEMORY_LIMIT);
        cmd.setProcLimit(PROC_LIMIT);
        // copyIn
        JSONObject copyIn = new JSONObject();
        copyIn.set(languageConfig.getExeFileName(),new JSONObject().set("fileId", fileId));
        cmd.setCopyIn(copyIn);

        SandBoxRequest sandBoxRequest = new SandBoxRequest();

        List<Cmd> cmds = Arrays.asList(cmd);
        sandBoxRequest.setCmd(cmds);

        SandBoxResponse response = sandboxRun.run(sandBoxRequest);
        String status = response.getStatus();
        // 执行成功
        if (status.equals(SandBoxResponseStatus.ACCEPTED.getValue())) {
            log.info("执行成功");
            String stdout = response.getFiles().getStdout();
            log.info("代码输出 = " + stdout);
        } else {
            log.info("运行失败");
            log.info(response.getError());
        }
        return response;
    }

    /**
     * 编译代码
     * @return
     */
    @Override
    public SandBoxResponse compileCode(String sourceCode, LanguageConfig languageConfig) throws IOException {
        Cmd cmd = new Cmd();
        // args
        List<String> args = languageConfig.getCmpArgs();
        cmd.setArgs(args);
        // envs
        List<String> envs = languageConfig.getEnvs();
        cmd.setEnv(envs);
        // files
        JSONArray files = new JSONArray();
        files.add(new JSONObject().set("content",""));
        files.add(new JSONObject().set("name","stdout").set("max", 10240));
        files.add(new JSONObject().set("name","stderr").set("max", 10240));
        cmd.setFiles(files);
        // limit
        cmd.setCpuLimit(CPU_LIMIT);
        cmd.setMemoryLimit(MEMORY_LIMIT);
        cmd.setProcLimit(PROC_LIMIT);
        // copyOut
        List<String> copyOut = Arrays.asList("stdout", "stderr");
        cmd.setCopyOut(copyOut);
        // copyOutCached
        List<String> copyOutCached = languageConfig.getExeArgs(); // ❗❗
        cmd.setCopyOutCached(copyOutCached);
        // copyIn
        JSONObject copyIn = new JSONObject();
        copyIn.set(languageConfig.getSourceFileName(), new JSONObject().set("content", sourceCode));
        cmd.setCopyIn(copyIn);
        SandBoxRequest sandBoxRequest = new SandBoxRequest();

        List<Cmd> cmds = Arrays.asList(cmd);
        sandBoxRequest.setCmd(cmds);

        // 调用sandboxRun编译
        SandBoxResponse response = sandboxRun.compile(sandBoxRequest);
        log.info(response.toString());
        if (response.getStatus().equals(SandBoxResponseStatus.ACCEPTED.getValue())) {
            return response;
        }
        log.info(response.getStatus());
        log.info(response.getError());
        return response;
    }


    /**
     * 根据样例得通过情况, 计算这次submission的得分
     * @param judgeCaseResults
     * @return
     */
    @Override
    public Integer getScore(List<JudgeCaseResult> judgeCaseResults) {
        int total = judgeCaseResults.size();
        int accepted = 0;
        for (var judgeCaseResult : judgeCaseResults) {
            if (judgeCaseResult.getMessage().equals(JudgeResultEnum.ACCEPTED.getValue())) {
                accepted++;
            }
        }
        log.info("total " + total);
        log.info("ac tests  " + accepted);
        if (total == 0) {
            return 0;
        }
        int score = accepted * 100 / total ;
        return score;
    }
}

package com.wxc.oj.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.wxc.oj.common.ErrorCode;
import com.wxc.oj.enums.JudgeResultEnum;
import com.wxc.oj.enums.submission.SubmissionStatus;
import com.wxc.oj.exception.BusinessException;
import com.wxc.oj.model.entity.Problem;
import com.wxc.oj.model.entity.Submission;
import com.wxc.oj.model.judge.JudgeCase;
import com.wxc.oj.model.judge.JudgeCaseResult;
import com.wxc.oj.model.judge.JudgeConfig;
import com.wxc.oj.model.judge.JudgeInfo;
import com.wxc.oj.model.submission.SubmissionResult;
import com.wxc.oj.sandbox.SandboxRun;
import com.wxc.oj.sandbox.dto.Cmd;
import com.wxc.oj.sandbox.dto.SandBoxRequest;
import com.wxc.oj.sandbox.dto.SandBoxResponse;
import com.wxc.oj.sandbox.enums.SandBoxResponseStatus;
import com.wxc.oj.service.JudgeService;
import com.wxc.oj.service.ProblemService;
import com.wxc.oj.service.SubmissionService;
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
@Slf4j(topic = "✔✔✔✔✔✔")
public class JudgeServiceImpl implements JudgeService {


    @Resource
    private SandboxRun sandboxRun;


    @Resource
    private SubmissionService submissionService;


    @Resource
    private ProblemService problemService;


    /**
     * @param submissionId
     * @return 返回每个测试用例的判题信息
     */
    @Override
    public SubmissionResult doJudge(Long submissionId) throws IOException {
        // 获取提交
        Submission submission = submissionService.getById(submissionId);
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

        // 更新数据库中的submission的status字段, 以便前端即时查看到submission的状态
        Submission submissionUpd = new Submission();
        submissionUpd.setId(submissionId);
        submissionUpd.setStatus(SubmissionStatus.COMPILING.getStatus());
        submissionService.updateById(submissionUpd);

        // 封装传入代码沙箱的请求
        String sourceCode = submission.getSourceCode();

        // 编译代码
        SandBoxResponse sandBoxResponse = compileCode(sourceCode);
        Map<String, String> fileIds = sandBoxResponse.getFileIds();
        String exeId = fileIds.get("main");
        log.info("可执行文件id = " + exeId);
        if (!sandBoxResponse.getStatus().equals(SandBoxResponseStatus.ACCEPTED.getValue())) {
            log.info("编译失败");
            log.info(sandBoxResponse.getStatus());
            log.info(sandBoxResponse.getError());
            // 删除可执行文件
            if (exeId != null) {
                sandboxRun.delFile(exeId);
            }
            // 返回编译错误
            SubmissionResult submissionResult = new SubmissionResult();
            submissionResult.setMessage(SubmissionStatus.COMPILE_ERROR.getName());
            return submissionResult;
        }
        log.info("编译成功");

        String judgeCaseStr = problem.getJudgeCase();
        List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        List<String> inputs = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());
        List<String> outputs = new ArrayList<>();
        List<JudgeCaseResult> judgeCaseResults = new ArrayList<>();
        // 读取判题配置
        String judgeConfigStr = problem.getJudgeConfig();
        JudgeConfig judgeConfig = JSONUtil.toBean(judgeConfigStr, JudgeConfig.class);
        // 测试每组样例, 获得输出
        for (String input : inputs) {
            SandBoxResponse runResponse = runCode(exeId, input);
            String status = runResponse.getStatus();
            JudgeCaseResult judgeCaseResult = new JudgeCaseResult();
            // 执行成功
            if (status.equals(SandBoxResponseStatus.ACCEPTED.getValue())) {
                outputs.add(runResponse.getFiles().getStdout());
                // 判断超时
                if (runResponse.getRunTime() >= judgeConfig.getTimeLimit()) {
                    judgeCaseResult.setMessage(JudgeResultEnum.TIME_LIMIT_EXCEEDED.getValue());
                }
                // 判断超内存
                if (runResponse.getMemory() >= judgeConfig.getMemoryLimit()) {
                    judgeCaseResult.setMessage(JudgeResultEnum.MEMORY_LIMIT_EXCEEDED.getValue());
                }
            } else if (status.equals(SandBoxResponseStatus.NON_ZERO_ERROR)) {
                judgeCaseResult.setMessage(JudgeResultEnum.RUNTIME_ERROR.getValue());
            } else if (status.equals(SandBoxResponseStatus.MEMORY_LIMIT_EXCEEDED)) {
                judgeCaseResult.setMemoryUsed(runResponse.getMemory());
            } else if (status.equals(SandBoxResponseStatus.OUTPUT_LIMIT_EXCEEDED)) {
                judgeCaseResult.setMessage(JudgeResultEnum.OUTPUT_LIMIT_EXCEEDED.getValue());
            }
            judgeCaseResults.add(judgeCaseResult);
        }







        // 获取答案样例输出
        List<String> answerOutputs = judgeCaseList.stream().map(JudgeCase::getOutput).collect(Collectors.toList());
        // 判断输出样例和答案对比
        for (int i = 0; i < answerOutputs.size(); i++) {
            // 出去首位空格
            outputs.get(i).trim();
            if (!answerOutputs.get(i).equals(outputs.get(i))) {
                JudgeCaseResult judgeCaseResult = judgeCaseResults.get(i);
                judgeCaseResult.setMessage(JudgeResultEnum.WRONG_ANSWER.getValue());
            }
        }
        // 计算得分
        int score = getScore(judgeCaseResults);
        //




        // 判题结束后, 修改数据库中的submission的信息
        submissionUpd.setId(submissionId);
        submissionUpd.setStatus(SubmissionStatusEnum.SUCCEED.getValue());
        submissionUpd.setJudgeInfo(JSONUtil.toJsonStr(judgeInfos));
        boolean updated = submissionService.updateById(submissionUpd);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目更新失败");
        }
        return null;
    }

    @Override
    public SandBoxResponse runSandBox(SandBoxRequest request) {
        return null;
    }


    /**
     * 运行代码
     * @param fileId
     * @return
     */
    public SandBoxResponse runCode(String fileId, String input) {
        Cmd cmd = new Cmd();
        // args
        List<String> args = Arrays.asList("main");
        cmd.setArgs(args);
        // envs
        List<String> envs = Arrays.asList("PATH=/usr/bin:/bin");
        cmd.setEnv(envs);
        // files
        JSONArray files = new JSONArray();
        files.add(new JSONObject().set("content", input));
        files.add(new JSONObject().set("name","stdout").set("max", 10240));
        files.add(new JSONObject().set("name","stderr").set("max", 10240));
        cmd.setFiles(files);
        // limit
        cmd.setCpuLimit(10000000000L);
        cmd.setMemoryLimit(104857600L);
        cmd.setProcLimit(50);
        // copyIn
        JSONObject copyIn = new JSONObject();
        copyIn.set("main",new JSONObject().set("fileId", fileId));
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
        sandboxRun.delFile(fileId);
        return response;
    }

    /**
     * 编译代码
     * @return
     */
    @Override
    public SandBoxResponse compileCode(String sourceCode) throws IOException {
        Cmd cmd = new Cmd();
        // args
        List<String> args = Arrays.asList("/usr/bin/g++", "main.cpp", "-o", "main");
        cmd.setArgs(args);
        // envs
        List<String> envs = Arrays.asList("PATH=/usr/bin:/bin");
        cmd.setEnv(envs);
        // files
        JSONArray files = new JSONArray();
        files.add(new JSONObject().set("content",""));
        files.add(new JSONObject().set("name","stdout").set("max", 10240));
        files.add(new JSONObject().set("name","stderr").set("max", 10240));
        cmd.setFiles(files);
        // limit
        cmd.setCpuLimit(10000000000L);
        cmd.setMemoryLimit(104857600L);
        cmd.setProcLimit(50);
        // copyOut
        List<String> copyOut = Arrays.asList("stdout", "stderr");
        cmd.setCopyOut(copyOut);
        // copyOutCached
        List<String> copyOutCached = Arrays.asList("main");
        cmd.setCopyOutCached(copyOutCached);
        // copyIn
        JSONObject copyIn = new JSONObject();
        copyIn.set("main.cpp",new JSONObject().set("content", sourceCode));
        cmd.setCopyIn(copyIn);
        SandBoxRequest sandBoxRequest = new SandBoxRequest();

        List<Cmd> cmds = Arrays.asList(cmd);
        sandBoxRequest.setCmd(cmds);

        // 调用sandboxRun编译
        SandBoxResponse response = sandboxRun.compile(sandBoxRequest);
        Map<String, String> fileIds = response.getFileIds();
        String exeId = fileIds.get("main");
        log.info("可执行文件id = " + exeId);
        if (response.getStatus().equals(SandBoxResponseStatus.ACCEPTED.getValue())) {
            log.info("编译成功");
            SandBoxResponse sandBoxResponse = runCode(exeId, "1 2");
            return sandBoxResponse;
        } else {
            log.info("编译失败");
            log.info(response.getStatus());
            log.info(response.getError());
            sandboxRun.delFile(exeId);
        }
        return response;
    }


    /**
     * 根据样例得通过情况, 计算这次submission的得分
     * @param judgeCaseResults
     * @return
     */
    @Override
    public int getScore(List<JudgeCaseResult> judgeCaseResults) {
        int total = judgeCaseResults.size();
        int accepted = 0;
        for (var judgeCaseResult : judgeCaseResults) {
            if (judgeCaseResult.getMessage().equals(JudgeResultEnum.ACCEPTED)) {
                accepted++;
            }
        }
        int score = accepted / total;
        return score;
    }
}

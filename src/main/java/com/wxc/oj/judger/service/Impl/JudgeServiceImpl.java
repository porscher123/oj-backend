package com.wxc.oj.judger.service.Impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.wxc.oj.common.ErrorCode;
import com.wxc.oj.constant.LanguageConfigs;
import com.wxc.oj.enums.JudgeResultEnum;
import com.wxc.oj.enums.submission.SubmissionLanguageEnum;
import com.wxc.oj.enums.submission.SubmissionStatus;
import com.wxc.oj.exception.BusinessException;
import com.wxc.oj.judger.model.TestCase;
import com.wxc.oj.judger.model.TestCases;
import com.wxc.oj.judger.service.JudgeService;
import com.wxc.oj.queueMessage.SubmissionMessage;
import com.wxc.oj.model.entity.Problem;
import com.wxc.oj.model.entity.Submission;
import com.wxc.oj.model.judge.JudgeCaseResult;
import com.wxc.oj.model.judge.JudgeConfig;
import com.wxc.oj.model.submission.SubmissionResult;
import com.wxc.oj.sandbox.SandboxRun;
import com.wxc.oj.sandbox.dto.Cmd;
import com.wxc.oj.sandbox.dto.SandBoxRequest;
import com.wxc.oj.sandbox.dto.SandBoxResponse;
import com.wxc.oj.sandbox.enums.SandBoxResponseStatus;
import com.wxc.oj.sandbox.model.LanguageConfig;
import com.wxc.oj.service.ProblemService;
import com.wxc.oj.service.SubmissionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * todo:
 *  一个样例是用一个字符串还是一组字符串呢???
 *  {
 *   "problemId": 1763440748296044545,
 *   "sourceCode": "",
 *   "language": "cpp"
 * }
 */
@Service
@Slf4j(topic = "✔✔✔✔JudgeServiceImpl✔✔✔✔")
public class JudgeServiceImpl implements JudgeService {

    /**
     * 用于访问resources/data/xxx/
     */


    @Resource
    private SandboxRun sandboxRun;


    @Resource
    private SubmissionService submissionService;

    @Resource
    private ProblemService problemService;

    /**
     * 时间限制10s
     */
    public static final Long CPU_LIMIT = 10000000000L;
    /**
     * 内存限制512MB
     */
    public static final Long MEMORY_LIMIT = 536870912L;

    public static final String QUEUE = "submission";
    public static final String DATA_PATH = "F:\\oj\\oj-backend\\src\\main\\resources\\data";
    public static final Integer PROC_LIMIT = 50;

    @RabbitListener(queues = QUEUE, messageConverter = "jacksonConverter")
    public void listenSubmission(SubmissionMessage message) throws IOException {
        Long id = message.getId();
        log.info("🔆🔆🔆🔆🔆接收到的id: " + id);
        doJudge(id);
    }




    /**
     * 编译CPP文件, 返回代码沙箱保存的可执行文件的ID
     * @param sourceCode
     * @return
     * @throws IOException
     */
    public String compileCppFile(String sourceCode) throws IOException {
        SandBoxResponse sandBoxResponse = compileCode(sourceCode, LanguageConfigs.CPP);
        log.info(sandBoxResponse.toString());
        // 获取返回得文件id
        Map<String, String> fileIds = sandBoxResponse.getFileIds();
        if (!sandBoxResponse.getStatus().equals(SandBoxResponseStatus.ACCEPTED.getValue())) {
            log.info("❗❗❗编译失败❗❗❗");
//            log.info(sandBoxResponse.getStatus());
//            log.info(sandBoxResponse.getError());
            return null;
        }
        log.info("编译成功");
        String exeId = fileIds.get("main");
//        log.info("可执行文件id = " + exeId);
        return exeId;
    }


    /**
     * cppJudge
     * @param submission
     * @param problem
     * @throws IOException
     */
    public void cppJudge(Submission submission, Problem problem) throws IOException {
        Long pid = problem.getId();

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

        String exeId = compileCppFile(sourceCode);
        if (exeId == null) {
            // 返回编译错误
            submissionResult.setStatus(SubmissionStatus.COMPILE_ERROR.getValue());
            submissionUpd.setSubmissionResult(JSONUtil.toJsonStr(submissionResult));
            boolean updated = submissionService.updateById(submissionUpd);
            if (!updated) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "submission更新失败");
            }
            return;
        }


        // 编译代码
//        SandBoxResponse sandBoxResponse = compileCode(sourceCode, LanguageConfigs.CPP);
//        log.info(sandBoxResponse.toString());
//
//        // 获取返回得文件id
//        Map<String, String> fileIds = sandBoxResponse.getFileIds();
//
//        if (!sandBoxResponse.getStatus().equals(SandBoxResponseStatus.ACCEPTED.getValue())) {
//            log.info("❗❗❗编译失败❗❗❗");
//            log.info(sandBoxResponse.getStatus());
//            log.info(sandBoxResponse.getError());
//            // 返回编译错误
//            submissionResult.setStatus(SubmissionStatus.COMPILE_ERROR.getValue());
//            submissionUpd.setSubmissionResult(JSONUtil.toJsonStr(submissionResult));
//            boolean updated = submissionService.updateById(submissionUpd);
//            if (!updated) {
//                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "submission更新失败");
//            }
//            return;
//        }
//        log.info("编译成功");
//        String exeId = fileIds.get("main");
//        log.info("可执行文件id = " + exeId);

//        String judgeCaseStr = problem.getJudgeCase();
//        List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
//        List<String> inputs = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());
//        List<String> outputs = new ArrayList<>();

        List<JudgeCaseResult> judgeCaseResults = new ArrayList<>();
        // 读取判题配置
        String judgeConfigStr = problem.getJudgeConfig();
        JudgeConfig judgeConfig = JSONUtil.toBean(judgeConfigStr, JudgeConfig.class);
        Long totalTime = 0L;
        Long memoryUsed = 0L;
        // 3.17
        // 将config.json转为TestCases
        String filePath = DATA_PATH + File.separator + pid + File.separator + "config.json";
        String jsonStr = FileUtil.readUtf8String(filePath);
        TestCases testCases = JSONUtil.toBean(jsonStr, TestCases.class);
        List<TestCase> testCaseList = testCases.getCases();

        // 计算得分
        int accepptCase = 0;
        int totalCase = testCaseList.size();

        for (TestCase testCase : testCaseList) {
            // 获取第index个测试样例的输入文件, 并转化为字符串
            int index = testCase.getIndex();
            String inputFile = DATA_PATH +File.separator + pid + File.separator + index + ".in";
            System.out.println("📍📍📍inputFile = " + inputFile);
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }
            String input = content.toString();
            System.out.println("🚛🚛🚛🚛🚛🚛🚛🚛input = " + input);

            // 运行第index个测试样例
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
                // 获取输出文件.ans
                String output = runResponse.getFiles().getStdout();
                FileWriter fileWriter = new FileWriter(DATA_PATH + File.separator + pid + File.separator + index + ".ans");
                fileWriter.write(output);
                fileWriter.flush();
                // 比较.ans和.out文件
                boolean accepted = checker(pid, index);
                // 删除临时用于比对的.ans文件
                deleteDotAnsFile(pid, index);
                // 根据.out和.ans文件的比对结果, 更新judgeCaseResult
                if (accepted) {
                    judgeCaseResult.setMessage(JudgeResultEnum.ACCEPTED.getValue());
                    accepptCase++;
                } else {
                    judgeCaseResult.setMessage(JudgeResultEnum.WRONG_ANSWER.getValue());
                }

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


        // 运行完样例后, 每组样例的运行时间, 内存占用都保存在judgeCaseResult
        // 样例输出文件, 保存在data/xxx/1.ans中
        // 比对data/xxx/中 1.out和1.ans的内容
        // 设置程序的总运行
        submissionResult.setMemoryUsed(memoryUsed);
        submissionResult.setTotalTime(totalTime);

        // 根据AC样例数与总样例数, 计算分数
        int score = accepptCase * 100 / totalCase;

        // 删除沙箱服务中保存的文件
        if (exeId != null) {
            sandboxRun.delFile(exeId);
        }

        submissionResult.setScore(score);
        // 提交结果中包含所有测试样例的测试结果
        submissionResult.setJudgeCaseResults(judgeCaseResults);
        // 判题结束后, 修改数据库中的submission的信息
        submissionUpd.setId(submissionId);
        submissionUpd.setStatus(SubmissionStatus.JUDGED.getStatus());
        submissionUpd.setSubmissionResult(JSONUtil.toJsonStr(submissionResult));

        // 更新submission表
        boolean updated = submissionService.updateById(submissionUpd);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "submission更新失败");
        }
    }

    /**
     * 删除为了比对生成的临时文件index.ans
     * @param pid
     * @param index
     * @return
     */
    public boolean deleteDotAnsFile(Long pid, int index) {
        String filePath = DATA_PATH + File.separator + pid + File.separator + index + ".ans";
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
            return true;
        }
        return false;
    }
    public boolean checker(Long pid, int index) throws IOException {
        String stdoutFilePath = DATA_PATH + File.separator + pid + File.separator + index + ".out";
        String useroutFilePath = DATA_PATH + File.separator + pid + File.separator + index + ".ans";
        BufferedReader br1 = new BufferedReader(new FileReader(stdoutFilePath));
        BufferedReader br2 = new BufferedReader(new FileReader(useroutFilePath));
        String line1, line2;
        int lineNumber = 0;
        while ((line1 = br1.readLine()) != null && (line2 = br2.readLine()) != null) {
            lineNumber++;
            // 移除行首和行尾空格
            line1 = line1.trim();
            line2 = line2.trim();

            if (!line1.equals(line2)) {
                return false;
            }
        }
        // 检查是否有剩余的行
        if (br1.readLine() != null || br2.readLine() != null) {
            return false;
        }
        return true;

    }
    /**
     * @param submissionId
     * @return 返回每个测试用例的判题信息
     * 根据不同得语言选择不同得判题逻辑
     */
    @Override
    public void doJudge(Long submissionId) throws IOException {
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
            cppJudge(submission, problem);
        } else if (language.equals(SubmissionLanguageEnum.CPP.getValue())) {

        } else if (language.equals(SubmissionLanguageEnum.CPP.getValue())) {

        } else if (language.equals(SubmissionLanguageEnum.CPP.getValue())) {

        }
        SubmissionResult submissionResult = new SubmissionResult();
        submissionResult.setStatus("编程语言不支持");
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
        // 这里的JSON使用了content字段, 即使用了MemoryFile, 直接指定输入文件的内容
        // interface MemoryFile {
        //    content: string | Buffer; // 文件内容
        //}
        // 其实也可以指定本地的路径, 例如data/1763440748296044545/.in
        // interface LocalFile {
        //    src: string; // 文件绝对路径
        //}
        // 也可以指定到上传到服务器的文件id
        // 这种得提前上传文件, 然后再指定文件id
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
        files.add(new JSONObject().set("name","stdout").set("max", 64 * 1024 * 1024));
        files.add(new JSONObject().set("name","stderr").set("max", 64 * 1024 * 1024));
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
        cmd.setStrictMemoryLimit(true);
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

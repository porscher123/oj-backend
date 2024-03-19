package com.wxc.oj.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxc.oj.annotation.AuthCheck;
import com.wxc.oj.common.BaseResponse;
import com.wxc.oj.common.ErrorCode;
import com.wxc.oj.common.ResultUtils;
import com.wxc.oj.constant.UserConstant;
import com.wxc.oj.exception.BusinessException;
import com.wxc.oj.model.dto.problem.ProblemQueryRequest;
import com.wxc.oj.model.dto.submission.SubmissionAddRequest;
import com.wxc.oj.model.dto.submission.SubmissionQueryDTO;
import com.wxc.oj.model.entity.Problem;
import com.wxc.oj.model.entity.Submission;
import com.wxc.oj.model.entity.User;
import com.wxc.oj.model.judge.JudgeInfo;
import com.wxc.oj.model.submission.SubmissionResult;
import com.wxc.oj.model.vo.SubmissionVO;
import com.wxc.oj.service.JudgeService;
import com.wxc.oj.service.SubmissionService;
import com.wxc.oj.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("submission")
@Slf4j(topic = "SubmissionController")
public class SubmissionController {


    @Resource
    private SubmissionService submissionService;


    @Resource
    private UserService userService;


    @Resource
    private JudgeService judgeService;


    /**
     * 提交题目
     * @param request
     * @return 提交的submission id
     */
    @PostMapping("submit")
    public BaseResponse doSubmit(@RequestBody SubmissionAddRequest submissionAddRequest, HttpServletRequest request) throws IOException {
        if (submissionAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取当前用户
        User loginUser = userService.getLoginUser(request);
        // 执行插入submission操作
        Submission submission = submissionService.submitCode(submissionAddRequest, loginUser);
        // todo: 执行判题服务
        SubmissionResult submissionResult = judgeService.doJudge(submission.getId());
        return ResultUtils.success(submissionResult);
    }

    /**
     * 分页获取submission
     */
    @PostMapping("/list/page")
    public BaseResponse listSubmissionByPage(@RequestBody SubmissionQueryDTO submissionQueryDTO) {
        long current = submissionQueryDTO.getCurrent();
        long size = submissionQueryDTO.getPageSize();
        Page<Submission> submissionPage = submissionService.page(new Page<>(current, size),
                submissionService.getQueryWrapper(submissionQueryDTO));
        Page<SubmissionVO> submissionVOPage = submissionService.getSubmissionVOPage(submissionPage);
        return ResultUtils.success(submissionVOPage);
    }
}

package com.wxc.oj.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxc.oj.common.BaseResponse;
import com.wxc.oj.common.ErrorCode;
import com.wxc.oj.common.ResultUtils;
import com.wxc.oj.exception.BusinessException;
import com.wxc.oj.model.dto.submission.SubmissionAddRequest;
import com.wxc.oj.model.dto.submission.SubmissionQueryDTO;
import com.wxc.oj.model.po.Submission;
import com.wxc.oj.model.po.User;
import com.wxc.oj.model.vo.SubmissionVO;
import com.wxc.oj.service.SubmissionService;
import com.wxc.oj.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("submission")
@Slf4j(topic = "😊😊😊😊")
public class SubmissionController {


    @Resource
    private SubmissionService submissionService;


    @Resource
    private UserService userService;




    /**
     * 提交题目
     * @param request
     * @return 提交的submission id
     */
    @PostMapping("submit")
    public BaseResponse<SubmissionVO> doSubmit(@RequestBody SubmissionAddRequest submissionAddRequest,
                                               HttpServletRequest request) throws IOException {
        if (submissionAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取当前用户
        User loginUser = userService.getLoginUser(request);
        // 执行插入submission操作
        SubmissionVO submission = submissionService.submitCode(submissionAddRequest, loginUser);
        return ResultUtils.success(submission);
    }

    /**
     * 分页获取submission
     * 按CreateTime降序排序
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<SubmissionVO>> listSubmissionByPage(@RequestBody
                                                         SubmissionQueryDTO submissionQueryDTO) {
        long current = submissionQueryDTO.getCurrent();
        long size = submissionQueryDTO.getPageSize();
        LambdaQueryWrapper<Submission> queryWrapper
                = submissionService.getQueryWrapper(submissionQueryDTO);
        queryWrapper.orderByDesc(Submission::getCreateTime);
        Page<Submission> submissionPage = submissionService.page(new Page<>(current, size), queryWrapper);
        Page<SubmissionVO> submissionVOPage = submissionService.getSubmissionVOPage(submissionPage);
        return ResultUtils.success(submissionVOPage);
    }
    /**
     * 分页获取特定用户的submission
     */
    @GetMapping("/user/page")
    public BaseResponse getSubmissionPageByUserId(Long userId, Long problemId) {
        LambdaQueryWrapper<Submission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Submission::getUserId, userId)
                .eq(Submission::getProblemId, problemId);
        List<Submission> list = submissionService.list(queryWrapper);
        return ResultUtils.success(list);
    }
    @GetMapping("/get")
    public BaseResponse getSubmission(Long id) {
        Submission byId = submissionService.getById(id);
        SubmissionVO submissionVO = submissionService.submissionToVO(byId);
        return ResultUtils.success(submissionVO);
    }
}

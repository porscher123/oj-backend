package com.wxc.oj.controller;


import com.wxc.oj.common.BaseResponse;
import com.wxc.oj.common.ErrorCode;
import com.wxc.oj.common.ResultUtils;
import com.wxc.oj.exception.BusinessException;
import com.wxc.oj.model.dto.submission.SubmissionAddRequest;
import com.wxc.oj.model.entity.User;
import com.wxc.oj.service.SubmissionService;
import com.wxc.oj.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("submission")
@Slf4j
public class SubmissionController {


    @Autowired
    private SubmissionService submissionService;


    @Autowired
    private UserService userService;


    /**
     *
     * @param submission
     * @param request
     * @return 提交的submission id
     */
    @PostMapping("submit")
    public BaseResponse<Long> doSubmit(SubmissionAddRequest submission, HttpServletRequest request) {
        if (submission == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 获取当前用户
        User loginUser = userService.getLoginUser(request.getHeader("token"));
        // 执行插入submission操作
        Long newId = submissionService.submitCode(submission, loginUser);

        return ResultUtils.success(newId);
    }
}

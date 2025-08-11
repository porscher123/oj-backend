package com.wxc.oj.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxc.oj.common.BaseResponse;
import com.wxc.oj.common.ErrorCode;
import com.wxc.oj.common.ResultUtils;
import com.wxc.oj.exception.BusinessException;
import com.wxc.oj.model.dto.submission.SubmissionAddRequest;
import com.wxc.oj.model.dto.submission.SubmissionQueryDTO;
import com.wxc.oj.model.po.Submission;
import com.wxc.oj.model.vo.problem.ProblemStatisticsVO;
import com.wxc.oj.model.vo.problem.SubmissionVO;
import com.wxc.oj.service.SubmissionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("submission")
@Slf4j(topic = "😊😊😊😊")
public class SubmissionController {


    @Resource
    private SubmissionService submissionService;

    /**
     * 提交题目
     * @param
     * @return 提交的submission id
     */
    @PostMapping("submit")
    public BaseResponse<SubmissionVO> doSubmit(@RequestBody SubmissionAddRequest
                                                           submissionAddRequest)  {
        if (submissionAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 执行插入submission操作
        SubmissionVO submission = submissionService.submitCode(submissionAddRequest);
        return ResultUtils.success(submission);
    }

    /**
     * 分页获取submission
     * 按CreateTime降序排序
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<SubmissionVO>> listSubmissionByPage(@RequestBody
                                                         SubmissionQueryDTO submissionQueryDTO) {
        Page<SubmissionVO> submissionVOPage
                = submissionService.listByPage(submissionQueryDTO);
        return ResultUtils.success(submissionVOPage);
    }


    @GetMapping("/statistics")
    public BaseResponse<ProblemStatisticsVO> getSubmissionStatistics(
            @RequestParam("problemId") Long problemId) {
        ProblemStatisticsVO problemStatisticsVO = submissionService.getProblemStatisticsVO(problemId);
        return ResultUtils.success(problemStatisticsVO);
    }


    @GetMapping("/get")
    public BaseResponse getSubmission(@RequestParam Long id) {
        Submission byId = submissionService.getById(id);
        SubmissionVO submissionVO = submissionService.submissionToVO(byId);
        return ResultUtils.success(submissionVO);
    }
}

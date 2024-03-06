package com.wxc.oj.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxc.oj.model.dto.problem.ProblemQueryRequest;
import com.wxc.oj.model.dto.submission.SubmissionAddRequest;
import com.wxc.oj.model.dto.submission.SubmissionQueryDTO;
import com.wxc.oj.model.entity.Problem;
import com.wxc.oj.model.entity.Submission;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wxc.oj.model.entity.User;
import com.wxc.oj.model.vo.ProblemVO;
import com.wxc.oj.model.vo.SubmissionVO;

/**
* @author 王新超
* @description 针对表【submission】的数据库操作Service
* @createDate 2024-02-28 10:33:17
*/
public interface SubmissionService extends IService<Submission> {


    Submission submitCode(SubmissionAddRequest submissionAddRequest, User loginUser);

    LambdaQueryWrapper<Submission> getQueryWrapper(SubmissionQueryDTO submissionQueryDTO);


    SubmissionVO getSubmissionVO(Submission submission);

    Page<SubmissionVO> getSubmissionVOPage(Page<Submission> submissionPage);
}

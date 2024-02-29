package com.wxc.oj.service;

import com.wxc.oj.model.dto.submission.SubmissionAddRequest;
import com.wxc.oj.model.pojo.Submission;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wxc.oj.model.pojo.User;

/**
* @author 王新超
* @description 针对表【submission】的数据库操作Service
* @createDate 2024-02-28 10:33:17
*/
public interface SubmissionService extends IService<Submission> {



    public Long submitCode(SubmissionAddRequest submissionAddRequest, User loginUser);
}

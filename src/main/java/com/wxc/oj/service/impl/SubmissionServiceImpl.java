package com.wxc.oj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxc.oj.common.ErrorCode;
import com.wxc.oj.enums.SubmissionLanguageEnum;
import com.wxc.oj.enums.SubmissionStatusEnum;
import com.wxc.oj.exception.BusinessException;
import com.wxc.oj.model.dto.submission.SubmissionAddRequest;
import com.wxc.oj.model.entity.Submission;
import com.wxc.oj.model.entity.User;
import com.wxc.oj.service.SubmissionService;
import com.wxc.oj.mapper.SubmissionMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 王新超
* @description 针对表【submission】的数据库操作Service实现
* @createDate 2024-02-28 10:33:17
*/
@Service
public class SubmissionServiceImpl extends ServiceImpl<SubmissionMapper, Submission> implements SubmissionService {

    /**
     * 提交代码
     * @param submissionAddRequest
     * @param loginUser
     * @return 插入的submission的id
     */
    @Override
    public Long submitCode(SubmissionAddRequest submissionAddRequest, User loginUser) {
        Submission submission = new Submission();
        // 检查编程语言是否存在
        String language = submissionAddRequest.getLanguage();
        List<String> submissionLanguages = SubmissionLanguageEnum.getValues();
        if (!submissionLanguages.contains(language)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编程语言错误");
        }
        // 判断提交的题目是否存在
        Long problemId = submissionAddRequest.getProblemId();
        if (problemId == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 创建并初始化待插入数据库的submission
        submission.setProblemId(problemId);
        submission.setUserId(loginUser.getId());
        submission.setSourceCode(submissionAddRequest.getSourceCode());
        submission.setLanguage(submissionAddRequest.getLanguage());
        // 初始化判题状态为 waiting
        submission.setStatus(SubmissionStatusEnum.WAITING.getValue());
        // 保存到数据库
        boolean save = this.save(submission);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据插入失败");
        }
        // 返回submission的id
        return submission.getId();
    }
}





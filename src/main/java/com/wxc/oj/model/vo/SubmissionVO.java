package com.wxc.oj.model.vo;

import cn.hutool.json.JSONUtil;
import com.wxc.oj.model.entity.Submission;
import com.wxc.oj.model.judge.JudgeInfo;
import com.wxc.oj.model.submission.SubmissionResult;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 返回给前端的关于Problem的数据封装类
 */
@Data

public class SubmissionVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long userId;
    private Long problemId;
    private String sourceCode;
    /**
     * 返回多组测试用例的判题信息
     */
    private SubmissionResult submissionResult;

    /**
     * 判题状态
     * waiting, ...
     */
    private Integer status;
    /**
     * 得分: AC的样例占总样例的比例
     */
    private Integer score;

    private String language;
    private ProblemVO problemVO;
    private UserVO userVO;
    private Date createTime;

    /**
     * vo -> pojo
     */
    public static Submission voToObj(SubmissionVO submissionVO) {
        if (submissionVO == null) {
            return null;
        }
        Submission submission = new Submission();
        BeanUtils.copyProperties(submissionVO, submission);
        // 将submissionVO的JudgeInfo 转为 字符串 存到entity
        SubmissionResult submissionResult1 = submissionVO.getSubmissionResult();
        if (submissionResult1 != null) {
            submission.setSubmissionResult(JSONUtil.toJsonStr(submissionResult1));
        }
        return submission;
    }


    /**
     * pojo -> vo
     */
    public static SubmissionVO objToVo(Submission submission) {
        if (submission == null) {
            return null;
        }
        SubmissionVO submissionVO = new SubmissionVO();
        BeanUtils.copyProperties(submission, submissionVO);

        String judgeInfoString = submission.getSubmissionResult();
        SubmissionResult submissionResult1 = JSONUtil.toBean(judgeInfoString, SubmissionResult.class);
        submissionVO.setSubmissionResult(submissionResult1);
        return submissionVO;
    }
}
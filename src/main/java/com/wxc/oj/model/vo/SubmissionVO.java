package com.wxc.oj.model.vo;

import cn.hutool.json.JSONUtil;
import com.wxc.oj.enums.submission.SubmissionStatus;
import com.wxc.oj.model.entity.Submission;
import com.wxc.oj.model.submission.SubmissionResult;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

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


    private Long totalTime;

    private Long totalMemory;

    private Integer codeLength;
    /**
     * 返回多组测试用例的判题信息
     */
    private SubmissionResult submissionResult;

    /**
     * 判题状态
     * waiting, ...
     */
    private Integer status;

    private String submissionStatus;
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

        String submissionResultStr = submission.getSubmissionResult();
        SubmissionResult submissionResult1 = JSONUtil.toBean(submissionResultStr, SubmissionResult.class);
        Integer status1 = submission.getStatus();
        String typeByStatus = SubmissionStatus.getTypeByStatus(status1);
        submissionVO.setSubmissionStatus(typeByStatus);
        submissionVO.setSubmissionStatus(SubmissionStatus.getTypeByStatus(submission.getStatus()));
        submissionVO.setSubmissionResult(submissionResult1);
        return submissionVO;
    }
}
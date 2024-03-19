package com.wxc.oj.model.vo;

import cn.hutool.json.JSONUtil;
import com.wxc.oj.model.entity.Submission;
import com.wxc.oj.model.judge.JudgeInfo;
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
    private Long id;

    private Long userId;

    private Long problemId;

    private String sourceCode;

    /**
     * 返回多组测试用例的判题信息
     */
    private List<JudgeInfo> judgeInfo;

    /**
     * 判题结果
     * AC, WA...
     */
    private String result;

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

    private static final long serialVersionUID = 1L;

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
        JudgeInfo judgeInfo = (JudgeInfo) submissionVO.getJudgeInfo();
        if (judgeInfo != null) {
            submission.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
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

        String judgeInfoString = submission.getJudgeInfo();
        JudgeInfo judgeInfoObj = JSONUtil.toBean(judgeInfoString, JudgeInfo.class);
        submissionVO.setJudgeInfo((List<JudgeInfo>) judgeInfoObj);
        return submissionVO;
    }
}
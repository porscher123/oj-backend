package com.wxc.oj.model.vo;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.wxc.oj.common.PageRequest;
import com.wxc.oj.model.dto.judge.JudgeConfig;
import com.wxc.oj.model.dto.judge.JudgeInfo;
import com.wxc.oj.model.entity.Problem;
import com.wxc.oj.model.entity.Submission;
import lombok.Data;
import nonapi.io.github.classgraph.json.JSONUtils;
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

    private JudgeInfo judgeInfo;

    private Integer status;

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
        JudgeInfo judgeInfo = submissionVO.getJudgeInfo();
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
        submissionVO.setJudgeInfo(judgeInfoObj);
        return submissionVO;
    }
}
package com.wxc.oj.model.dto.problem;

import com.wxc.oj.model.dto.judge.JudgeCase;
import com.wxc.oj.model.dto.judge.JudgeConfig;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 普通用户可以对一个题目的修改
 * 点赞数, 收藏数
 */
@Data
public class ProblemEditRequest implements Serializable {

//    private Long id;
//
//    private String title;
//
//    private String content;

    private List<String> tags;

    private String level;

    private String solution;

    private Integer thumbNum;

    private Integer favorNum;

    private List<JudgeCase> judgeCase;

    private JudgeConfig judgeConfig;


    private static final long serialVersionUID = 1L;
}
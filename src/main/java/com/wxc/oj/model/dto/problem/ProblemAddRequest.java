package com.wxc.oj.model.dto.problem;

import com.wxc.oj.model.dto.judge.JudgeCase;
import com.wxc.oj.model.dto.judge.JudgeConfig;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


/**
 * 创建请求
 * 创建一个Problem所需要的东西
 */
@Data
public class ProblemAddRequest implements Serializable {


    private String title;

    private String content;

    private List<String> tags;

    private String level;

    private String solution;


    private List<JudgeCase> judgeCase;

    private JudgeConfig judgeConfig;

    private Long userId;


    private static final long serialVersionUID = 1L;
}
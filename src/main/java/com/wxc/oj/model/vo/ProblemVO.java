package com.wxc.oj.model.vo;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wxc.oj.model.po.Tag;
import com.wxc.oj.model.judge.JudgeConfig;
import com.wxc.oj.model.po.Problem;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 返回给前端的关于Problem的数据封装类
 * 用户给前端表格展示的题目,不需要返回content大文本
 */
@Data
public class ProblemVO implements Serializable {

    /*
     * 可以给前端用户查看题目id
     */
    private Long id;

    private String title;

    private String content;

    private List<Tag> tags;

    private Integer level;

    private Integer submittedNum;

    private Integer acceptedNum;

    private Integer thumbNum;

    private Integer favorNum;

    /**
     * 测试配置也要返回给前端用户
     * 不然用户怎么知道限制要求, 就不会约束自己
     */
    private JudgeConfig judgeConfig;


    /**
     * 只给前端返回到日期, 不要返回到具体时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd",timezone="GMT+8")
    private Date createTime;

    private Date updateTime;

    private Integer isDelete;

    private UserVO userVO;

    private static final long serialVersionUID = 1L;
    /**
     * vo -> pojo
     */
//    public static Problem voToObj(ProblemVO problemVO) {
//        if (problemVO == null) {
//            return null;
//        }
//        Problem problem = new Problem();
//        BeanUtils.copyProperties(problemVO, problem);
//        // pojo的tags时String, 要将vo的List<String>转换
//        List<Tag> tagList = problemVO.getTagList();
//        problem.setTags(JSONUtil.toJsonStr(tagList));
//        // vo的judgeConfig是对象, 所以将对象转为json
//        JudgeConfig judgeConfig = problemVO.getJudgeConfig();
//        problem.setJudgeConfig(JSONUtil.toJsonStr(judgeConfig));
//        return problem;
//    }


    /**
     * 查询题目列表，不返回题目content
     * pojo -> vo
     */
    public static ProblemVO objToVoWithoutContent(Problem problem) {
        if (problem == null) {
            return null;
        }
        ProblemVO problemVO = new ProblemVO();
        BeanUtils.copyProperties(problem, problemVO);
        // vo的tags时List<String>, 要将pojo的JSON String 转换
        // 将pojo的json字符串转为JudgeConfig类
        problemVO.setJudgeConfig(JSONUtil.toBean(problem.getJudgeConfig(), JudgeConfig.class));
        problemVO.setContent(null);
        return problemVO;
    }

    /**
     * 查询单个problem，返回具体题目描述信息
     * @param problem
     * @return
     */
    public static ProblemVO objToVoWithContent(Problem problem) {
        if (problem == null) {
            return null;
        }
        ProblemVO problemVO = new ProblemVO();
        BeanUtils.copyProperties(problem, problemVO);
        // vo的tags时List<String>, 要将pojo的JSON String 转换
        // 将pojo的json字符串转为JudgeConfig类
        problemVO.setJudgeConfig(JSONUtil.toBean(problem.getJudgeConfig(), JudgeConfig.class));
        return problemVO;
    }
}
package com.wxc.oj.model.vo;

import cn.hutool.json.JSONUtil;
import com.wxc.oj.model.entity.Tag;
import com.wxc.oj.model.judge.JudgeConfig;
import com.wxc.oj.model.entity.Problem;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 返回给前端的关于Problem的数据封装类
 */
@Data
public class ProblemVO implements Serializable {

    /**
     * 可以给前端用户查看题目id
     */
    private Long id;

    private String title;

    private String content;

    private List<Tag> tags;

    private String level;

//    private String solution;

    private Integer submittedNum;

    private Integer acceptedNum;

    private Integer thumbNum;

    private Integer favorNum;

    /**
     * 测试配置也要返回给前端用户
     * 不然用户怎么知道限制要求, 就不会约束自己
     */
    private JudgeConfig judgeConfig;

    private Date createTime;

    private Date updateTime;

    private Integer isDelete;

    private UserVO userVO;

    private static final long serialVersionUID = 1L;
    /**
     * vo -> pojo
     */
    public static Problem voToObj(ProblemVO problemVO) {
        if (problemVO == null) {
            return null;
        }
        Problem problem = new Problem();
        BeanUtils.copyProperties(problemVO, problem);
        // pojo的tags时String, 要将vo的List<String>转换
        List<Tag> tagList = problemVO.getTagList();
        problem.setTags(JSONUtil.toJsonStr(tagList));
        // vo的judgeConfig是对象, 所以将对象转为json
        JudgeConfig judgeConfig = problemVO.getJudgeConfig();
        problem.setJudgeConfig(JSONUtil.toJsonStr(judgeConfig));
        return problem;
    }

    private List<Tag> getTagList() {
        return this.tags;
    }

    /**
     * pojo -> vo
     */
    public static ProblemVO objToVo(Problem problem) {
        if (problem == null) {
            return null;
        }
        ProblemVO problemVO = new ProblemVO();
        BeanUtils.copyProperties(problem, problemVO);
        problemVO.setCreateTime(problem.getCreateTime());
        // vo的tags时List<String>, 要将pojo的JSON String 转换
        // 将pojo的json字符串转为JudgeConfig类
        problemVO.setJudgeConfig(JSONUtil.toBean(problem.getJudgeConfig(), JudgeConfig.class));
        return problemVO;
    }

    private void setTagList(List<Tag> list) {
        this.tags = list;
    }
}
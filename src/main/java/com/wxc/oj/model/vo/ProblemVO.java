package com.wxc.oj.model.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 返回给前端的关于Problem的数据封装类
 */
public class ProblemVO implements Serializable {
    /**
     * 可以给前端用户查看题目id
     */
    private Long id;

    private String title;

    private String content;

    private List<String> tags;

    private String level;

//    private String solution;

    private Integer submittedNum;

    private Integer acceptedNum;

    private Integer thumbNum;

    private Integer favorNum;

    private Long userId;

    private Date createTime;

    private Date updateTime;

    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}
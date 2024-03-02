package com.wxc.oj.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @TableName problem
 */
@TableName(value ="problem")
@Data
public class Problem implements Serializable {
    @TableId("id")
    private Long id;

    private String title;

    private String content;

    private String tags;

    /**
     * 难度
     */
    private String level;

    /**
     * 题解
     */
    private String solution;

    /**
     * 提交数
     */
    private Integer submittedNum;
    /**
     * 通过数
     */
    private Integer acceptedNum;

    /**
     * 测试用例
     */
    private String judgeCase;
    /**
     * 测试配置
     */
    private String judgeConfig;
    /**
     * 题目创建者
     */
    private Long userId;

    private Date createTime;

    private Date updateTime;


    private Integer isDelete;

    /**
     * 点赞数
     */
    private Integer thumbNum;
    /**
     * 收藏数
     */
    private Integer favorNum;

    private static final long serialVersionUID = 1L;
}
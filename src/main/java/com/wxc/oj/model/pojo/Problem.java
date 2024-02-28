package com.wxc.oj.model.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * @TableName problem
 */
@TableName(value ="problem")
@Data
public class Problem implements Serializable {
    private Long id;

    private String title;

    private String content;

    private String tags;

    private String level;

    private String solution;

    private Integer submittedNum;

    private Integer acceptedNum;

    private String judgeCase;

    private String judgeConfig;

    private Long userId;

    private Date createTime;

    private Date updateTime;

    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}
package com.wxc.oj.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * @TableName submission
 */
@TableName(value ="submission")
@Data
public class Submission implements Serializable {
    @TableId
    private Long id;

    private Long userId;

    private Long problemId;

    private String sourceCode;

    private String judgeInfo;

    private Integer status;

    private String language;

    private Date createTime;

    private Date updateTime;

    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}
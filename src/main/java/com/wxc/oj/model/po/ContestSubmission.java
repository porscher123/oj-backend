package com.wxc.oj.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * @TableName contest_submission
 */
@TableName(value ="contest_submission")
@Data
public class ContestSubmission implements Serializable {


    /**
     * id主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long contestId;

    private Long userId;

    private Long problemId;

    private Date submissionTime;

    private String sourceCode;

    private String language;

    private String submissionResult;

    private Integer isDeleted;

    // 将submissionResult的一部分数据拿出来
    private Integer status;

    private String statusDescription;


    private Integer score;

    private Long totalTime;

    private Long memoryUsed;

    private String judgeCaseResults;

    private static final long serialVersionUID = 1L;
}
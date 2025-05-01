package com.wxc.oj.model.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * @TableName contest_problem
 */
@TableName(value ="contest_problem")
@Data
public class ContestProblem implements Serializable {
    @TableId
    private Long id;

    private Long contestId;

    private Long problemId;

    private Integer pindex;

    private Date createdTime;

    private Date updatedTime;

    private Integer isDeleted;

    private static final long serialVersionUID = 1L;
}
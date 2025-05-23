package com.wxc.oj.model.dto.problem;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * @TableName problem_tag
 */
@TableName(value ="problem_tag")
@Data
public class ProblemTag implements Serializable {
    private Long problemId;

    private Integer tagId;

    private static final long serialVersionUID = 1L;
}
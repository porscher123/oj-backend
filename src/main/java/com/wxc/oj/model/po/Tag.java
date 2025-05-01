package com.wxc.oj.model.po;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * @TableName tag
 */
@TableName(value ="tag")
@Data
public class Tag implements Serializable {
    private Integer id;

    private String name;

    private String color;

    private static final long serialVersionUID = 1L;
}
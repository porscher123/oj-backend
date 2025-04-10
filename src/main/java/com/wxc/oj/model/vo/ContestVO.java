package com.wxc.oj.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @TableName contest
 */
@Data
public class ContestVO implements Serializable {

    private String title;

    private String description;

    private Date startTime;

    private Integer duration;

    private Integer status;

    private Integer isPublic;


    private static final long serialVersionUID = 1L;
}
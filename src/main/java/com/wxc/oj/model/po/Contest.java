package com.wxc.oj.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * @TableName contest
 */
@TableName(value ="contest")
@Data
public class Contest implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    private Date startTime;

    private Integer duration;

    private Integer status;

    private Integer isPublic;

    private Date createdTime;

    private Date updatedTime;

    private Integer isDeleted;

    private Long hostId;

    private Integer canRegister;

    private static final long serialVersionUID = 1L;
}
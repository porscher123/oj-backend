package com.wxc.oj.model.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * @TableName contest_registration
 */
@TableName(value ="contest_registration")
@Data
public class ContestRegistration implements Serializable {
    @TableId
    private Long id;

    private Long userId;

    private Long contestId;

    private Date createdTime;

    private Date updatedTime;

    private Integer isDeleted;

    private Integer status;

    private static final long serialVersionUID = 1L;
}
package com.wxc.oj.model.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {

    @TableId
    private Long id;

    private String userAccount;

    private String userPassword;

    private String userName;

    private String unionId;

    private String userAvatar;

    private String userProfile;

    private Integer userRole;

    private Date createTime;

    private Date updateTime;

    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}
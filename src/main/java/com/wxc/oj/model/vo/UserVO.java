package com.wxc.oj.model.vo;

import java.io.Serializable;
import java.util.Date;

import com.wxc.oj.model.entity.User;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/**
 * 用户视图（脱敏）
 */
@Data
public class UserVO implements Serializable {

    /**
     * id
     */
    private Long id;

    private String userAccount;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    private String jwtToken;


    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

    /**
     * 创建时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;


    /**
     * pojo -> vo
     */
    public static UserVO objToVo(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setId(null);
        userVO.setUserRole(null);
        userVO.setCreateTime(null);
        return userVO;
    }
}
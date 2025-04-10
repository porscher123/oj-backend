package com.wxc.oj.constant;

/**
 * 用户常量
 * @author wxc
 * @date 2025年3月25日16点41分
 */
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";

    //  region 权限

    /**
     * 默认角色
     */
    Integer DEFAULT_ROLE = 0;

    /**
     * 管理员角色
     */
    Integer ADMIN_ROLE = 1;

    /**
     * 被封号
     */
    Integer BAN_ROLE = 2;

    // endregion
}

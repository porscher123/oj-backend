package com.wxc.oj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxc.oj.annotation.AuthCheck;
import com.wxc.oj.common.BaseResponse;
import com.wxc.oj.common.DeleteRequest;
import com.wxc.oj.common.ErrorCode;
import com.wxc.oj.common.ResultUtils;
import com.wxc.oj.constant.UserConstant;
import com.wxc.oj.exception.BusinessException;
import com.wxc.oj.exception.ThrowUtils;
import com.wxc.oj.model.dto.user.*;
import com.wxc.oj.model.entity.User;
import com.wxc.oj.model.vo.UserVO;
import com.wxc.oj.service.UserService;
import com.wxc.oj.utils.JwtHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wxc.oj.constant.UserConstant.USER_LOGIN_STATE;
import static com.wxc.oj.service.impl.UserServiceImpl.SALT;

@RestController
@RequestMapping("user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtHelper jwtHelper;

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return 返回注册成功的user的VO对象
     */
    @PostMapping("register")
    public BaseResponse userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return null;
        }
        UserVO userVO = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(userVO);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param
     * @return
     */
    @PostMapping("login")
    public BaseResponse<UserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserVO userVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(userVO);
    }


//    @PostMapping("logout")
//    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
//        if (request == null) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        boolean result = userService.userLogout(request);
//        return ResultUtils.success(result);
//    }

    /**
     * 通过token获取当前登录用户
     */
    @GetMapping("/get/login")
    public BaseResponse<UserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getUserVO(user));
    }
    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */


    /**
     * 创建用户(管理员)
     *
     * @param userAddRequest
     */
    @PostMapping("add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse addUser(@RequestBody UserAddRequest userAddRequest) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        // 对初始密码加密
        String initPassword = user.getUserPassword();
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + initPassword).getBytes());
        user.setUserPassword(encryptPassword);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        User createdUser = userService.getById(user.getId());
        UserVO userVO = userService.getUserVO(createdUser);
        return ResultUtils.success(userVO);
    }

    /**
     * 删除用户
     *
     * @param deleteRequest
     */
    @PostMapping("delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse deleteUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = deleteRequest.getId();
        boolean b = userService.removeById(id);
        Map data = new HashMap();
        data.put("deleted", b);
        data.put("user", userService.getById(id));
        return ResultUtils.success(data);
    }


    /**
     * 根据 id 获取用户（仅管理员）
     *
     * @param id
     */
    @GetMapping("get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse getUserById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        UserVO userVO = userService.getUserVO(user);
        return ResultUtils.success(userVO);
    }

    /**
     * 根据 id 获取包装类
     * 用户可能会根据账户进行查询其它用户
     */
    @GetMapping("/get/vo")
    public BaseResponse queryUserVOByAccount(String userAccount) {
        List<User> userList = userService.queryUserVOByAccount(userAccount);
        List<UserVO> userVOList = userService.getUserVO(userList);
        return ResultUtils.success(userVOList);
    }

    /**
     * 分页获取用户列表（仅管理员）
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse listUserByPage(@RequestBody UserQueryRequest userQueryRequest) {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        return ResultUtils.success(userPage);
    }

    /**
     * 分页获取用户封装列表
     * 用于普通用户
     */
    @PostMapping("/list/page/vo")
    public BaseResponse listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
        userVOPage.setRecords(userVO);
        return ResultUtils.success(userVOPage);
    }

    /**
     * 更新用户仅管理员可更新
     */
    @PostMapping("update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        UserVO userVO = userService.getUserVO(user);
        return ResultUtils.success(userVO);
    }

    /**
     * 当前登录用户更新个人信息
     */
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyUser(@RequestBody UserUpdateMyRequest userUpdateMyRequest,
                                              HttpServletRequest request) {
        if (userUpdateMyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 当前用户
        User loginUser = userService.getLoginUser(request);
        User user = new User();
        BeanUtils.copyProperties(userUpdateMyRequest, user);
        user.setId(loginUser.getId());
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 查询自己信息
     */
    @GetMapping("/get/my")
    public BaseResponse queryMyUser(@RequestBody UserQueryRequest userQueryRequest,
                                    HttpServletRequest request) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 当前用户
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return ResultUtils.success(loginUser);
    }
}

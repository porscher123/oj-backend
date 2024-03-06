package com.wxc.oj.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxc.oj.common.ErrorCode;
import com.wxc.oj.constant.CommonConstant;
import com.wxc.oj.exception.BusinessException;
import com.wxc.oj.mapper.UserMapper;
import com.wxc.oj.model.dto.user.UserQueryRequest;
import com.wxc.oj.enums.UserRoleEnum;
import com.wxc.oj.model.entity.User;
import com.wxc.oj.model.vo.LoginUserVO;
import com.wxc.oj.model.vo.UserVO;
import com.wxc.oj.service.UserService;
import com.wxc.oj.utils.JwtHelper;
import com.wxc.oj.utils.SqlUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.wxc.oj.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author 王新超
 * @description 针对表【user】的数据库操作Service实现
 * @createDate 2024-02-28 10:12:35
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 盐值，混淆密码
     */
    public static final String SALT = "wxc";


    @Autowired
    private JwtHelper jwtHelper;


    /**
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return
     */
    public UserVO userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            var queryWrapper = new LambdaQueryWrapper<User>();
            queryWrapper.eq(User::getUserAccount, userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            user = getById(user.getId());
            UserVO userVO = getUserVO(user);
            return userVO;
        }
    }

    /**
     * 用户登录功能
     * 使用token保存用户登录态
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @return
     */
    public UserVO userLogin(String userAccount, String userPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // todo: 可对账户和密码进行限制
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        var queryWrapper = new LambdaQueryWrapper<User>();
        queryWrapper.eq(User::getUserAccount, userAccount);
        queryWrapper.eq(User::getUserPassword, encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 脱敏
        UserVO userVO = getUserVO(user);
        // 3. 记录用户的登录态
        // 登录成功, 根据id生成token
        return userVO;
    }

    @Override
    public List<User> queryUserVOByAccount(String userAccount) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(userAccount), User::getUserAccount, userAccount);
        List<User> userList = this.list(queryWrapper);
        return userList;
    }


    /**
     * 通过http请求获取当前登录用户
     * @param request
     * @return
     */
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }
    /**
     * TODO:
     *      1. 校验token有效性(是否过期)
     *      2. 根据token解析出UserId
     *      3. 根据UserId查询用户数据
     *      4. 去掉密码(因为数据库中的密码是加密的,
     *          不应该返回, 再说用户肯定知道自己的密码), 封装Result返回
     * @param token
     * @return
     */
//    @Override
//    public User getLoginUser(String token) {
//        if (jwtHelper.isExpiration(token)) {
//            // token失效, 视作未登录
//            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
//        }
//        // 校验成功, 获取id查询, 封装, 返回
//        int userId = jwtHelper.getUserId(token).intValue();
//        User loginUser = this.getById(userId);
////        UserVO userVO = getUserVO(loginUser);
//        return loginUser;
//    }

//    /**
//     * 获取当前登录用户（允许未登录）
//     * @param request
//     * @return
//     */
//    public UserVO getLoginUserPermitNull(HttpServletRequest request) {
//        // 先判断是否已登录
//        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
//        User currentUser = (User) userObj;
//        if (currentUser == null || currentUser.getId() == null) {
//            return null;
//        }
//        // 从数据库查询（追求性能的话可以注释，直接走缓存）
//        long userId = currentUser.getId();
//        UserVO userVO = getUserVO(getById(userId));
//        return userVO;
//    }

    /**
     * 是否为管理员
     * @param request
     * @return
     */
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        User loginUser = this.getLoginUser(request);
        return isAdmin(loginUser);
    }


    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        return false;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        return null;
    }

    /**
     * 用户注销
     * 使用Session存储会更好
     * @param
     */

//    public boolean userLogout(HttpServletRequest request) {
//        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
//            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
//        }
//        // 移除登录态
//        request.getSession().removeAttribute(USER_LOGIN_STATE);
//        return true;
//    }


    public LoginUserVO fUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    /**
     * user -> userVO
     * @param user
     * @return
     */
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }


    public List<UserVO> getUserVO(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }


    /**
     * 辅助方法:
     * 根据请求获取查询用的LambdaQueryWrapper
     * @param userQueryRequest
     * @return
     */
    public LambdaQueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        if (sortOrder == null) {
            sortOrder = CommonConstant.SORT_ORDER_ASC;
        }
        // 先创建QueryWrapper
        var queryWrapper = new QueryWrapper<User>();
        queryWrapper.eq(id != null, "userId", id);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        // 排序后转为LambdaQueryWrapper
        LambdaQueryWrapper<User> lambdaQueryWrapper = queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField).lambda();
        return lambdaQueryWrapper;
    }
}





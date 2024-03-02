package com.wxc.oj.interceptor;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxc.oj.common.BaseResponse;
import com.wxc.oj.common.ErrorCode;
import com.wxc.oj.common.ResultUtils;
import com.wxc.oj.utils.JwtHelper;
import com.wxc.oj.utils.SpringContextUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录保护拦截器:
 * 检查请求头是否包含有效token
 * 如果有: 放行
 * 没有: 返回504
 */
@Component
public class LoginProtectInterceptor implements HandlerInterceptor {


    @Autowired
    private JwtHelper jwtHelper;

    /**
     * TODO:
     *      1. 从请求头中获取token
     *      2. 检查token是否有效
     *          有效: 放行
     *          无效: 返回504
     * @param request
     * @param response
     * @param handler
     * @return true -> 放行
     *         false -> 不放行
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("token");
        // token为空, 肯定是无效的
        boolean expiration = jwtHelper.isExpiration(token);
        // 有效
        if (!expiration) {
            return true;
        }
        // token无效, 设置响应体内容
        BaseResponse result = ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(result);
        response.getWriter().print(json);
        return false;
    }

}

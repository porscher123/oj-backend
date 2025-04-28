package com.wxc.oj.interceptor;


import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxc.oj.common.BaseResponse;
import com.wxc.oj.common.ErrorCode;
import com.wxc.oj.common.ResultUtils;
import com.wxc.oj.model.entity.User;
import com.wxc.oj.model.vo.UserVO;
import com.wxc.oj.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录保护拦截器:
 * 检查请求头是否包含有效token
 * 如果有: 放行
 * 没有: 返回504
 * @LastModifiedBy wxc
 * @LastModifiedDate 2025年3月26日15点13分
 */
@Component
@Slf4j
public class LoginProtectInterceptor implements HandlerInterceptor {


    @Resource
    StringRedisTemplate stringRedisTemplate;
    private static final String BEARER_PREFIX = "Bearer ";
    @Resource
    private JwtUtils jwtUtils;

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
        if (HttpMethod.OPTIONS.toString().equals(request.getMethod())) {
            return true;
        }
//        String token = request.getHeader("token");
        String token1 = request.getHeader("Authorization");
        log.info("token = " + token1);
        if (token1 == null || !token1.startsWith(BEARER_PREFIX)) {
            BaseResponse result = ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR);
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(result);
            response.getWriter().print(json);
            return false;
        }
        log.info("token = " + token1);

        String token = token1.substring(7);
        log.info("token = " + token);
        // token为空, 肯定是无效的
        boolean valid = jwtUtils.isTokenValid(token);
        // 有效
        if (valid) {
            log.info("token有效");
//            UserVO userVO = JwtUtils.parseUserVOFromToken(token);
//            log.info("uservo = " + userVO);
//            Long userId = userVO.getId();
//            log.info("userId = " + userId);
            Long userId = JwtUtils.getUserIdFromToken(token);
            String s = stringRedisTemplate.opsForValue().get("user:" + userId);
            User user = JSONUtil.toBean(s, User.class);
            if (s != null && user != null) {
                log.info("token有效2");

                return true;
            }
        }
        log.info("token无效");
        // token无效, 设置响应体内容
        BaseResponse result = ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(result);
        response.getWriter().print(json);
        return false;
    }

}

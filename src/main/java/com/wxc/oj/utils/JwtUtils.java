package com.wxc.oj.utils;

import com.alibaba.druid.util.StringUtils;
import io.jsonwebtoken.*;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @Auhor: wxc
 * @Date: 2025年3月25日19点40分
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt.token") // 配置读取属性的前缀
public class JwtUtils {

    // 以下2个值, 会读取application.yaml中配置的属性
    private static long tokenExpiration = 86400000; //有效时间,单位毫秒 1000毫秒 == 1秒

    private static String tokenSignKey = "onlinejudge";  //当前程序签名秘钥

    //定义token返回头部
    public static final String AUTH_HEADER_KEY = "Authorization";

    //token前缀
    public static final String TOKEN_PREFIX = "Bearer ";
    /**
     * 给特定ID的用户生成token字符串
     * @param userId
     * @return
     */
    public static String createToken(Long userId) {
        String token = Jwts.builder()
                .setSubject("online judge")
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration*1000*60)) //单位分钟
                .claim("userId", userId)
                .signWith(SignatureAlgorithm.HS512, tokenSignKey)
                .compressWith(CompressionCodecs.GZIP)
                .compact();
        return token;
    }

    /**
     * 从token字符串获取userid
     * @param token
     * @return
     */
    public Long getUserId(String token) {
        if(StringUtils.isEmpty(token)) return null;
        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();
        Integer userId = (Integer)claims.get("userId");
        return userId.longValue();
    }

    public static Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(tokenSignKey)
                .parseClaimsJws(token)
                .getBody();
        return claims.get("userId", Long.class);
    }
    /**
     * 判断token是否有效(过期)
     * @param token
     * @return
     */
    public boolean isExpiration(String token){
        try {
            boolean isExpire = Jwts.parser()
                    .setSigningKey(tokenSignKey)
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration().before(new Date());
            //没有过期，有效，返回false
            return isExpire;
        }catch(Exception e) {
            //过期出现异常，返回true
            return true;
        }
    }
}

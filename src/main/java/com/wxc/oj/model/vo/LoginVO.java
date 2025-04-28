package com.wxc.oj.model.vo;

import lombok.Data;

@Data
public class LoginVO {
    UserVO userVO;
    String token;
}

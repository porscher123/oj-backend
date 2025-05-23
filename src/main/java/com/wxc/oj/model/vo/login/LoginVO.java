package com.wxc.oj.model.vo.login;

import com.wxc.oj.model.vo.UserVO;
import lombok.Data;

@Data
public class LoginVO {
    UserVO userVO;
    String token;
}

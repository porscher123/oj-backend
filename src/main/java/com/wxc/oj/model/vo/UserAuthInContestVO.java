package com.wxc.oj.model.vo;


import lombok.Data;

@Data
public class UserAuthInContestVO {
    private boolean isRegistered;
    private boolean canRegister;
    private boolean canView;
}

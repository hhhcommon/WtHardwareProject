package com.wotingfm.activity.im.interphone.commom.model;

import java.io.Serializable;

public class UserInfo implements Serializable {
    private static final long serialVersionUID = -274654203100125137L;

    public String userId;
    public String UserName;
    public String LoginName;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getLoginName() {
        return LoginName;
    }

    public void setLoginName(String loginName) {
        LoginName = loginName;
    }
}

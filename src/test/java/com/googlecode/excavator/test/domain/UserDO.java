package com.googlecode.excavator.test.domain;

import java.io.Serializable;

public class UserDO implements Serializable {

    private static final long serialVersionUID = -860008917517733810L;

    private long userId;
    private String username;
    private String password;
    private String realname;
    
    public long getUserId() {
        return userId;
    }
    public void setUserId(long userId) {
        this.userId = userId;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getRealname() {
        return realname;
    }
    public void setRealname(String realname) {
        this.realname = realname;
    }
    
}

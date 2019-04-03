package com.wtn.mybatis_plugs.dto;

import com.wtn.mybatis_plugs.util.PageVo;

/**
 * 2019/4/3
 * Created by 蒋欣洋.
 */
public class UserDto extends PageVo {

    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

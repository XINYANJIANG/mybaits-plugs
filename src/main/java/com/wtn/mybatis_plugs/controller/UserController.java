package com.wtn.mybatis_plugs.controller;

import com.wtn.mybatis_plugs.domain.User;
import com.wtn.mybatis_plugs.dto.UserDto;
import com.wtn.mybatis_plugs.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 2019/4/2
 * Created by 蒋欣洋.
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/findAllUser")
    public List<User> findAllUser(){
        return userService.findAllUser();
    }

    @GetMapping("/findAllUserByUsername")
    public List<User> findAllUserByUsername(String username){
        return userService.findAllUserByUsername(username, 1);
    }


    @PostMapping("/findUserPage")
    public List<User> findUserPage(@RequestBody UserDto userDto){
        return userService.findUserPage(userDto);
    }
}

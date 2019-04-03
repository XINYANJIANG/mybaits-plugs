package com.wtn.mybatis_plugs.service.impl;

import com.wtn.mybatis_plugs.dao.UserDao;
import com.wtn.mybatis_plugs.domain.User;
import com.wtn.mybatis_plugs.dto.UserDto;
import com.wtn.mybatis_plugs.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 2019/4/2
 * Created by 蒋欣洋.
 */
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserDao userDao;

    @Override
    public List<User> findAllUser() {
        return userDao.findAllUser();
    }

    @Override
    public List<User> findAllUserByUsername(String username, int id) {
        return userDao.findAllUserByUsername(username,id);
    }

    @Override
    public List<User> findUserPage(UserDto userDto) {
        return userDao.findUserPage(userDto);
    }
}

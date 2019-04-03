package com.wtn.mybatis_plugs.service;



import com.wtn.mybatis_plugs.domain.User;
import com.wtn.mybatis_plugs.dto.UserDto;

import java.util.List;

/**
 * 2019/4/2
 * Created by 蒋欣洋.
 */

public interface UserService {

    List<User> findAllUser();

    List<User> findAllUserByUsername(String username, int id);

    List<User> findUserPage(UserDto userDto);
}

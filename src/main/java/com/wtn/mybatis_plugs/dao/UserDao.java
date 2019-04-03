package com.wtn.mybatis_plugs.dao;


import com.wtn.mybatis_plugs.domain.User;
import com.wtn.mybatis_plugs.dto.UserDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 2019/4/2
 * Created by 蒋欣洋.
 */
@Mapper
public interface UserDao {
    List<User> findAllUser();

    List<User> findAllUserByUsername(@Param("username") String username, int id);

    List<User> findUserPage(UserDto userDto);
}

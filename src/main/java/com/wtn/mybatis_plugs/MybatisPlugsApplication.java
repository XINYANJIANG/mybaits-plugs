package com.wtn.mybatis_plugs;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@MapperScan(basePackages = "com.wtn.mybatis_plugs.dao.*")
public class MybatisPlugsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MybatisPlugsApplication.class, args);
    }

}

package com.imooc.mall.service.impl;

import com.imooc.mall.MallApplicationTests;
import com.imooc.mall.enums.ResponseEnum;
import com.imooc.mall.enums.RoleEnum;
import com.imooc.mall.pojo.User;
import com.imooc.mall.service.IUserService;
import com.imooc.mall.vo.ResponseVo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class UserServiceImplTest extends MallApplicationTests {

    public static final String USERNAME ="abc";
    public static final String USER_PASSWORD ="123456789";

    @Autowired
    IUserService iUserService;

    @Before
    public void register() {
        User user = new User(USERNAME,USER_PASSWORD,"12@qq.com", RoleEnum.ADMIN.getCode());
        ResponseVo<User> userResponseVo = iUserService.register(user);
    }

    @Test
    public void login() {
        ResponseVo responseVo=iUserService.login(USERNAME,USER_PASSWORD);
        Assert.assertEquals(ResponseEnum.SUCCESS.getCode(),responseVo.getStatus());
    }
}
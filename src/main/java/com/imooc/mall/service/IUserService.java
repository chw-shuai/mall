package com.imooc.mall.service;

import com.imooc.mall.pojo.User;
import com.imooc.mall.vo.ResponseVo;

/**
 * @author 常红伟
 */
public interface IUserService {


    /**
     * 注册
     */
    ResponseVo<User> register(User user);

    /**
     * 登陆
     * @param username
     * @param password
     * @return
     */
    ResponseVo<User> login(String username,String password);

}

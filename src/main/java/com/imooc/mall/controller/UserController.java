package com.imooc.mall.controller;

import com.imooc.mall.consts.MallConst;
import com.imooc.mall.form.UserLoginForm;
import com.imooc.mall.form.UserRegisterForm;
import com.imooc.mall.pojo.User;
import com.imooc.mall.service.IUserService;
import com.imooc.mall.vo.ResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import static com.imooc.mall.enums.ResponseEnum.NEED_LOGIN;
import static com.imooc.mall.enums.ResponseEnum.PARAM_ERROR;

@RestController
@Slf4j

public class UserController {
    @Autowired
    IUserService iUserService;


    /**
     * 注册
     * @param userRegisterForm
     * @param bindingResult
     * @return
     */
    @PostMapping("/user/register")
    ResponseVo <User>register(@Valid @RequestBody UserRegisterForm userRegisterForm, BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            log.error("注册提交的数据有误,{} {}",
                    bindingResult.getFieldError().getField(),
                    bindingResult.getFieldError().getDefaultMessage());
            return ResponseVo.error(NEED_LOGIN,bindingResult);
        }

        User user = new User();
        BeanUtils.copyProperties(userRegisterForm,user);

        return iUserService.register(user);
    }


    /**
     * 登陆
     * @param userLoginForm
     * @param bindingResult
     * @param session
     * @return
     */
    @PostMapping("/user/login")
    ResponseVo <User>login(@Valid @RequestBody UserLoginForm userLoginForm, BindingResult bindingResult, HttpSession session){
        if (bindingResult.hasErrors()){
            return ResponseVo.error(PARAM_ERROR,bindingResult);
        }
        //调用service登录验证方法进行验证
        ResponseVo<User> userResponseVo = iUserService.login(userLoginForm.getUsername(), userLoginForm.getPassword());
        //设置session
        session.setAttribute(MallConst.CURRENT_USER,userResponseVo.getData());
        log.info("/login sessionId={}",session.getId());
        return userResponseVo;

    }

    /**
     * 查询用户信息
     * @param session
     * @return
     */
    @GetMapping("/user")
    ResponseVo<User> userInfo(HttpSession session){
        log.info("/user sessionId={}",session.getId());
     User user = (User)session.getAttribute(MallConst.CURRENT_USER);
     return ResponseVo.success(user);
    }


    /**
     * 退出登录
     * @param session
     * @return
     */
    @PostMapping("/user/logout")
    ResponseVo<User> logout(HttpSession session){
        log.info("/user/logout sessionId={}",session.getId());

        return ResponseVo.success();
    }
}

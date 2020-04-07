package com.imooc.mall.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 用户登陆 表单校验对象
 * @author 常红伟
 */
@Data
public class UserLoginForm {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}

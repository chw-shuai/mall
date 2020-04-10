package com.imooc.mall.form;

import lombok.Data;

import javax.validation.constraints.NotNull;


/**
 * 购物车添加商品表单
 */
@Data
public class CartAddForm {

    @NotNull
     private Integer productId;

    /**
     * 商品是否被选中
     */
    private Boolean selected = true;
}

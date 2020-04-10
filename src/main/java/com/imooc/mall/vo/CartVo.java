package com.imooc.mall.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartVo {

    /**
     * 购物车商品集合
     */
    List<CartProductVo> cartProductVoList;

    /**
     * 是否全选
     */
    Boolean selectedAll;

    /**
     * 购物车商品总价
     */
    BigDecimal cartTotalPrice;


    /**
     * 购物车总数量
     */
    Integer cartTotalQuantity;
}

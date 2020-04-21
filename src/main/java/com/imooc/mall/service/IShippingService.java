package com.imooc.mall.service;

import com.github.pagehelper.PageInfo;
import com.imooc.mall.form.ShippingForm;
import com.imooc.mall.vo.ResponseVo;

import java.util.Map;

public interface IShippingService {

    /**
     * 添加收货地址
     * @return
     */
    ResponseVo<Map<String,Integer>> insert(Integer uid, ShippingForm shippingForm);

    /**
     * 删除收货地址
     * @param uid
     * @param shippingId
     * @return
     */
    ResponseVo delete(Integer uid,Integer  shippingId);

    /**
     * 更新收货地址
     * @param uid
     * @param shippingForm
     * @return
     */
    ResponseVo update(Integer uid,Integer shippingId, ShippingForm shippingForm);

    /**
     * 查询收货地址
     * @param uid
     * @param pageNum
     * @param pageSize
     * @return
     */
    ResponseVo<PageInfo> select(Integer uid,Integer pageNum,Integer pageSize );
}

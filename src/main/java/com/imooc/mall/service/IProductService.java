package com.imooc.mall.service;

import com.github.pagehelper.PageInfo;
import com.imooc.mall.vo.ProductDetailVo;
import com.imooc.mall.vo.ResponseVo;

public interface IProductService {

    /**
     * 查询商品列表
     * @param categoryId
     * @param pageNum
     * @param pageSize
     * @return
     */
    ResponseVo<PageInfo> list(Integer categoryId, Integer pageNum, Integer pageSize);


    /**
     * 查询商品商品详情列表
     * @param productId
     * @return
     */
    ResponseVo<ProductDetailVo> ProductDetailVoList(Integer productId);

}

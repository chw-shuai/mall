package com.imooc.mall.service;

import com.imooc.mall.vo.CategoryVo;
import com.imooc.mall.vo.ResponseVo;

import java.util.List;
import java.util.Set;

/**
 *类目接口
 * @author 常红伟
 */
public interface ICategoryService {

    /**
     * 查询所有类目
     * @return
     */
    ResponseVo<List<CategoryVo>>selectAllCategory();

    /**
     * 查询所有子类目
     * @param id
     * @param resultSet
     */
    void findSubCategoryId(Integer id, Set<Integer> resultSet);
}

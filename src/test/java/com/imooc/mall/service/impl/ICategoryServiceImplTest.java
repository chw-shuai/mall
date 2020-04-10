package com.imooc.mall.service.impl;

import com.imooc.mall.MallApplicationTests;
import com.imooc.mall.enums.ResponseEnum;
import com.imooc.mall.vo.CategoryVo;
import com.imooc.mall.vo.ResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class ICategoryServiceImplTest extends MallApplicationTests {

    @Autowired
    private CategoryServiceImpl categoryService;

    @Test
    public void selectAllCategory() {
        ResponseVo<List<CategoryVo>> listResponseVo = categoryService.selectAllCategory();
        Assert.assertEquals(ResponseEnum.SUCCESS.getCode(),listResponseVo.getStatus());
    }

    @Test
    public void findSubCategoryId(){
        Set <Integer>set = new HashSet();
        categoryService.findSubCategoryId(100001,set);
        log.info("set={}",set);


    }

}
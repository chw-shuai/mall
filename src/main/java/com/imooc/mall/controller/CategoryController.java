package com.imooc.mall.controller;


import com.imooc.mall.service.ICategoryService;
import com.imooc.mall.vo.CategoryVo;
import com.imooc.mall.vo.ResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author 常红伟
 */
@RestController
@Slf4j
public class CategoryController {

    @Autowired
    ICategoryService ICategoryService;

    @GetMapping("/categories")
    public ResponseVo<List<CategoryVo>>categories(){

        return ICategoryService.selectAllCategory();
    }
}

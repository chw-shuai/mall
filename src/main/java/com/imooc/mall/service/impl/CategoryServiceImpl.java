package com.imooc.mall.service.impl;

import com.imooc.mall.dao.CategoryMapper;
import com.imooc.mall.pojo.Category;
import com.imooc.mall.service.ICategoryService;
import com.imooc.mall.vo.CategoryVo;
import com.imooc.mall.vo.ResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.imooc.mall.consts.MallConst.ROOT_PARENT_ID;

/**
 * @author 常红伟
 */
@Service
@Slf4j
public class CategoryServiceImpl implements ICategoryService {

    @Autowired
    CategoryMapper categoryMapper;

    @Override
    public ResponseVo<List<CategoryVo>> selectAllCategory() {
        //查询所有类目
        List<Category> categories = categoryMapper.selectAllCategory();
        //所有ID为0的跟目录
        //lambda + stream
        List<CategoryVo> categoryVoList = categories.stream()
                .filter(e -> e.getParentId().equals(ROOT_PARENT_ID))
                .map(this::category2CategoryVo)
                .sorted(Comparator.comparing(CategoryVo::getSortOrder).reversed())
                .collect(Collectors.toList());
        //查询子目录
        findSubCategory(categories,categoryVoList);
        return ResponseVo.success(categoryVoList);
    }

    @Override
    public void findSubCategoryId(Integer id, Set<Integer> resultSet) {
        List<Category> categories = categoryMapper.selectAllCategory();
        findSubCategoryId(id, resultSet ,categories);
    }

    public void findSubCategoryId(Integer id, Set<Integer> resultSet,List<Category> categories) {
        for (Category category : categories) {
            if (category.getParentId().equals(id)){
                resultSet.add(category.getId());
                findSubCategoryId(category.getId(),resultSet,categories);
            }
        }
    }

    private void findSubCategory(List<Category> categories,List<CategoryVo>categoryVoList){

        //遍历所有父目录
        for (CategoryVo categoryVo : categoryVoList) {
            //存放所有子目录
            List<CategoryVo>subCategoryVoList = new ArrayList<>();
            //遍历所有目录
            for (Category category : categories) {
                //如何父目录的id 等于 子目录的 ParentId
                if (categoryVo.getId().equals(category.getParentId())){
                    //就把该目录存放到 子目录集合
                    subCategoryVoList.add(category2CategoryVo(category));
                }
                //排序
                subCategoryVoList.sort(Comparator.comparing(CategoryVo::getSortOrder).reversed());
                //再把子目录存放到父目录的属性中
                categoryVo.setCategoryVoList(subCategoryVoList);
                //递归实现 多及目录查询 将子目录再次1进行传递
                findSubCategory(categories,subCategoryVoList);
            }
        }
    }

    private CategoryVo category2CategoryVo(Category category){
        CategoryVo categoryVo = new CategoryVo();
        BeanUtils.copyProperties(category,categoryVo);
        return categoryVo;
    }
}

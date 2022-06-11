package com.it.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.reggie.common.CustomException;
import com.it.reggie.entity.Category;
import com.it.reggie.entity.Dish;
import com.it.reggie.entity.Setmeal;
import com.it.reggie.mapper.CategoryMapper;
import com.it.reggie.service.CategoryService;
import com.it.reggie.service.DishService;
import com.it.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 根据id进行删除分类，删除之前需要判断
     * @param id
     */
    @Override
    public void remove(Long id) {

        //*1 当前分类是否关联菜品，如果已关联，抛出一个业务异常
        LambdaQueryWrapper<Dish>  dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 添加查询条件，按id查询
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);
        // 根据条件统计菜品数量
        int count = dishService.count(dishLambdaQueryWrapper);
        if(count>0){
            // 已经关联菜品，抛出一个业务异常
            throw new CustomException("当前分类项关联了菜品，不能删除");
        }
        //*2 当前分类是否关联套餐，如果已关联，抛出一个业务异常
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        int setmealCount = setmealService.count(setmealLambdaQueryWrapper);
        if(setmealCount>0){
            // 已关联套餐，抛出一个业务异常
            throw new CustomException("当前分类项关联了套餐，不能删除");
        }

        //*3 正常删除分类
        super.removeById(id);
    }
}

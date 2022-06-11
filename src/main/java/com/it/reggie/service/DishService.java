package com.it.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.it.reggie.dto.DishDto;
import com.it.reggie.entity.Dish;


public interface DishService extends IService<Dish> {
    // 新增菜品，同时插入菜品对应的口味数据，需要操作两张表：dish，dish_flavor
    public void saveWithFlavor(DishDto dishDto);

    // 根据id查询菜品信息+口味信息
    public DishDto getByIdWithflavor(Long id);

    //更新菜品信息+口味信息
    public void updateWithFlavor(DishDto dishDto);

    //批量+单条删除菜品信息+口味信息
    public void deleteWithFlavor(String ids);

    //批量+单条启停售菜品信息
    public void openAndSotpWithFlavor(int status, String ids);
}


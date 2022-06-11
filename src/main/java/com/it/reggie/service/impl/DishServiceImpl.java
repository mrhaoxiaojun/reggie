package com.it.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.reggie.dto.DishDto;
import com.it.reggie.entity.Dish;
import com.it.reggie.entity.DishFlavor;
import com.it.reggie.mapper.DishMapper;
import com.it.reggie.service.DishFlavorService;
import com.it.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;
    /**
     * 新增菜品同时保存口味数据
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存基本信息到菜品表dish
        this.save(dishDto);

        Long dishId = dishDto.getId(); //菜品id

        List<DishFlavor> flavors = dishDto.getFlavors();

        //需要把菜品id，添加到口味list中，方便保存到口味表中
        flavors = flavors.stream().map((item)-> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        //保存基本信息到菜品口味表dish_falvor
        dishFlavorService.saveBatch(flavors);

    }

    /**
     * 根据id查询菜品信息+口味信息
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithflavor(Long id) {
        //查询菜品基本信息
        Dish dish = this.getById(id);
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        //查询当前菜品对应的口味信息，从dish_flavor表查询
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        //查询口信息味集合
        List<DishFlavor> flavors =dishFlavorService.list(queryWrapper);

        dishDto.setFlavors(flavors);
        return dishDto;
    }

    /**
     * 更新修改菜品信息+口味信息表
     * @param dishDto
     * @return
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish表信息
        this.updateById(dishDto);
        //更新口味表
        //先清理口味数据---dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);
        //添加新的口味数据---dish_flavor表的insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();

        //需要把菜品id，添加到口味list中，方便保存到口味表中
        flavors = flavors.stream().map((item)-> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 批量+单个删除菜品信息+口味信息表
     * @param ids
     */
    @Override
    @Transactional
    public void deleteWithFlavor(String ids) {
        //笨方法
        //String[] idAry = ids.split(",");
        //for (String id : idAry) {
        //    LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //    LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //    queryWrapper.eq(Dish::getId,id);
        //    this.remove(queryWrapper);
        //    dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId,id);
        //    dishFlavorService.remove(dishFlavorLambdaQueryWrapper);
        //}
        //删除菜品
        String[] idAry = ids.split(",");
        List<String> idList = Arrays.asList(idAry);
        this.removeByIds(idList);
        //根据菜品id删除口味数据
        for (String dishIdss : idAry) {
            LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId,dishIdss);
            dishFlavorService.remove(dishFlavorLambdaQueryWrapper);
        }

    }

    /**
     * 批量+单条启停售菜品信息
     * @param status
     * @param ids
     */
    @Override
    public void openAndSotpWithFlavor(int status, String ids) {
        //类型转换
        String[] idAry = ids.split(",");
        List<String> idsList = Arrays.asList(idAry);
        //查询菜品id对应的数据并修改其status字段
        List<Dish> dishList= idsList.stream().map((item)->{
            Dish dish = this.getById(item);
            dish.setStatus(status);
            return dish;
        }).collect(Collectors.toList());
        //更新数据
        this.updateBatchById(dishList);
    }

}

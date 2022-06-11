package com.it.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.reggie.entity.SetmealDish;
import com.it.reggie.mapper.SetmealDishMapper;
import com.it.reggie.service.SetmealDishSerivce;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SetmealDishSerivceImpl extends ServiceImpl<SetmealDishMapper, SetmealDish> implements SetmealDishSerivce {
}

package com.it.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.it.reggie.common.BaseContext;
import com.it.reggie.common.R;
import com.it.reggie.entity.ShoppingCart;
import com.it.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add (@RequestBody ShoppingCart shoppingCart){
        log.info("购物车数据,{}",shoppingCart);

        //1、设置用户Id，指定当前是哪个用户的购物车数据
        Long currentId = (Long) BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        //2、查询当前菜品或套餐是否在购物车中
        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currentId);

        if (dishId!=null){
            // add的是菜品
            queryWrapper.eq(ShoppingCart::getDishId,dishId);

         }else{
            // add的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
         }
        //sql:select * from shopping_cart shere user_id = ? and dish_id/setmeal_id = ?
        ShoppingCart shoppingCartServiceOne = shoppingCartService.getOne(queryWrapper);


        if(shoppingCartServiceOne!=null){
            //3、如果已经存在，+1
            Integer number = shoppingCartServiceOne.getNumber();
            shoppingCartServiceOne.setNumber(number+1);
            shoppingCartService.updateById(shoppingCartServiceOne);
        }else{
            //4、如果不存在，添加到购物车，默认数量1
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            //为了方便统一返回，所复制给shoppingCartServiceOne
            shoppingCartServiceOne = shoppingCart;
        }

        return  R.success(shoppingCartServiceOne);
    }

    /**
     * 减少购物车数量
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<String> sub(@RequestBody ShoppingCart shoppingCart){
        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        if (dishId!=null){
            // 删除菜品
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
        }else{
            // 删除套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        ShoppingCart shoppingCartServiceOne = shoppingCartService.getOne(queryWrapper);


        shoppingCartServiceOne.setNumber(shoppingCartServiceOne.getNumber() -1);

        if(shoppingCartServiceOne.getNumber() == 0){
            shoppingCartService.removeById(shoppingCartServiceOne.getId());
        }

        shoppingCartService.updateById(shoppingCartServiceOne);

        return R.success("删少一份"+shoppingCartServiceOne.getName());
    }

    /**
     * 查看购物车
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list (){
        log.info("查看购物车。。");
        LambdaQueryWrapper<ShoppingCart>  queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        return R.success(list);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean(){
        //sql: delete from shopping_cat where user_id = ?
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);
        return R.success("清空购物车成功");
    }
}

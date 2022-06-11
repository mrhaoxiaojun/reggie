package com.it.reggie.common;

/**
 * 基于ThreadLocal封装的工具类，用于保存和获取当前登录用户id
 */
public class BaseContext {
    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentId (Long id){
        threadLocal.set(id);
    }

    public static Object getCurrentId(){
        return threadLocal.get();
    }
}

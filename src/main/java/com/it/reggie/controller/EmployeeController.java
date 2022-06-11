package com.it.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.it.reggie.common.R;
import com.it.reggie.entity.Employee;
import com.it.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login (HttpServletRequest request, @RequestBody Employee employee){

        /**
         * 1、密码md5加密
         * 2、通用username查询数据库
         * 3、查询不到则返回登录失败
         * 4、密码比对，失败返回登录失败
         * 5、查看员工status，如果被禁用则返回用户被禁用
         * 6、登录成功，将员工的id存入Session 并返回成功
         */

        // 1、密码md5加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        // 2、通用username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        String username = employee.getUsername();
        queryWrapper.eq(Employee::getUsername,username);
        Employee emp = employeeService.getOne(queryWrapper);

        // 3、查询不到则返回登录失败
        if(emp == null){
            return R.error("登录失败");
        }

        // 4、密码比对，失败返回登录失败
        if(!emp.getPassword().equals(password)){
            return R.error("登录失败");
        }

        // 5、查看员工status，如果被禁用则返回用户被禁用
        if(emp.getStatus() == 0){
            return R.error("账号已禁用");
        }

        // 6、登录成功，将员工的id存入Session 并返回成功
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    };

    @PostMapping("/logout")
    public R<String> logout (HttpServletRequest request) {
        // 清理session里存保存的当前登录员工id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    };

    @PostMapping
    public R<String> save (HttpServletRequest request,@RequestBody Employee employee){
        log.info("新增员工，员工信息:{}",employee.toString());

        // 设置初始密码123456，需要md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        //employee.setCreateTime(LocalDateTime.now());
        //employee.setUpdateTime(LocalDateTime.now());
        //
        //// 获取当前登录用户id
        //Long empid= (Long) request.getSession().getAttribute("employee");
        //
        //employee.setCreateUser(empid);
        //employee.setUpdateUser(empid);

        employeeService.save(employee);

        return R.success("新增员工成功");
    };

    /**
     * 员工信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        log.info("page={},pageSize={},name={}",page,pageSize,name);

        // 构造分页构造器
        Page pageInfo = new Page(page,pageSize);

        // 构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        // 添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        // 添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        // 查询结构
        employeeService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 根据id修改员工信息
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update (HttpServletRequest request,@RequestBody Employee employee){

        Long empId = (Long) request.getSession().getAttribute("employee");
        //改到全局自动填充
        //employee.setUpdateTime(LocalDateTime.now());
        //employee.setUpdateUser(empId);
        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }

    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据id查元员工信息");
        Employee employee = employeeService.getById(id);
        if(employee != null){
            return R.success(employee);
        }
        return  R.error("没有查询到对于的员工信息");
    }
}

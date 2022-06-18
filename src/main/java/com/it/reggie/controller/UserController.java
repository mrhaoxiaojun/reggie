package com.it.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.it.reggie.common.R;
import com.it.reggie.entity.User;
import com.it.reggie.service.UserService;
import com.it.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发送手机短信验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) throws Exception {
        //获取手机号
        String phone = user.getPhone();

        if(StringUtils.isNotEmpty(phone)){
            //生成随机的4位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code={}",code);

            //调用阿里云提供的短信服务API完成发送短信---个人没办法注册
            //SMSUtils.sendMessage("阿里云短信测试","SMS_154950909",phone,code);

            //方法1 - 需要将生成的验证码保存到Session
            //session.setAttribute(phone,code);

            //方法2 - 将生成的验证码缓存到Redis中，并且设置有效期为5分钟
            redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES) ;

            return R.success("手机验证码短信发送成功");
        }

        return R.error("短信发送失败");
    }

    /**
     * 移动端用户登录
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){
        log.info(map.toString());

        //获取手机号
        String phone = map.get("phone").toString();

        //获取验证码
        String code = map.get("code").toString();

        //方法1 - 从Session中获取保存的验证码
        //Object codeInSession = session.getAttribute(phone);

        //方法2 - 从Reids中获取保存的验证码
        Object codeInSession = redisTemplate.opsForValue().get(phone);

        ////进行验证码的比对（页面提交的验证码和Session中保存的验证码比对）
        //if(codeInSession != null && codeInSession.equals(code)){
        //    //如果能够比对成功，说明登录成功
        //
        //    LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        //    queryWrapper.eq(User::getPhone,phone);
        //
        //    User user = userService.getOne(queryWrapper);
        //    if(user == null){
        //        //判断当前手机号对应的用户是否为新用户，如果是新用户就自动完成注册
        //        user = new User();
        //        user.setPhone(phone);
        //        user.setStatus(1);
        //        userService.save(user);
        //    }
        //    session.setAttribute("user",user.getId());
        //    //如果登登录成功，删除rdedis中缓存的登录验证码
        //    redisTemplate.delete(phone)
        //    return R.success(user);
        //}
        //return R.error("登录失败");



        // 由于前端无法拿到验证码，这里就放开正常逻辑，直接返回成功了

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone,phone);

        User user = userService.getOne(queryWrapper);
        if(user == null){
            //判断当前手机号对应的用户是否为新用户，如果是新用户就自动完成注册
            user = new User();
            user.setPhone(phone);
            user.setStatus(1);
            userService.save(user);
        }
        session.setAttribute("user",user.getId());
        return R.success(user);

    }

}


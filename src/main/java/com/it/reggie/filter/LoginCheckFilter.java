package com.it.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.it.reggie.common.BaseContext;
import com.it.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否已经登录
 */

@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {

    // 路由匹配器支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        // 向下转型 https://blog.csdn.net/weixin_41066584/article/details/110280455
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 1、获取本次请求的URI
        String requestURI = request.getRequestURI();
        log.info("拦截器到请求：{}", requestURI);
        // 定义不需要处理的路径
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/page/demo/**",
                "/backend/**",
                "/front/**",
                "/user/sendMsg", // 移动端发送短信
                "/user/login" // 移动端登录
        };

        // 2、判断本次请求是否需要处理
        Boolean check = check(urls,requestURI);

        // 3、如果不需要处理则直接放行
        if(check){
            // 放行
            log.info("本次请求{}不需要处理", requestURI);
            filterChain.doFilter(request,response);
            return;
        }

        // 4-1、判断登录状态，已登录，放行
        Long sessionId = (Long) request.getSession().getAttribute("employee");
        if(sessionId != null){
            log.info("用户已登录，用户id为：{}",sessionId);

            //设置用户id到线程中，方便元素就自动填充字段中的id填充
            BaseContext.setCurrentId(sessionId);

            filterChain.doFilter(request,response);
            return;
        }
        // 4-2、移动端判断登录状态，已登录，放行
        Long userSessionId = (Long) request.getSession().getAttribute("user");
        if(userSessionId != null){
            log.info("用户已登录，用户id为：{}",userSessionId);

            //设置用户id到线程中，方便元素就自动填充字段中的id填充
            BaseContext.setCurrentId(userSessionId);

            filterChain.doFilter(request,response);
            return;
        }
        // 5、如未登录则直接返回未登录结果
        log.info("用户未登录");
        // 问题：为啥需要写回一个JSON，而不是和controllter 一样直接写R函数？
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;

    }

    /**
     * 路径匹配，检查本次请求是否放行
     * @param urls
     * @param requestURI
     * @return
     */
    public Boolean check (String[] urls,String requestURI){
        for (String url : urls) {
            Boolean match =PATH_MATCHER.match(url,requestURI);
            if(match){
                return true;
            }
        }
        return false;
    };
}

package com.bjpowernode.crm.settings.controller;

import com.bjpowernode.crm.commons.constant.Constant;
import com.bjpowernode.crm.commons.domain.ReturnObject;
import com.bjpowernode.crm.commons.utils.DateUtils;
import com.bjpowernode.crm.settings.domain.User;
import com.bjpowernode.crm.settings.service.UserService;
import com.bjpowernode.crm.settings.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping("/settings/qx/user/toLogin.do")
    public String toLogin(){
        return "settings/qx/user/login";
    }

    @RequestMapping("/settings/qx/user/login.do")
    @ResponseBody
    public Object login(String loginAct, String loginPwd, String isRemPwd, HttpServletRequest request, HttpSession session, HttpServletResponse response){
        //创建对象
        //UserService userService = new UserServiceImpl();
        //封装参数
        Map<String,Object> map = new HashMap<>();
        map.put("loginAct",loginAct);
        map.put("loginPwd",loginPwd);

        //调用方法 获取到User
        User user = userService.queryUserByLoginActAndPwd(map);
        ReturnObject returnObject = new ReturnObject();

        //根据条件进行判断
        if (user == null){
            //验证失败
            returnObject.setCode("0");
            returnObject.setMessage("用户名或者密码错误");

        }else {
            //表示账号密码正确
            //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String format = DateUtils.formatDateTime(new Date());

            if (format.compareTo(user.getExpireTime()) > 0){//判断时间是否失效 使用dateFormat转换成字符串进行比较
                //时间已经失效
                returnObject.setCode(Constant.RETURN_OBJECT_CODE_FAIL);
                returnObject.setMessage("账号已经过期");
            }else if ("0".equals(user.getLockState())){//判断状态是否被锁定 0被锁定
                //用户被锁定
                returnObject.setCode(Constant.RETURN_OBJECT_CODE_FAIL);
                returnObject.setMessage("用户已被锁定");
            }else if(!user.getAllowIps().contains(request.getRemoteUser())){//判断ip是否受限
                //ip受限
                returnObject.setCode(Constant.RETURN_OBJECT_CODE_FAIL);
                returnObject.setMessage("用户ip受限");
            }
            //验证成功返回消息
            returnObject.setCode(Constant.RETURN_OBJECT_CODE_SUCCESS);
            //將用戶名存進session域中 使用user對象
            session.setAttribute(Constant.USER_SESSION,user);

            //进行免登录实现
            Cookie c1,c2;
            //向浏览器写入cookie
            if ("true".equals(isRemPwd)){
                c1 = new Cookie("loginAct",user.getLoginAct());
                //设置生效时间
                c1.setMaxAge(10*60*24*60);
                c2 = new Cookie("loginPwd",user.getLoginPwd());
                c2.setMaxAge(10*60*24*60);
                response.addCookie(c1);
                response.addCookie(c2);
            }else{
                //删除过期的Cookie
                c1 = new Cookie("loginAct","1");
                c1.setMaxAge(0);
                c2 = new Cookie("loginPwd","1");
                c2.setMaxAge(0);
                response.addCookie(c1);
                response.addCookie(c2);
            }


        }
        return returnObject;
    }

    @RequestMapping("/settings/qx/user/logout.do")
    public String logout(HttpServletResponse response,HttpSession session){
        //删除Cookie
        Cookie c1,c2;
        c1 = new Cookie("loginAct","1");
        c1.setMaxAge(0);
        c2 = new Cookie("loginPwd","1");
        c2.setMaxAge(0);
        response.addCookie(c1);
        response.addCookie(c2);

        //删除Session
        session.invalidate();

        return "redirect:/";//重定向到首页
        //借助SpringMVC进行response.sendRedirect("/crm/") 在非框架时需要手动加上项目名称
    }
}

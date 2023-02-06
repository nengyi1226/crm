package com.xxxx.crm;

import com.alibaba.fastjson.JSON;
import com.xxxx.base.ResultInfo;
import com.xxxx.crm.exceptions.AuthException;
import com.xxxx.crm.exceptions.NoLoginException;
import com.xxxx.crm.exceptions.ParamsException;
import com.xxxx.crm.interceptors.NoLoginInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

@Component
public class GlobalExceptionResolver implements HandlerExceptionResolver {
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        /**
         *   方法返回值类型
         *       视图
         *       json
         *  如何判断方法返回的是视图  还是 json?
         *     约定:如果方法级别配置@ResponseBody  方法响应内容为json  反之 方法响应内容为html页面
         */
        ModelAndView mv=new ModelAndView();
        mv.setViewName("error");
        mv.addObject("code",400);
        mv.addObject("msg","系统异常，请稍后再试...");

        if(ex instanceof NoLoginException){
            mv.setViewName("no_login");
            mv.addObject("msg","用户未登录!");
            mv.addObject("ctx",request.getContextPath());
            return  mv;
        }



        if(handler instanceof  HandlerMethod){
            HandlerMethod hm = (HandlerMethod) handler;
            ResponseBody responseBody= hm.getMethod().getDeclaredAnnotation(ResponseBody.class);
            if(null == responseBody){
                /**
                 * 方法响应内容为视图
                 */
                if(ex instanceof ParamsException){
                    ParamsException pe = (ParamsException) ex;
                    mv.addObject("msg",pe.getMsg());
                    mv.addObject("code",pe.getCode());
                }else  if(ex instanceof AuthException){
                    AuthException ae = (AuthException) ex;
                    mv.addObject("msg",ae.getMsg());
                    mv.addObject("code",ae.getCode());
                }
                return mv;
            }else{
                /**
                 *  方法响应内容为json
                 */
                ResultInfo resultInfo=new ResultInfo();
                resultInfo.setCode(300);
                resultInfo.setMsg("系统错误,请稍后再试...");
                if(ex instanceof  ParamsException){
                    ParamsException pe = (ParamsException) ex;
                    resultInfo.setCode(pe.getCode());
                    resultInfo.setMsg(pe.getMsg());
                }else  if(ex instanceof AuthException){
                    AuthException ae = (AuthException) ex;
                    resultInfo.setCode(ae.getCode());
                    resultInfo.setMsg(ae.getMsg());
                }
                response.setContentType("application/json;charset=utf-8");
                response.setCharacterEncoding("utf-8");
                PrintWriter pw=null;
                try {
                    pw=response.getWriter();
                    pw.write(JSON.toJSONString(resultInfo));
                    pw.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    if(null !=pw){
                        pw.close();
                    }
                }
                return null;
            }
        }else{
            return mv;
        }

    }
}

package com.xxxx.crm.proxy;

import com.xxxx.crm.annotaions.RequirePermission;
import com.xxxx.crm.exceptions.AuthException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import java.util.List;

@Component
@Aspect
public class PermissionProxy {

    @Autowired
    private HttpSession session;


    @Around(value = "@annotation(com.xxxx.crm.annotaions.RequirePermission)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Object result=null;
        List<String> permissions = (List<String>) session.getAttribute("permissions");
        if(null==permissions || permissions.size()==0){
            throw  new AuthException();
        }

        MethodSignature methodSignature= (MethodSignature) pjp.getSignature();
        RequirePermission requirePermission= methodSignature.getMethod().getDeclaredAnnotation(RequirePermission.class);
        if(!(permissions.contains(requirePermission.code()))){
            throw  new AuthException();
        }
        result=pjp.proceed();
        return result;
    }

}

package com.arto.core.intercepter;

import com.arto.event.util.ThreadContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * Created by xiong.j on 2017/2/15.
 */
@Component
@Aspect
public class TransactionMessageAspect implements Ordered, ApplicationContextAware {

    private ApplicationContext applicationContext;

    //切面注解TXAction
    @Pointcut("@annotation(com.arto.core.annotation.TransactionMessage)")
    public void pointcutTransactionMessage(){}

    @Around("pointcutTransactionMessage()")
    public Object doTransactionMessage(ProceedingJoinPoint pjp) throws Throwable {
        if (ThreadContextHolder.getContext()!= null){
            return pjp.proceed();
        }
        try {
            // TODO



            // 执行业务操作
            return pjp.proceed();
        }finally{
            // 清空上下文内容
            ThreadContextHolder.setContext(null);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext= applicationContext;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}

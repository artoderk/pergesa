package com.arto.core.intercepter;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Created by xiong.j on 2017/2/15.
 */
@Component
public class TransactionMessageIntercepter implements ApplicationContextAware {

    private ApplicationContext applicationContext;




    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext= applicationContext;
    }
}

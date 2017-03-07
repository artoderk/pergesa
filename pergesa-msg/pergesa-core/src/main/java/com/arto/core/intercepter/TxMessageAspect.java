package com.arto.core.intercepter;

import com.arto.core.bootstrap.MqClient;
import com.arto.core.event.MqEvent;
import com.arto.core.exception.MqClientException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.Ordered;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.List;

/**
 * 事务消息切面，监控jdbc执行状态，如果执行成功，将未发送的事务消息加入发送队列.
 * (用来提升发送速度，不用等待调度发送)
 *
 * Created by xiong.j on 2017/2/15.
 */
@Component
@Aspect
public class TxMessageAspect implements Ordered, ResourceLoaderAware, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private ResourceLoader resourceLoader;

    @Autowired
    private DataSourceTransactionManager transactionManager;

    //切面注解TXAction
    @Pointcut("@annotation(com.arto.core.annotation.TxMessage)")
    public void pointcutTxMessage(){}

    @Around("pointcutTxMessage()")
    public Object doTransactionMessage(ProceedingJoinPoint pjp) throws Throwable {
        // 获取数据源
        DataSource dataSource = transactionManager.getDataSource();
        // 判断最外层是否开启了写事务
        assertTransactional(dataSource);
        // 获取当前事务连接
        ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
        final Connection conn = conHolder.getConnection();
        // 监控事务提交状态
        Method setterMethod= ReflectionUtils.findMethod(ConnectionHolder.class, "setConnection", Connection.class);
        ReflectionUtils.makeAccessible(setterMethod);
        // 重写commit方法
        ReflectionUtils.invokeMethod(setterMethod, conHolder,
                Proxy.newProxyInstance(resourceLoader.getClassLoader(),
                        new Class<?>[]{Connection.class}, new InvocationHandler() {
                            @Override
                            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                try {
                                    return method.invoke(conn, args);
                                }finally {
                                    // 确保在commit执行后将事务消息加入发送队列中，加快发送速度
                                    if ("commit".equals(method.getName())) {
                                        List<MqEvent> txMessages = TxMessageContextHolder.getTxMessages();
                                        if (txMessages != null) {
                                            MqClient.getPipeline(txMessages.get(0).getType()).offerAll(txMessages);
                                        }
                                        TxMessageContextHolder.clear();
                                    } else if ("rollback".equals(method.getName())) {
                                        TxMessageContextHolder.clear();
                                    }
                                }
                            }
                        }));
        // 执行业务操作
        return pjp.proceed();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext= applicationContext;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader= resourceLoader;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private void assertTransactional(DataSource dataSource) throws Throwable{
        Assert.notNull(dataSource, "Can't get datasource from bean 'transactionManager'.");
        ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
        if (conHolder == null || conHolder.getConnectionHandle()==null || !conHolder.isSynchronizedWithTransaction()) {
            throw new MqClientException("It's not in spring jdbc transaction.");
        }
    }
}

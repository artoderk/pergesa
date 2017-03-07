package com.arto.event.router;

import com.arto.event.storage.EventInfo;
import com.google.common.base.Strings;
import javassist.*;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 根据event类型生成对应的路由类实例
 *
 * Created by xiong.j on 2017/1/5.
 */
@Slf4j
public class PersistentEventRouterFactory {

    private static final ConcurrentMap<String, Class<?>> CLASSES = new ConcurrentHashMap<String, Class<?>>();

    private static final ConcurrentMap<Class<?>, Object> INSTANCES = new ConcurrentHashMap<Class<?>, Object>();

    /**
     * 根据event类型生成对应的路由代理
     *
     * @param eventInfo 事件
     * @return 代理对象
     */
    public static <T> T getProxy(EventInfo eventInfo) throws Throwable {
        return getInstance(getEventClass(eventInfo));
    }

    @SuppressWarnings("unchecked")
    private static <T> T getInstance(Class<?> clazz) throws Throwable {
        if (clazz == null) {
            return null;
        }

        T instance = (T) INSTANCES.get(clazz);
        if (instance == null) {
            INSTANCES.putIfAbsent(clazz, (T) clazz.newInstance());
            instance = (T) INSTANCES.get(clazz);
        }

        return instance;
    }

    private static Class<?> getEventClass(EventInfo eventInfo) throws Throwable{
        if (eventInfo == null || Strings.isNullOrEmpty(eventInfo.getEventType())) {
            return null;
        }

        String className = getClassName(eventInfo.getEventType());
        Class<?> newCls = getProxyClassFromCache(className);
        if (newCls != null) {
            return newCls;
        }

        // Class append
        ClassPool pool = new ClassPool(true);
        pool.appendClassPath(new LoaderClassPath(getClassloader()));
        CtClass cc = pool.makeClass(className + "_RouterStub");
        cc.addInterface(pool.get(PersistentEventRouter.class.getName()));

        // Append single mehtod
        StringBuilder sb = new StringBuilder();
        sb.append("public void router").append("(com.arto.event.storage.EventInfo eventInfo) throws Throwable { ");
        // 根据持久化消息体反序列化成事件
        sb.append(eventInfo.getEventType()).append(" event = (").append(eventInfo.getEventType())
                .append(")(com.alibaba.fastjson.JSON.parseObject(eventInfo.getPayload(), ")
                .append(eventInfo.getEventType()).append(".class)); ");
        // 加入持久化属性
        sb.append(" com.arto.event.bootstrap.EventContext eventContext = new com.arto.event.bootstrap.EventContext(eventInfo);");
        sb.append(" event.setEventContext(eventContext);");
        // 发送事件
        sb.append(" com.arto.event.bootstrap.EventBusFactory.getInstance().post(event); }");

        String methodStr = sb.toString();
        log.info("Create method source:" + methodStr);
        CtMethod mthd = CtNewMethod.make(methodStr, cc);
        cc.addMethod(mthd);
        sb.setLength(0);

        //生成Class
        newCls = cc.toClass();
        CLASSES.putIfAbsent(className, newCls);

        return newCls;
    }

    private static Class<?> getProxyClassFromCache(String className){
        return CLASSES.get(className);
    }

    private static ClassLoader getClassloader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) classLoader = PersistentEventRouterFactory.class.getClassLoader();
        return classLoader;
    }

    private static String getClassName(String eventType) {
        return eventType.substring(eventType.lastIndexOf(".") + 1);
    }
}

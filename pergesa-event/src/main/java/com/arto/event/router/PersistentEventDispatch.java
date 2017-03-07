package com.arto.event.router;

import com.arto.event.service.PersistentEventService;
import com.arto.event.storage.EventInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 持久化事件路由
 *
 * Created by xiong.j on 2017/1/4.
 */
@Slf4j
@Component
public class PersistentEventDispatch {

    @Autowired
    private PersistentEventService persistentEventService;

    /**
     * 持久化事件路由
     *
     * @param eventInfo
     * @return
     */
    public boolean router(EventInfo eventInfo) {
        try{
            // 调用静态代理动态解析事件并路由给指定事件处理器
            PersistentEventRouter proxy = PersistentEventRouterFactory.getProxy(eventInfo);
            proxy.router(eventInfo);
        } catch (Throwable e) {
            log.error("Router event failed.", e);
            fail(eventInfo);
        }
        return true;
    }

    /**
     * 持久化事件路由失败时的处理
     *
     * @param eventInfo
     */
    private void fail(EventInfo eventInfo){
        // 加锁
        persistentEventService.lock(eventInfo);
        // 更新失败状态
        persistentEventService.fail(eventInfo);
    }
}

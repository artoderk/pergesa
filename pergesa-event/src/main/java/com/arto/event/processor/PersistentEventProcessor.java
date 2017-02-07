package com.arto.event.processor;

import com.arto.event.storage.EventInfo;
import com.arto.event.router.PersistentEventRouter;
import com.arto.event.router.PersistentEventRouterFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by xiong.j on 2017/1/4.
 */
@Slf4j
@Component
public class PersistentEventProcessor {

    @Autowired
    private ThreadPoolTaskExecutor executor;

    private Map eventMaps = new ConcurrentHashMap<String, Object>();

    //private Queue<PersistentEvent> eventQueue = new LinkedBlockingQueue<PersistentEvent>();

    public boolean router(EventInfo eventInfo) {
        try{

            //eventQueue.add(eventInfo2PersistenEvent(eventInfo));
            // TODO event.group的设定
            PersistentEventRouter proxy = PersistentEventRouterFactory.getProxy(eventInfo);
            proxy.router(eventInfo);
        } catch (Throwable e) {
            // TODO 错误处理流程，参照事件出错处理
            log.error("Router event failed. ", e);
        }
        return true;
    }


//    public int queueSize(){
//        return eventQueue.size();
//    }
}

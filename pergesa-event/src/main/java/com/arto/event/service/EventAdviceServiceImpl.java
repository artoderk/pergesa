package com.arto.event.service;

import com.arto.event.domain.EventInfo;
import com.arto.event.build.Event;
import com.arto.event.exception.PersistentEventLockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by xiong.j on 2017/1/4.
 */
@Slf4j
@Service("eventAdviceService")
public class EventAdviceServiceImpl implements EventAdviceService {

    @Autowired
    private PersistentEventService persistentEventService;

    /**
     * Event处理前的处理
     *
     * @param event
     * @return
     * @throws
     */
    @Override
    public Event before(Event event) throws Exception{
        if (isPersistentEvent(event)) {
            EventInfo eventInfo = persistentEventService.lock(event.getEventContext().getEventInfo());
            if (eventInfo != null) {
                event.getEventContext().setEventInfo(eventInfo);
            }
        }
        return event;
    }

    /**
     * Event处理后的处理
     *
     * @param event
     */
    @Override
    public void after(Event event){
        log.info("Process event successful. Event=" + event);
        if (isPersistentEvent(event)) {
            persistentEventService.finish(event.getEventContext().getEventInfo());
        }
        // 如果有callback的话，可以在此处处理
    }

    /**
     * Event处理失败时的处理
     *
     * @param event
     * @param throwable
     */
    @Override
    public void fail(Event event, Throwable throwable){
        log.error("Process event failed. Event=" + event, throwable);
        if (isPersistentEvent(event) && !(throwable instanceof PersistentEventLockException)) {
            // 持久化消息 且 错误类型不为加锁失败时
            persistentEventService.fail(event.getEventContext().getEventInfo());
        }
    }

    private boolean isPersistentEvent(Event event) {
        if (event.getEventContext() != null) {
            if (event.getEventContext().getEventInfo() != null) {
                return true;
            }
        }
        return false;
    }
}

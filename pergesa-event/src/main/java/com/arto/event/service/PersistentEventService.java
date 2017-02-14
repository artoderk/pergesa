package com.arto.event.service;

import com.arto.event.bootstrap.Event;
import com.arto.event.exception.EventException;
import com.arto.event.storage.EventInfo;

/**
 * Created by xiong.j on 2017/1/4.
 */
public interface PersistentEventService {

    /**
     * 持久化Event
     *
     * @param event
     * @param type
     * @throws EventException
     */
    public void persist(Event event, String type) throws EventException;

    /**
     * 对持久化Event加锁(JDBC时有效)
     *
     * @param eventInfo
     * @return
     * @throws EventException
     */
    public EventInfo lock(EventInfo eventInfo) throws EventException;

    /**
     * 持久化Event处理失败时的处理
     *
     * @param eventInfo
     */
    public void fail(EventInfo eventInfo);

    /**
     * 持久化Event处理成功时的处理
     *
     * @param eventInfo
     */
    public void finish(EventInfo eventInfo);

}

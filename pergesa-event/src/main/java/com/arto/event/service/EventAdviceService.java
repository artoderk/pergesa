package com.arto.event.service;

import com.arto.event.bootstrap.Event;

/**
 * Created by xiong.j on 2017/1/4.
 */
public interface EventAdviceService {

    /**
     * Event处理前时的处理
     *
     * @param event
     * @return
     * @throws
     */
    public Event before(Event event) throws Exception;

    /**
     * Event处理后时的处理
     *
     * @param event
     */
    public void after(Event event);

    /**
     * Event处理失败时的处理
     *
     * @param event
     * @param throwable
     */
    public void fail(Event event, Throwable throwable);

}

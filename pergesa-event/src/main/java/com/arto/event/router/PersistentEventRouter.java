package com.arto.event.router;

import com.arto.event.domain.EventInfo;

/**
 * Created by xiong.j on 2017/1/5.
 */
public interface PersistentEventRouter<T> {

    void router(EventInfo eventInfo) throws Throwable;

}

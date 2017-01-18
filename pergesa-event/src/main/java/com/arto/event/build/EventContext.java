package com.arto.event.build;

import com.arto.event.storage.EventInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by xiong.j on 2016/12/28.
 */
@Setter
@Getter
@ToString
public class EventContext {

    /** 持久化消息DB对象 */
    private EventInfo eventInfo;

    public EventContext(){}

    public EventContext(EventInfo eventInfo) {
        this.eventInfo = eventInfo;
    }
}

package com.arto.event.build;

import com.arto.event.domain.EventInfo;
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

    private EventInfo eventInfo;

    public EventContext(){}

    public EventContext(EventInfo eventInfo) {
        this.eventInfo = eventInfo;
    }
}

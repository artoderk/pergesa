package com.arto.core.event;

import com.arto.event.bootstrap.Event;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by xiong.j on 2017/1/11.
 */
@Setter
@Getter
@ToString(callSuper = true)
public class MqEvent extends Event {

    /** 目的地 */
    private String destination;

    /** 优先级 */
    private int priority = 1;

}

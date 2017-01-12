package com.arto.core.event;

import com.arto.event.build.Event;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by xiong.j on 2017/1/11.
 */
@Setter
@Getter
public class MqEvent extends Event {

    /** 目的地 */
    private String destination;

    /** 对应生产者acks */
    private int priority = -1;

    /** 消息内容 */
    private String payload;

}

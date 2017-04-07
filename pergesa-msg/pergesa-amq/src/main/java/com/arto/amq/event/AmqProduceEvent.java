package com.arto.amq.event;

import com.arto.core.common.MqTypeEnum;
import com.arto.core.event.MqEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by xiongjie on 2016/12/21.
 */
@Setter
@Getter
@ToString(callSuper = true)
public class AmqProduceEvent extends MqEvent {

    /** 是否持久化 */
    private int deliveryMode = -1;

    /** 消息存活时间 */
    private long timeToLive = -1;

    public AmqProduceEvent(){
        this.setType(MqTypeEnum.ACTIVEMQ.getMemo());
        this.setGroup(AmqProduceEvent.class);
    }

    public AmqProduceEvent(String dest){
        this();
        this.setDestination(dest);
    }
}

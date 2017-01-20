package com.arto.kafka.event;

import com.arto.core.consumer.MqListener;
import com.arto.core.event.MqEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by xiongjie on 2016/12/21.
 */
@Setter
@Getter
@ToString
public class KafkaConsumeEvent extends MqEvent {

    /** 消息监听者接收的消息类型，框架会据此反序列化消息 */
    private Class type;

    /** 消息监听者 */
    private MqListener listener;

}

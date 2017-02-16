package com.arto.core.consumer;

import com.arto.core.bootstrap.MqConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by xiong.j on 2017/1/11.
 */
@Setter
@Getter
@ToString
public class ConsumerConfig extends MqConfig{

    /** 消息类型反序列化类 */
//    @Deprecated
//    private Class deserializer;

    /** 消息处理类 */
    private MqListener listener;

    /** 消费优先级
     * 1:重要消息(单条消息处理完成后消费标识同步提交，为了避免阻塞后续消息，消息处理出错 > 3次后该消息入库，等待调度任务重试处理)
     * 2:普通消息(单条消息处理完成后消费标识同步提交，消息处理出错 > 3次后?)
     * 3:不重要消息(消费标识异步提交, 处理出错后将会丢失该条消息) */
    private int priority = 1;
}

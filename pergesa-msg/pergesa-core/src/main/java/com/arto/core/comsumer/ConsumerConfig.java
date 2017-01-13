package com.arto.core.comsumer;

import com.arto.core.producer.MqCallback;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by xiong.j on 2017/1/11.
 */
@Setter
@Getter
@ToString
public class ConsumerConfig {

    /** 消息中间件类型 */
    private String type;

    /** 目的地 */
    private String destination;

    /** 消息优先级 1:非常重要(不会丢失) 2:重要(极端情况下丢失) 3:不重要(异步发送) */
    private int priority = 1;

    /** 发送完成后的回调 */
    private MqCallback callback;

    /** 是否启用消息二阶段提交，启用后确保DB和消息中间件的原子性, 目前采用客户端模拟方案 */
    private boolean isTransaction;

}

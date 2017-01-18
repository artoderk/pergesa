package com.arto.core.producer;

import com.arto.core.build.MqConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by xiong.j on 2017/1/11.
 */
@Setter
@Getter
@ToString
public class ProducerConfig extends MqConfig {

    /** 消息优先级 1:非常重要(不会丢失) 2:重要(极端情况下丢失) 3:不重要(异步发送) */
    private int priority = 1;

    /** 发送完成后的回调，优先级"1"和"2"时禁止设置 */
    private MqCallback callback;

    /** 是否启用消息二阶段提交，启用后确保DB和消息中间件的原子性, 目前采用客户端模拟方案 */
    private boolean isTransaction;

}


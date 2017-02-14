package com.arto.core.producer;

import com.arto.core.bootstrap.MqConfig;
import com.arto.core.common.MessagePriorityEnum;
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

    /** 消息优先级 1:事务消息 2:非事务重要消息 3:普通消息 */
    private MessagePriorityEnum priority = MessagePriorityEnum.HIGH;

    /** 发送完成后的回调，优先级"1:事务消息"时禁止设置 */
    private MqCallback callback;

    /** 是否启用消息二阶段提交，启用后确保DB和消息中间件的原子性, 目前采用客户端模拟方案 */
    private boolean isTransaction;

}


package com.arto.core.producer;

import com.arto.core.common.MessageRecord;
import com.arto.core.exception.MqClientException;

/**
 * 消息中件间客户端通用生产者接口。
 *
 * Created by xiong.j on 2017/1/11.
 */
public interface MqProducer<T> {

    /**
     * 发送消息(简化方法，非事务类消息可以使用直接使用此方法发送)
     *
     * @param message
     * @throws MqClientException
     */
    void send(T message) throws MqClientException;

    /**
     * 发送消息(定制发送，可配置发送参数)
     * 注:事务消息必须设置"业务凭证流水号"和"业务类型"，以便发送异常时追踪排错
     *
     * @param record
     * @throws MqClientException
     */
    void send(MessageRecord record) throws MqClientException;

    /**
     * 发送非事务消息(开启事务发送后，可使用此方法发送非事务消息)
     *
     * @param record
     * @throws MqClientException
     */
    void sendNonTx(MessageRecord record) throws MqClientException;
}

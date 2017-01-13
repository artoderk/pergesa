package com.arto.core.producer;

import com.arto.core.common.MessageRecord;

/**
 * 消息中件间客户端生产者接口。
 * 注意：在使用基于客户端的消息二阶段提交时，send方法和db操作必须处理同一事务中。
 *
 * Created by xiong.j on 2017/1/11.
 */
public interface MqProducer<T> {

    void send(T message) throws Exception;

    void send(String key, T message) throws Exception;

    void send(String key, int partition, T message) throws Exception;

    /**
     *
     *
     * @param record
     * @throws Exception
     */
    void send(MessageRecord record) throws Exception;
}

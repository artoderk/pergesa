package com.arto.core.consumer;

import com.arto.core.common.MessageRecord;

/**
 * Created by xiong.j on 2017/1/16.
 */
public interface MqListener<T> {

    /**
     * 收到消息时的实际处理逻辑
     *
     * @param record
     */
    void onMessage(MessageRecord<T> record);

    /**
     * 该消息是否已经处理，为True则跳过此消息
     *
     * @param record
     */
    boolean checkRedeliver(MessageRecord<T> record);

}

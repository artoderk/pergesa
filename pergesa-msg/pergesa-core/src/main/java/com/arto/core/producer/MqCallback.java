package com.arto.core.producer;

import com.arto.core.common.MessageRecord;
import com.arto.event.bootstrap.Event;
import com.arto.event.bootstrap.EventCallback;

/**
 * Created by xiong.j on 2017/1/11.
 */
public abstract class MqCallback implements EventCallback {

    /**
     * 异步事件处理完成时的回调接口
     *
     * @param event
     */
    public void onCompletion(Event event){
        onCompletion((MessageRecord)event.getPayload());
    }

    /**
     * 异步消息处理完成时的回调接口
     *
     * @param record
     */
    protected abstract void onCompletion(MessageRecord record);

}

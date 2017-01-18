package com.arto.core.comsumer;

/**
 * Created by xiong.j on 2017/1/16.
 */
public interface MqListener<T> {

    /**
     * 收到消息时的实际处理逻辑
     *
     * @param t
     */
    void onMessage(T t);

    /**
     * 该消息是否已经处理，为True则跳过此消息
     *
     * @param t
     */
    boolean checkRedeliver(T t);

}

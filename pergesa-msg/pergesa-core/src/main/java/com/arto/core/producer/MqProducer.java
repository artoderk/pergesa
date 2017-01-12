package com.arto.core.producer;

/**
 * Created by xiong.j on 2017/1/11.
 */
public interface MqProducer<T> {

    public void send(T message) throws Exception;

    public void send(String key, T message) throws Exception;

    public void send(String key, int partition, T message) throws Exception;
}

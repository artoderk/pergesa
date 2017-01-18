package com.arto.core.comsumer;

/**
 * Created by xiong.j on 2017/1/11.
 */
public interface MqConsumer {

    void receive(final Class type, final MqListener listener);

    void receiveWithParallel(final Class type, final int numThreads, final MqListener listener);

}

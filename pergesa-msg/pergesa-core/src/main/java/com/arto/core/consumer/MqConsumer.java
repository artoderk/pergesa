package com.arto.core.consumer;

/**
 * @Deprecated
 * TODO 生产消息者的时候直接设定配置，此类可能废弃.
 *
 * Created by xiong.j on 2017/1/11.
 */
public interface MqConsumer {

    /**
     * 默认消息模式
     * Kafka: kafka在此模式下将每个主题的每个分区启动一个线程"顺序"消费
     * ActiveMq: TODO
     *
     * @param type 消息监听者接收的消息类型，框架会据此反序列化消息
     * @param listener 消息监听者
     */
    @Deprecated
    void receive(final Class type, final MqListener listener);

    /**
     * 并行消息模式(TODO 第一版不实现)
     * Kafka: kafka在此模式下将每个主题启动指定数量的线程池“乱序”消费
     * ActiveMq: TODO
     *
     * @param type 消息监听者接收的消息类型，框架会据此反序列化消息
     * @param numThreads 线程数量
     * @param listener 消息监听者
     */
    @Deprecated
    void receiveWithParallel(final Class type, final int numThreads, final MqListener listener);

}

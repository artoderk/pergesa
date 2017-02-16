package com.arto.core.bootstrap;

import com.arto.core.consumer.ConsumerConfig;
import com.arto.core.consumer.MqConsumer;
import com.arto.core.producer.MqProducer;
import com.arto.core.producer.ProducerConfig;

/**
 * Created by xiong.j on 2017/1/11.
 */
public interface MqFactory {

    /**
     * 根据生产者配置文件生成一个新的生产者
     *
     * @param config
     * @return
     */
    MqProducer buildProducer(ProducerConfig config);

    /**
     * 根据消费者配置文件生成一个新的消费者
     *
     * @param config
     * @return
     */
    MqConsumer buildConsumer(ConsumerConfig config);

    /**
     * 获取当前的实例使用的消息中件间类型
     *
     * @return
     */
    String getMqType();

    /**
     * 销毁所有的生产者和消息者
     */
    void destroy();
}

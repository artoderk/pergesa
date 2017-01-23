package com.arto.kafka.consumer.binding;

import com.arto.core.consumer.ConsumerConfig;
import com.arto.kafka.common.Constants;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by xiong.j on 2017/1/12.
 */
@Setter
@Getter
@ToString
public class KafkaConsumerConfig extends ConsumerConfig {

    /** 并发线程数量 */
    private int numThreads;

    public KafkaConsumerConfig(String destination) {
        this.setType(Constants.KAFKA);
        this.setDestination(destination);
    }

    public KafkaConsumerConfig(String destination, int priority) {
        this.setType(Constants.KAFKA);
        this.setDestination(destination);
        this.setPriority(priority);
    }
}

package com.arto.kafka.consumer.binding;

import com.arto.core.consumer.ConsumerConfig;
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

}

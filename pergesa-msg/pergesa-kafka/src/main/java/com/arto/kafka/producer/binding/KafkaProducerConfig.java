package com.arto.kafka.producer.binding;

import com.arto.core.producer.ProducerConfig;
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
public class KafkaProducerConfig extends ProducerConfig {

    public KafkaProducerConfig(String destination) {
        this.setType(Constants.KAFKA);
        this.setDestination(destination);
    }

    public KafkaProducerConfig(String destination, int priority) {
        this.setType(Constants.KAFKA);
        this.setDestination(destination);
        this.setPriority(priority);
    }

    public KafkaProducerConfig(String destination, boolean isTransaction) {
        this.setType(Constants.KAFKA);
        this.setDestination(destination);
        this.setTransaction(isTransaction);
    }
}

package com.arto.kafka.producer.binding;

import com.arto.core.common.MessagePriorityEnum;
import com.arto.core.common.MqTypeEnum;
import com.arto.core.producer.ProducerConfig;
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
        this.setType(MqTypeEnum.KAFKA);
        this.setDestination(destination);
    }

    public KafkaProducerConfig(String destination, MessagePriorityEnum priority) {
        this.setType(MqTypeEnum.KAFKA);
        this.setDestination(destination);
        this.setPriority(priority);
        if (priority == MessagePriorityEnum.HIGH) {
            this.setTransaction(true);
        }
    }
}

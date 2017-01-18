package com.arto.kafka.consumer;

import com.arto.event.util.ThreadUtil;
import com.arto.kafka.consumer.binding.KafkaConsumerConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Kafka消息处理线程
 *
 * Created by xiong.j on 2017/1/18.
 */
@Slf4j
public class KafkaConsumerThread implements Callable{

    /** Kafka消费者 */
    private KafkaConsumer<String, String> consumer;

    /** Topic消费者配置 */
    private KafkaConsumerConfig config;

    /** Topic拉取的消息(单个分区) */
    private List<ConsumerRecord<String, String>> records;

    /** 连续消费出错次数 */
    private int errCount;

    public KafkaConsumerThread(final KafkaConsumer<String, String> consumer, final KafkaConsumerConfig config
            , final List<ConsumerRecord<String, String>> records) {
        this.consumer = consumer;
        this.config = config;
        this.records = records;
    }

    @Override
    public Object call() throws Exception {
        TopicPartition topicPartition = new TopicPartition(records.get(0).topic(), records.get(0).partition());

        for (ConsumerRecord<String, String> record : records) {
            try {
                config.getListener().onMessage(record);
            } catch (Exception e) {
                errCount++;
            }
            if (errCount > 3) {
                // 消息处理错误次数大于3的情况下，暂停处理1分钟
                ThreadUtil.sleep(60000, Thread.currentThread(), log);
                errCount = 0;
            }
        }

        // TODO 使用单独线程管理消费的暂停与恢复
        consumer.resume(Collections.singleton(topicPartition));
        return null;
    }
}

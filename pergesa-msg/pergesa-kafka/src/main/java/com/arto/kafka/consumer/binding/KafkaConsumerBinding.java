package com.arto.kafka.consumer.binding;

import com.arto.core.consumer.MqConsumer;
import com.arto.core.consumer.MqListener;
import com.arto.event.util.SpringContextHolder;
import com.arto.kafka.consumer.KafkaConsumerThread;
import com.arto.kafka.consumer.KafkaMessageConsumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Collections.singleton;

/**
 * Created by xiong.j on 2017/1/17.
 */
@Slf4j
public class KafkaConsumerBinding implements MqConsumer {

    /** Kafka消费者配置 */
    private final KafkaConsumerConfig config;

    /** Kafka消费者 */
    private final KafkaMessageConsumer consumer;

    /** 消息消费线程关闭Flag */
    private final AtomicBoolean closeFlag = new AtomicBoolean(false);

    /** 消息消费线程 */
    private ConsumerWithTopicThread localThread;

    public KafkaConsumerBinding(KafkaConsumerConfig config) {
        this.config = config;
        this.consumer = SpringContextHolder.getBean("kafkaMessageConsumer");
        consumer.subscribe(this);
    }

    @Override
    @Deprecated
    public void receive(Class type, MqListener listener) {
        //config.setDeserializer(type);
        config.setListener(listener);
        consumer.subscribe(this);
    }

    @Override
    @Deprecated
    public void receiveWithParallel(Class type, int numThreads, MqListener listener) {
        //config.setDeserializer(type);
        config.setListener(listener);
        config.setNumThreads(numThreads);
        consumer.subscribe(this);
    }

    /**
     * 开启消费线程
     *
     * @param consumer
     * @param topicQueue
     */
    public void start(final KafkaConsumer<java.lang.String, String> consumer
            , final LinkedBlockingQueue<List<ConsumerRecord<String, String>>> topicQueue) {
        if (localThread == null) {
            localThread = new ConsumerWithTopicThread(consumer, topicQueue);
            new Thread(localThread, "ConsumerWithTopicThread_" + config.getDestination()).start();
        }
    }

    /**
     * 关闭
     */
    public void close() {
        if (localThread == null) {
            closeFlag.set(true);
            localThread.destroy();
        }
    }

    /**
     * 获取绑定的配置
     *
     * @return
     */
    public KafkaConsumerConfig getConfig() {
        return config;
    }

    /**
     * Topic消息处理线程
     */
    private class ConsumerWithTopicThread implements Runnable {

        /** Kafka消费者 */
        private KafkaConsumer<String, String> consumer;

        /** Topic拉取的消息(整个Topic) */
        private LinkedBlockingQueue<List<ConsumerRecord<String, String>>> topicQueue;

        /** 消息处理线程池 */
        private ExecutorService executor;

        /**
         * Topic消息的处理线程
         *
         * @param consumer
         * @param topicQueue
         */
        public ConsumerWithTopicThread(final KafkaConsumer<String, String> consumer
                , final LinkedBlockingQueue<List<ConsumerRecord<String, String>>> topicQueue) {
            this.consumer = consumer;
            this.topicQueue = topicQueue;
            executor = Executors.newCachedThreadPool();
        }

        @Override
        public void run() {
            while (!closeFlag.get()) {
                List<ConsumerRecord<String, String>> records;
                TopicPartition topicPartition;
                try {
                    // 获取拉取到的消息
                    records = topicQueue.poll(10, TimeUnit.SECONDS);
                    if (records != null) {
                        // 暂停当前分区的消息拉取直到消息处理完成
                        topicPartition = new TopicPartition(config.getDestination(), records.get(0).partition());
                        consumer.pause(singleton(topicPartition));
                        log.info("Consumer pause:" + topicPartition);
                        // 将消息按分区消息
                        executor.submit(new KafkaConsumerThread(consumer, config, records));
                    }
                } catch (InterruptedException e) {
                    log.warn("Consumer thread interrupted. ", e);
                } catch (Throwable e) {
                    log.warn("Topic records process failed. Topic=" + config.getDestination(), e);
                }
            }
        }

        /**
         * 销毁线程池
         */
        public void destroy() {
            executor.shutdown();
            log.info("Kafka consumer thread pool is destroyed. topic=" + config.getDestination());
        }
    }
}

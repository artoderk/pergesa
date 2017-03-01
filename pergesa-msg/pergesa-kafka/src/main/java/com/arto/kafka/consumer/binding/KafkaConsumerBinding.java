package com.arto.kafka.consumer.binding;

import com.arto.core.consumer.MqConsumer;
import com.arto.core.consumer.MqListener;
import com.arto.event.util.SpringContextHolder;
import com.arto.kafka.consumer.KafkaConsumerThread;
import com.arto.kafka.consumer.KafkaConsumerWrapper;
import com.arto.kafka.consumer.KafkaMessageConsumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private volatile ConsumerWithTopicThread localThread;

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
     * @param consumerWrapper
     * @param topicQueue
     */
    public void start(final KafkaConsumerWrapper<String, String> consumerWrapper
            , final LinkedBlockingQueue<List<ConsumerRecord<String, String>>> topicQueue) {
        if (localThread == null) {
            localThread = new ConsumerWithTopicThread(consumerWrapper, topicQueue);
            new Thread(localThread, "ConsumerWithTopicThread_" + config.getDestination()).start();
        }
    }

    /**
     * 关闭
     */
    public void close() {
        if (localThread != null) {
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

        /** Kafka消费者包装类 */
        private KafkaConsumerWrapper<String, String> consumerWrapper;

        /** Topic拉取的消息(整个Topic) */
        private LinkedBlockingQueue<List<ConsumerRecord<String, String>>> topicQueue;

        /** 消息处理线程池 */
        private ThreadPoolExecutor executor;

        /**
         * Topic消息的处理线程
         *
         * @param consumerWrapper
         * @param topicQueue
         */
        public ConsumerWithTopicThread(final KafkaConsumerWrapper<String, String> consumerWrapper
                , final LinkedBlockingQueue<List<ConsumerRecord<String, String>>> topicQueue) {
            this.consumerWrapper = consumerWrapper;
            this.topicQueue = topicQueue;
            executor = new ThreadPoolExecutor(3, 3,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(),
                    new ThreadPoolExecutor.CallerRunsPolicy());
        }

        @Override
        public void run() {
            while (!closeFlag.get()) {
                List<ConsumerRecord<String, String>> records;
                try {
                    // 获取拉取到的消息
                    records = topicQueue.poll(50, TimeUnit.MILLISECONDS);
                    if (records != null) {
                        // 将消息按分区消息
                        executor.submit(new KafkaConsumerThread(consumerWrapper, config, records));
                        System.out.println("Thread pool active: " + executor.getActiveCount());
                    }
                } catch (InterruptedException e) {
                    log.warn("ConsumerWithTopicThread interrupted. ", e);
                } catch (Throwable e) {
                    log.warn("ConsumerWithTopicThread process failed. Topic=" + config.getDestination(), e);
                }
            }
        }

        /**
         * 销毁线程池
         */
        public void destroy() {
            log.info("Kafka consumer thread pool is destroyed. topic=" + config.getDestination());
            executor.shutdown();
        }
    }
}

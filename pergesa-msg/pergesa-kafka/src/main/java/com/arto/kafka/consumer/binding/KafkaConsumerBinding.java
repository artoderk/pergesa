package com.arto.kafka.consumer.binding;

import com.arto.core.consumer.MqConsumer;
import com.arto.core.consumer.MqListener;
import com.arto.event.util.SpringContextHolder;
import com.arto.event.util.SpringThreadPoolUtil;
import com.arto.kafka.consumer.KafkaConsumerThread;
import com.arto.kafka.consumer.KafkaConsumerWrapper;
import com.arto.kafka.consumer.KafkaMessageConsumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
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

    /** 消费线程关闭Flag */
    private final AtomicBoolean closeFlag = new AtomicBoolean(false);

    /** 消费线程 */
    private volatile ConsumerWithTopicThread localThread;

    /** 消费者重分配监听器 */
    private final HandleRebalanceListener rebalanceListener = new HandleRebalanceListener();

    public KafkaConsumerBinding(KafkaConsumerConfig config) {
        this.config = config;
        this.consumer = SpringContextHolder.getBean("kafkaMessageConsumer");
        consumer.subscribe(this, rebalanceListener);
    }

    @Override
    @Deprecated
    public void receive(Class type, MqListener listener) {
        //config.setDeserializer(type);
        config.setListener(listener);
        consumer.subscribe(this, rebalanceListener);
    }

    @Override
    @Deprecated
    public void receiveWithParallel(Class type, int numThreads, MqListener listener) {
        //config.setDeserializer(type);
        config.setListener(listener);
        config.setNumThreads(numThreads);
        consumer.subscribe(this, rebalanceListener);
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
     * 关闭消费线程
     */
    public void close() {
        if (localThread != null) {
            closeFlag.set(true);
//            localThread.destroy();
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

    private void commitOffsetImmediately(){
        if (localThread != null) {
            localThread.commitOffsetImmediately();
        }
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
        private ExecutorService executor;

        /** 消息处理线程列表 */
        private List<WeakReference<KafkaConsumerThread>> threadList;

        /**
         * Topic消息的处理线程
         *
         * @param consumerWrapper
         * @param topicQueue
         */
        ConsumerWithTopicThread(final KafkaConsumerWrapper<String, String> consumerWrapper
                , final LinkedBlockingQueue<List<ConsumerRecord<String, String>>> topicQueue) {
            this.consumerWrapper = consumerWrapper;
            this.topicQueue = topicQueue;
//            executor = new ThreadPoolExecutor(config.getNumThreads(), config.getNumThreads(),
//                    0L, TimeUnit.MILLISECONDS,
//                    new LinkedBlockingQueue<Runnable>(),
//                    new ThreadPoolExecutor.CallerRunsPolicy());
            executor = SpringThreadPoolUtil.getNewPool(config.getDestination(), config.getNumThreads(), config.getNumThreads()
                    , 100, null).getThreadPoolExecutor();
            threadList = new LinkedList<WeakReference<KafkaConsumerThread>>();
        }

        @Override
        public void run() {
            while (!closeFlag.get()) {
                List<ConsumerRecord<String, String>> records;
                try {
                    // 获取拉取到的消息
                    records = topicQueue.poll(200, TimeUnit.MILLISECONDS);
                    if (records != null) {
                        // 将消息按分区消息
                        executor.submit(createConsumerThread(records));
                    }

                } catch (InterruptedException e) {
                    log.warn("ConsumerWithTopicThread interrupted. ", e);
                } catch (Throwable e) {
                    log.warn("ConsumerWithTopicThread process failed. Topic:" + config.getDestination(), e);
                }
            }
        }

        /**
         * 立即提交消费标识
         */
        void commitOffsetImmediately(){
            for (WeakReference<KafkaConsumerThread> weakReference : threadList) {
                KafkaConsumerThread thread = weakReference.get();
                if (thread != null) {
                    thread.commitOffsetImmediately();
                }
            }
        }

        /**
         * 销毁线程池
         */
//        void destroy() {
//            log.info("Kafka consumer thread pool is destroyed. topic:" + config.getDestination());
//            executor.shutdown();
//        }

        private KafkaConsumerThread createConsumerThread(List<ConsumerRecord<String, String>> records){
            KafkaConsumerThread thread = new KafkaConsumerThread(consumerWrapper, config, records);
            threadList.add(new WeakReference<KafkaConsumerThread>(thread));
            return thread;
        }

    }

    private class HandleRebalanceListener implements ConsumerRebalanceListener {

        @Override
        public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
            commitOffsetImmediately();
            log.info("Before rebalance, commit offset once. topic:" + config.getDestination());
        }

        @Override
        public void onPartitionsAssigned(Collection<TopicPartition> partitions) {

        }

    }

}

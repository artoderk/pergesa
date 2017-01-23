package com.arto.kafka.consumer;

import com.arto.event.util.ThreadUtil;
import com.arto.kafka.consumer.binding.KafkaConsumerBinding;
import com.arto.kafka.consumer.binding.KafkaConsumerConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by xiong.j on 2017/1/16.
 */
@Slf4j
@Component
public class KafkaMessageConsumer {

    /** 消费者者工厂 */
    @Autowired
    private KafkaMessageConsumerFactory factory;

    /** 消息拉取线程 */
    private PollThread pollThread;

    /** 消息拉取线程关闭Flag */
    private final AtomicBoolean closeFlag = new AtomicBoolean(false);

    /** Topic订阅配置集合 */
    private final ConcurrentMap<String, KafkaConsumerBinding> bindingMap = new ConcurrentHashMap<String, KafkaConsumerBinding>();

    /**
     * 初始化
     */
    @PostConstruct
    public void init(){
        try {
            pollThread = new PollThread(factory.getConsumer());
            new Thread(pollThread).start();
        } catch (Exception e) {
            log.error("kafka consumer init failed.");
        }

    }

    /**
     * 订阅Topic
     *
     * @param kafkaConsumerBinding
     */
    public void subscribe(final KafkaConsumerBinding kafkaConsumerBinding) {
        bindingMap.put(kafkaConsumerBinding.getConfig().getDestination(), kafkaConsumerBinding);
        pollThread.subscribe(kafkaConsumerBinding);
    }

    /**
     * 销毁
     */
    @PreDestroy
    public void destroy(){
        closeFlag.set(true);
        for(Map.Entry<String, KafkaConsumerBinding> entry : bindingMap.entrySet()){
            entry.getValue().close();
        }
    }

    /**
     * 根据主题获取对应的配置
     *
     * @param topic
     * @return
     */
    public KafkaConsumerConfig getConfig(String topic) {
        return bindingMap.get(topic).getConfig();
    }

    /**
     * Kafka消息拉取线程，一个线程对应多个Topic
     */
    private class PollThread implements Runnable{

        /** 消费者 */
        private KafkaConsumer<String, String> consumer;

        /** 消费者订阅的Topic集合 */
        private final Collection<String> topic;

        /** 拉取的消息集合(以Topic分类) */
        private Map<String, LinkedBlockingQueue<List<ConsumerRecord<String, String>>>> topicRecords
                = new ConcurrentHashMap<String, LinkedBlockingQueue<List<ConsumerRecord<String, String>>>>();

        /**
         * Kafka消息拉取线程构造方法
         *
         * @param consumer
         */
        public PollThread(final KafkaConsumer<String, String> consumer){
            this.consumer = consumer;
            topic = new ArrayList<String>();
        }

        /**
         * 订阅Topic
         *
         * @param kafkaConsumerBinding
         */
        public synchronized void subscribe(KafkaConsumerBinding kafkaConsumerBinding) {
            // 更新订阅的Topic集合
            topic.add(kafkaConsumerBinding.getConfig().getDestination());
            // 初始化Topic消费线程
            LinkedBlockingQueue<List<ConsumerRecord<String, String>>> topicQueue
                    = new LinkedBlockingQueue<List<ConsumerRecord<String, String>>>();
            topicRecords.put(kafkaConsumerBinding.getConfig().getDestination(), topicQueue);
            // TODO 应改传入封装后的Consumer，隔离一此方法的访问，以后优化
            kafkaConsumerBinding.start(consumer, topicQueue);
            // 更新拉取的Topic
            consumer.subscribe(topic);
        }

        @Override
        public void run() {
            // 延迟一定时间等系统启动后再开始消费
            ThreadUtil.sleep(1000, Thread.currentThread(), log);

            while (!closeFlag.get()) {
                synchronized (this) {
                    try {
                        if (consumer.subscription().size() > 0) {
                            // 拉取消息
                            ConsumerRecords<String, String> records = consumer.poll(500);
                            for (TopicPartition partition : records.partitions()) {
                                // 将拉取到的消息按Topic分类
                                topicRecords.get(partition.topic()).put(records.records(partition));
                            }
                            System.out.println("####### poll message size:" + records.count());
                        }
                    } catch (Throwable e) {
                        log.error("poll message failed.", e);
                    }
                }
                // 休眠500毫秒
                ThreadUtil.sleep(5000, Thread.currentThread(), log);
            }
        }
    }

}

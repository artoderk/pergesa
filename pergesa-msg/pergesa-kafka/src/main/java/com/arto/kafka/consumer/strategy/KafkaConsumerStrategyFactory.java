package com.arto.kafka.consumer.strategy;

import com.arto.event.bootstrap.EventBusFactory;

import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 消费策略工厂
 *
 * Created by xiong.j on 2017/1/20.
 */
public class KafkaConsumerStrategyFactory {

    private static volatile KafkaConsumerStrategyFactory instance;

    private final ConcurrentMap<Integer, SoftReference<KafkaConsumerStrategy>> strategyMap
            = new ConcurrentHashMap<Integer, SoftReference<KafkaConsumerStrategy>>(3);

    public static KafkaConsumerStrategyFactory getInstance(){
        if (null == instance) {
            synchronized (EventBusFactory.class) {
                if (null == instance) {
                    instance = new KafkaConsumerStrategyFactory();
                }
            }
        }
        return instance;
    }

    /**
     * 根据消费优先级生成不同的消费策略
     *
     * @param priority
     * @return
     */
    public KafkaConsumerStrategy getStrategy(final int priority){
        if (strategyMap.containsKey(priority)) {
            if (strategyMap.get(priority) != null) {
                return strategyMap.get(priority).get();
            }
        }
        return createStrategy(priority);
    }

    private KafkaConsumerStrategy createStrategy(final int priority) {
        if (strategyMap.containsKey(priority)) {
            if (strategyMap.get(priority) != null) {
                return strategyMap.get(priority).get();
            }
        }

        KafkaConsumerStrategy strategy;
        switch (priority) {
            case 1:
                // 重要消息，不能容忍消息丢失
                strategy = new KafkaConsumerDefaultStrategy();
                break;
//                case 2:
//                    // TODO 其它消费优先级待实现
//                    break;
            case 3:
                // 不重要消息，可容忍消息丢失
                strategy = new KafkaConsumerLowPriorityStrategy();
                break;
            default:
                strategy = new KafkaConsumerDefaultStrategy();
        }
        strategyMap.put(priority, new SoftReference<KafkaConsumerStrategy>(strategy));
        return strategy;
    }

}

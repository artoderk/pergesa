package com.arto.kafka.consumer.strategy;

import com.arto.event.build.EventBusFactory;

import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 消费策略工厂
 *
 * Created by xiong.j on 2017/1/20.
 */
public class KConsumerStrategyFactory {

    private static volatile KConsumerStrategyFactory instance;

    private final ConcurrentMap<Integer, SoftReference<KConsumerStrategy>> strategyMap
            = new ConcurrentHashMap<Integer, SoftReference<KConsumerStrategy>>(3);

    public static KConsumerStrategyFactory getInstance(){
        if (null == instance) {
            synchronized (EventBusFactory.class) {
                if (null == instance) {
                    instance = new KConsumerStrategyFactory();
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
    public KConsumerStrategy getStrategy(final int priority){
        if (strategyMap.containsKey(priority)) {
            return strategyMap.get(priority).get();
        } else {
            return createStrategy(priority);
        }
    }

    private KConsumerStrategy createStrategy(final int priority) {
        if (strategyMap.containsKey(priority)) {
            return strategyMap.get(priority).get();
        } else {
            KConsumerStrategy strategy;
            switch (priority) {
                case 1:
                    // 重要消息，不能容忍消息丢失
                    strategy = new KConsumerDefaultStrategy();
                    break;
//                case 2:
//                    // TODO 其它消费优先级待实现
//                    break;
//                case 3:
//                    // 不重要消息，可容忍消息丢失
//                    break;
                default:
                    strategy = new KConsumerDefaultStrategy();
            }
            strategyMap.put(priority, new SoftReference<KConsumerStrategy>(strategy));
            return strategy;
        }
    }

}

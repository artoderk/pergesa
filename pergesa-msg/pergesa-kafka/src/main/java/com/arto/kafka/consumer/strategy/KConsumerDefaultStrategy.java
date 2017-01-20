package com.arto.kafka.consumer.strategy;

import com.alibaba.fastjson.JSON;
import com.arto.core.common.MessageRecord;
import com.arto.event.service.PersistentEventService;
import com.arto.event.util.SpringContextHolder;
import com.arto.event.util.ThreadUtil;
import com.arto.kafka.common.Constants;
import com.arto.kafka.consumer.binding.KafkaConsumerConfig;
import com.arto.kafka.event.KafkaConsumeEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import static com.arto.kafka.common.KUtil.buildMessageId;

/**
 * 默认消息模式，适合重要消息
 * (单条消息处理完成后消费标识同步提交，为了避免阻塞后续消息，消息处理出错 > 3次后该消息入库，等待调度任务重试处理)
 *
 * Created by xiong.j on 2017/1/20.
 */
@Slf4j
public class KConsumerDefaultStrategy implements KConsumerStrategy {

    private final PersistentEventService service;

    public KConsumerDefaultStrategy(){
        this.service = SpringContextHolder.getBean("persistentEventService");
    }

    @Override
    public void onMessage(final KafkaConsumerConfig config, final ConsumerRecord<String, MessageRecord> record) {
        tryConsume(config, record);
    }

    private void tryConsume(final KafkaConsumerConfig config, final ConsumerRecord<String, MessageRecord> record) {
        boolean errFlag = false;
        // 如果出错，重试消费3次
        for (int i = 1; i <= 3; i++) {
            try {
                // 生成消息ID
                record.value().setMessageId(buildMessageId(record));
                // 消费消息
                config.getListener().onMessage(record.value());
            } catch (Exception e) {
                errFlag = true;
                log.warn("Kafka receive message failed, waiting for retry. record=" + record, e);
                if (i == 3) {
                    infiniteRetry(config, record);
                } else {
                    // 消息处理错误，暂停处理一小会
                    ThreadUtil.sleep(20000, Thread.currentThread(), log);
                }
            }
            // 消费成功则退出循环
            if (!errFlag) {
                break;
            }
        }
    }

    private void infiniteRetry(final KafkaConsumerConfig config, final ConsumerRecord<String, MessageRecord> record) {
        // 转换为事件
        KafkaConsumeEvent event = buildEvent(config, record);
        // 无限重试直到持久化成功
        while (true){
            try {
                service.persist(event, Constants.KAFKA);
                break;
            } catch (Exception e) {
                log.warn("Persist message failed, waiting for retry. record=" + record, e);
            }
            // 持久化消息错误，暂停处理一小会
            ThreadUtil.sleep(10000, Thread.currentThread(), log);
        }
        log.debug("Kafka receive message failed 3 times, persisted message to db waiting for retry. record=" + record);
    }

    private KafkaConsumeEvent buildEvent(final KafkaConsumerConfig config, final ConsumerRecord<String, MessageRecord> record){
        KafkaConsumeEvent event = new KafkaConsumeEvent();
        // 事件分组
        event.setGroup(com.arto.core.common.Constants.MQ);
        // 业务流水号
        event.setBusinessId(record.value().getMessageId());
        // 业务类型
        event.setBusinessType(Constants.K_CONSUME);
        // Topic
        event.setDestination(record.topic());
        // 优先级
        event.setPriority(config.getPriority());
        // 消息 使用fastjson序列化
        event.setPayload(JSON.toJSONString(record.value()));
        return event;
    }

}

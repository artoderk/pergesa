package com.arto.kafka.consumer.strategy;

import com.alibaba.fastjson.JSON;
import com.arto.core.common.MessageRecord;
import com.arto.core.consumer.MqListener;
import com.arto.event.util.TypeReferenceUtil;
import com.arto.event.build.Event;
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
    public void onMessage(final KafkaConsumerConfig config, final ConsumerRecord<String, String> record) {
        tryConsume(config, record);
    }

    @SuppressWarnings("unchecked")
    private void tryConsume(final KafkaConsumerConfig config, final ConsumerRecord<String, String> record) {
        // 如果出错，重试消费3次
        for (int i = 1; i <= 3; i++) {
            MessageRecord message = null;
            try {
                // 生成消息ID
                message = buildMessage(config.getListener(), record.value());
                message.setMessageId(buildMessageId(record));
                // 消费消息
                config.getListener().onMessage(message);
                break;
            } catch (Throwable e) {
                log.warn("Receive message failed, waiting for retry. record=" + record, e);
                if (i == 3) {
                    infiniteRetry(record, message);
                } else {
                    // 消息处理错误，暂停处理一小会
                    ThreadUtil.sleep(10000, Thread.currentThread(), log);
                }
            }
        }
    }

    private void infiniteRetry(final ConsumerRecord<String, String> record
            , MessageRecord message) {
        // 转换为事件
        Event event = buildEvent(record, message);
        // 无限重试直到持久化成功
        while (true){
            try {
                service.persist(event, Constants.K_CONSUME_EVENT_BEAN);
                break;
            } catch (Throwable e) {
                log.warn("Persist message failed, waiting for retry. record=" + record, e);
            }
            // 持久化消息错误，暂停处理一小会
            ThreadUtil.sleep(10000, Thread.currentThread(), log);
        }
        log.info("Receive message failed 3 times, persisted message to db waiting for retry. record=" + record);
    }

    private MessageRecord buildMessage(MqListener listener, String payload) throws Throwable {
        // 反序列化消息
        MessageRecord message = JSON.parseObject(payload, TypeReferenceUtil.getType(listener));
        return message;
    }

    private Event buildEvent(final ConsumerRecord<String, String> record, MessageRecord message){
        // 生成事件
        KafkaConsumeEvent event = new KafkaConsumeEvent();
        // 事件分组
        event.setGroup(KafkaConsumeEvent.class);
        // 业务流水号设为消息ID
        if (message == null) {
            event.setBusinessId(buildMessageId(record));
        } else {
            event.setBusinessId(message.getMessageId());
        }
        // 业务类型
        event.setBusinessType(Constants.K_CONSUME);
        // 消息
        event.setPayload(record.value());
        // 重试次数
        event.setRetry(3);
        return event;
    }

}

package com.arto.kafka.consumer.strategy;

import com.arto.event.common.Destroyable;
import com.arto.core.common.MessageRecord;
import com.arto.event.bootstrap.Event;
import com.arto.event.service.PersistentEventService;
import com.arto.event.util.SpringContextHolder;
import com.arto.event.util.SpringDestroyableUtil;
import com.arto.event.util.ThreadUtil;
import com.arto.kafka.common.Constants;
import com.arto.kafka.consumer.binding.KafkaConsumerConfig;
import com.arto.kafka.event.KafkaConsumeEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.arto.kafka.common.KUtil.buildMessageId;

/**
 * 默认消费模式，适合重要消息
 * (单条消息处理完成后消费标识同步提交，为了避免阻塞后续消息，消息处理出错 > 3次后该消息入库，等待调度任务重试处理)
 *
 * Created by xiong.j on 2017/1/20.
 */
@Slf4j
class KafkaConsumerDefaultStrategy extends AbstractKafkaConsumerStrategy implements KafkaConsumerStrategy, Destroyable {

    private final PersistentEventService service;

    /** 消息拉取线程关闭Flag */
    private final AtomicBoolean closeFlag = new AtomicBoolean(false);

    KafkaConsumerDefaultStrategy(){
        this.service = SpringContextHolder.getBean("persistentEventService");
        // 注册勾子
        SpringDestroyableUtil.add("kafkaConsumerDefaultStrategy", this);
    }

    @Override
    public void onMessage(final KafkaConsumerConfig config, final ConsumerRecord<String, String> record) {
        tryConsume(config, record);
    }

    /**
     * 销毁
     */
    @Override
    public void destroy() {
        closeFlag.set(true);
        log.info("Destroy KafkaConsumerDefaultStrategy successful.");
    }

    @SuppressWarnings("unchecked")
    private void tryConsume(final KafkaConsumerConfig config, final ConsumerRecord<String, String> record) {
        // 如果出错，重试消费3次，超过三次持久化后由调度任务再重试
        for (int i = 1; i <= 3; i++) {
            MessageRecord message = null;
            try {
                // 反序列化消息
                message = deserializerMessage(config, record.value());
                // 生成消息ID
                message.setMessageId(buildMessageId(record.partition(), record.offset()));
                // 重复消费检测
                if (!checkRedeliver(config, message)) {
                    // 消费消息
                    onMessage(config, message);
                }
                break;
            } catch (Throwable e) {
                log.warn("Receive message failed, waiting for retry. record=" + record, e);
                if (i == 3) {
                    infiniteRetry(record, message);
                } else {
                    // 消息处理错误，暂停处理一小会
                    ThreadUtil.sleep(5000, log);
                }
            }
        }
    }

    private void infiniteRetry(final ConsumerRecord<String, String> record
            , MessageRecord message) {
        // 转换为事件
        Event event = buildEvent(record, message);
        // 无限重试直到持久化成功
        while (closeFlag.get()){
            try {
                service.persist(event, Constants.K_CONSUME_EVENT_BEAN);
                break;
            } catch (Throwable e) {
                log.warn("Persist message failed, waiting for retry. record=" + record, e);
            }
            // 持久化消息错误，暂停处理一小会
            ThreadUtil.sleep(5000, log);
        }
        log.info("Receive message failed 3 times, persisted message to db waiting for retry. record=" + record);
    }

    private Event buildEvent(final ConsumerRecord<String, String> record, MessageRecord message){
        // 生成事件
        KafkaConsumeEvent event = new KafkaConsumeEvent();
        // 事件分组
        event.setGroup(KafkaConsumeEvent.class);
        // 业务流水号设为消息ID
        if (message == null) {
            event.setBusinessId(buildMessageId(record.partition(), record.offset()));
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

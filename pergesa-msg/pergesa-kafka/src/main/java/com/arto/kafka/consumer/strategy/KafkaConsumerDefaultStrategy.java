package com.arto.kafka.consumer.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.arto.core.common.MessageRecord;
import com.arto.core.common.MqTypeEnum;
import com.arto.core.exception.MqClientException;
import com.arto.event.bootstrap.Event;
import com.arto.event.common.Destroyable;
import com.arto.event.service.PersistentEventService;
import com.arto.event.util.SpringContextHolder;
import com.arto.event.util.SpringDestroyableUtil;
import com.arto.event.util.ThreadUtil;
import com.arto.kafka.common.Constants;
import com.arto.kafka.consumer.binding.KafkaConsumerConfig;
import com.arto.kafka.event.KafkaConsumeEvent;
import com.google.common.base.Strings;
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
        MessageRecord message = null;
        try {
            // 反序列化消息
            message = deserializerMessage(config, record.value());
            // 生成消息ID
            message.setMessageId(buildMessageId(record.partition(), record.offset()));
        } catch (Throwable e) {
            log.warn("Deserializer record failed, Discard this record:" + record, e);
            // 持久化消息，以便重试
            infiniteRetry(record, message);
        }

        // 如果消费出错，重试消费3次，超过三次持久化后由调度任务再重试
        for (int i = 1; i <= 3; i++) {
            try {
                // 重复消费检测
                if (!checkRedeliver(config, message)) {
                    // 消费消息
                    onMessage(config, message);
                } else {
                    log.info("Discard redelivered message:" + message);
                }
                break;
            } catch (Throwable e) {
                log.warn("Receive message failed, waiting for retry. record:" + record, e);
                if (i == 3) {
                    // 持久化消息，以便重试
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
        boolean failed = true;
        // 转换为事件
        Event event = buildEvent(record, message);
        // 无限重试直到持久化成功
        while (!closeFlag.get()){
            try {
                service.persist(event, Constants.K_CONSUME_EVENT_BEAN);
                failed = false;
                break;
            } catch (Throwable e) {
                log.warn("Persist message failed, waiting for retry. record:" + record, e);
            }
            // 持久化消息错误，暂停处理一小会
            ThreadUtil.sleep(5000, log);
        }
        if (failed) {
            throw new MqClientException("Persist message failed when stop server.");
        } else {
            log.warn("Receive message failed 3 times, persisted message to db waiting for retry. record:" + record);
        }
    }

    @SuppressWarnings("unchecked")
    private Event buildEvent(final ConsumerRecord<String, String> record, MessageRecord message){
        // 生成事件
        KafkaConsumeEvent event = new KafkaConsumeEvent();
        // 事件分组
        event.setGroup(KafkaConsumeEvent.class);
        // 默认业务类型
        event.setBusinessType(Constants.K_CONSUME);
        // 消息
        event.setPayload(message);
        // 目的地
        event.setDestination(record.topic());
        // 消息类型
        event.setType(MqTypeEnum.KAFKA.getMemo());
        // 是否持久化
        event.setPersistent(true);
        if (message == null) {
            JSONObject jsonObject = JSON.parseObject(record.value());
            String messageId = buildMessageId(record.partition(), record.offset());
            if (!Strings.isNullOrEmpty(jsonObject.getString("businessId"))) {
                // 消息自带业务流水号
                event.setBusinessId(jsonObject.getString("businessId"));
                // 消息自带业务类型
                event.setBusinessType(jsonObject.getString("businessType"));
            } else {
                // 业务流水号为消息ID
                event.setBusinessId(messageId);
            }
            // 设置消息Id
            event.setMessageId(messageId);
            // 消息解析出错时消息设为json对象
            event.setPayload(jsonObject);
        } else {
            // 设置消息Id
            event.setMessageId(message.getMessageId());
            if (Strings.isNullOrEmpty(message.getBusinessId())) {
                // 以非事务消息发送时业务流水号为消息ID
                event.setBusinessId(message.getMessageId());
            } else {
                // 消息自带业务流水号
                event.setBusinessId(message.getBusinessId());
                // 消息自带业务类型
                event.setBusinessType(message.getBusinessType());
            }
        }
        return event;
    }

}

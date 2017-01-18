package com.arto.kafka.producer.binding;

import com.alibaba.fastjson.JSON;
import com.arto.core.common.MessageRecord;
import com.arto.core.exception.MqClientException;
import com.arto.core.producer.MqProducer;
import com.arto.event.build.EventBusFactory;
import com.arto.event.service.PersistentEventService;
import com.arto.event.util.SpringContextHolder;
import com.arto.kafka.common.Constants;
import com.arto.kafka.common.KMessageRecord;
import com.arto.kafka.common.KAcksEnum;
import com.arto.kafka.event.KafkaEvent;

/**
 * 生产者绑定类，一个对象对应一个Topic和一份配置
 *
 * Created by xiong.j on 2017/1/12.
 */
public class KafkaProducerBinding implements MqProducer {

    /** Kafka生产者配置 */
    private final KafkaProducerConfig config;

    /** 持久化事件服务 */
    private PersistentEventService service;

    public KafkaProducerBinding(KafkaProducerConfig config) {
        verifyEvent(config);
        this.config = config;
        // 优先级转换为Kafka的acks
        this.config.setPriority(convert2Ack(config));
        // 暂时依赖Spring获取
        this.service = SpringContextHolder.getBean("persistentEventService");
    }

    @Override
    public void send(Object message) throws Exception {
        innerSend(buildMessage(null, -1, message));
    }

    @Override
    public void send(String key, Object message) throws Exception {
        innerSend(buildMessage(key, -1, message));
    }

    @Override
    public void send(String key, int partition, Object message) throws Exception {
        innerSend(buildMessage(key, partition, message));
    }

    @Override
    public void send(MessageRecord record) throws Exception {
        if (record instanceof KMessageRecord) {
            innerSend((KMessageRecord)record);
        } else {
            throw new MqClientException("Not support this messageRecord:" + record);
        }
    }

    private void innerSend(KMessageRecord record) throws Exception {
        // 转换为事件
        KafkaEvent event = buildEvent(record);
        if (event.isPersistent()) {
            // 持久化消息直接持久化(模拟客户端两阶段提交)
            service.persist(event, Constants.KAFKA_EVENT_BEAN);
        } else {
            // 非持久化消息直接发送
            EventBusFactory.getInstance().post(event);
        }
    }

    private KMessageRecord buildMessage(String key, int partition, Object message){
        KMessageRecord record = new KMessageRecord();
        // Hash主键
        record.setKey(key);
        // 分区
        record.setPartition(partition);
        // 消息
        record.setMessage(message);
        // 事务
        record.setTransaction(config.isTransaction());
        return record;
    }

    private KafkaEvent buildEvent(KMessageRecord record){
        KafkaEvent event = new KafkaEvent();
        // 事件分组
        event.setGroup(com.arto.core.common.Constants.MQ);
        // 业务流水号
        event.setBusinessId(record.getBusinessId());
        // 业务类型
        event.setBusinessType(record.getBusinessType());
        // Topic
        event.setDestination(config.getDestination());
        // Hash主键
        event.setKey(record.getKey());
        // 分区
        event.setPartition(record.getPartition());
        // 优先级
        event.setPriority(config.getPriority());
        // 持久化
        event.setPersistent(record.isTransaction());
        // 消息 使用fastjson序列化
        event.setPayload(JSON.toJSONString(record));
        // 回调
        event.setCallback(config.getCallback());
        return event;
    }

    private int convert2Ack(KafkaProducerConfig config){
        if (config.getPriority() == 3) {
            return KAcksEnum.ACK_NOWAIT.getCode();
        } else if (config.getPriority() == 0 ){
            return KAcksEnum.ACK_NOWAIT.getCode();
        } else if (config.getPriority() == 1){
            return KAcksEnum.ACK_ALL.getCode();
        } else if (config.getPriority() == 2){
            return KAcksEnum.ACK_LEADER.getCode();
        } else {
            return KAcksEnum.ACK_ALL.getCode();
        }
    }

    private void verifyEvent(KafkaProducerConfig config){
        if (config.getPriority() > 3 || config.getPriority() < -1) {
            throw new MqClientException("Not support this priority! ProducerConfig:" + config);
        }
        if ((config.getPriority() == 1 || config.getPriority() == 2) && config.getCallback() != null) {
            throw new MqClientException("Important messages can't send by asynchronous! ProducerConfig:" + config);
        }
    }
}

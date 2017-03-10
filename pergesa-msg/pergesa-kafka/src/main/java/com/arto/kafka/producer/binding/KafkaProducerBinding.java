package com.arto.kafka.producer.binding;

import com.arto.core.bootstrap.MqClient;
import com.arto.core.common.DataPipeline;
import com.arto.core.common.MessagePriorityEnum;
import com.arto.core.common.MessageRecord;
import com.arto.core.common.MqTypeEnum;
import com.arto.core.event.MqEvent;
import com.arto.core.exception.MqClientException;
import com.arto.core.intercepter.TxMessageContextHolder;
import com.arto.core.producer.MqProducer;
import com.arto.event.bootstrap.EventBusFactory;
import com.arto.event.service.PersistentEventService;
import com.arto.event.util.SpringContextHolder;
import com.arto.kafka.common.Constants;
import com.arto.kafka.common.KMessageRecord;
import com.arto.kafka.event.KafkaProduceEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 生产者绑定类，一个对象对应一个Topic和一份配置
 *
 * Created by xiong.j on 2017/1/12.
 */
@Slf4j
public class KafkaProducerBinding implements MqProducer {

    private static final AtomicBoolean closeFlag = new AtomicBoolean(false);

    /** Kafka生产者配置 */
    private final KafkaProducerConfig config;

    /** 持久化事件服务 */
    private final PersistentEventService service;

    static {
        // 注册Kafka客户端
        new Thread(new KafkaProducerBinding.KafkaProduceQueueThread(), "KafkaProduceQueueThread").start();
    }

    public KafkaProducerBinding(KafkaProducerConfig config) {
        verifyConfig(config);
        this.config = config;
        // 暂时依赖Spring获取
        this.service = SpringContextHolder.getBean("persistentEventService");
    }

    /**
     * 发送消息(简化方法，非事务类消息可以使用直接使用此方法发送)
     *
     * @param message
     * @throws MqClientException
     */
    @Override
    @SuppressWarnings("unchecked")
    public void send(Object message) throws MqClientException {
        innerSend(new MessageRecord(message), false);
    }

    /**
     * 发送消息(定制发送，可配置发送参数)
     * 注:事务消息必须设置"业务凭证流水号"和"业务类型"，以便发送异常时追踪排错
     *
     * @param record
     * @throws MqClientException
     */
    @Override
    public void send(MessageRecord record) throws MqClientException {
        innerSend(record, config.isTransaction());
    }

    /**
     * 发送非事务消息(开启事务发送后，可使用此方法发送非事务消息)
     *
     * @param record
     * @throws MqClientException
     */
    @Override
    @Deprecated
    public void sendNonTx(MessageRecord record) throws MqClientException {
        innerSend(record, false);
    }

    /**
     * 获取绑定的配置
     *
     * @return
     */
    public KafkaProducerConfig getConfig() {
        return config;
    }

    private void innerSend(MessageRecord record, boolean isTransaction) throws MqClientException {
        if (record == null || record.getMessage() == null) {
            throw new MqClientException("Message can't be null or blank");
        }

        // 转换为事件
        KafkaProduceEvent event = buildEvent(record, isTransaction);
        if (event.isPersistent()) {
            // 持久化消息直接持久化(模拟客户端两阶段提交)
            service.persist(event, Constants.KAFKA_EVENT_BEAN);
            // 加入线程上下文，等待事务正常结束后加入发送Queue处理(避免定时调度的延迟，调度默认10分钟执行一次)
            TxMessageContextHolder.setTxMessage(event);
        } else {
            // 非持久化消息直接发送
            EventBusFactory.getInstance().post(event);
        }
    }

    @SuppressWarnings("unchecked")
    private KafkaProduceEvent buildEvent(MessageRecord record, boolean isTransaction){
        KafkaProduceEvent event = new KafkaProduceEvent();
        // 业务流水号
        event.setBusinessId(record.getBusinessId());
        // 业务类型
        event.setBusinessType(record.getBusinessType());
        // Topic
        event.setDestination(config.getDestination());
        if (record instanceof KMessageRecord) {
            // Hash主键
            event.setKey(((KMessageRecord)record).getKey());
            // 分区
            event.setPartition(((KMessageRecord)record).getPartition());
        }
        // 优先级
        event.setPriority(config.getPriority().getCode());
        // 持久化
        event.setPersistent(isTransaction);
        // 消息
        event.setPayload(record);
        // 回调
        event.setCallback(config.getCallback());
        return event;
    }

    private void verifyConfig(KafkaProducerConfig config){
        if ((config.getPriority().getCode() > MessagePriorityEnum.LOW.getCode())
                || (config.getPriority().getCode() < MessagePriorityEnum.HIGH.getCode())) {
            throw new MqClientException("Not support this priority! ProducerConfig:" + config);
        }
        if (config.getPriority() == MessagePriorityEnum.HIGH && config.getCallback() != null) {
            throw new MqClientException("Transaction messages can't send by asynchronous! ProducerConfig:" + config);
        }
    }

    /**
     * 销毁线程
     */
    public void close() {
        if (!closeFlag.get()) {
            synchronized (KafkaProducerBinding.class) {
                if (!closeFlag.get()) {
                    closeFlag.set(true);
                }
            }
        }
    }

    /**
     * 持久化消息存储后，直接扔Queue里在此线程处理
     */
    private static class KafkaProduceQueueThread implements Runnable{

        private final DataPipeline<MqEvent> dataPipeline;

        private KafkaProduceQueueThread() {
            dataPipeline = MqClient.getPipeline(MqTypeEnum.KAFKA.getMemo());
        }

        @Override
        public void run() {
            while (!closeFlag.get()) {
                try {
                    KafkaProduceEvent event = (KafkaProduceEvent)dataPipeline.poll(300, TimeUnit.MILLISECONDS);
                    if (event != null) {
                        EventBusFactory.getInstance().post(event);
                    }
                } catch (Throwable t) {
                    log.warn("Send message failed.", t);
                }
            }
        }
    }
}

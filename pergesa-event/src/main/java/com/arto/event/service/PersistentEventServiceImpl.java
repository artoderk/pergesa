package com.arto.event.service;

import com.arto.event.build.Event;
import com.arto.event.common.EventStatusEnum;
import com.arto.event.storage.EventInfo;
import com.arto.event.exception.PersistentEventLockException;
import com.arto.event.storage.EventStorage;
import com.arto.event.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Random;

/**
 * Created by xiong.j on 2017/1/4.
 */
@Slf4j
@Service("persistentEventService")
public class PersistentEventServiceImpl implements PersistentEventService {

    /** 无限次重试时的重试间隔为5分钟 */
    @Value("${kafka.retry.interval:300}")
    private int retryInterval;

    /** 处理持久化消息时，默认采用悲观锁 */
    @Value("${event.persistent.lock.optimistic:false}")
    private boolean lockOptimistic;

    /** 系统名 */
    @Value("${sar.name:webapp}")
    private String systemId;

    /** 事件分片数 */
    @Value("${event.storage.tag:10}")
    private int tag;

    /** 默认重试次数 */
    @Value("${event.retry.times:5}")
    private int defaultRetry;

    /** 事件持久化操作 */
    @Autowired
    private EventStorage eventStorage;

    /** 随机数生成器，用来分隔Tag*/
    private Random random = new Random();

    /**
     * 持久化Event
     *
     * @param event
     * @param type
     * @throws
     */
    @Override
    public void persist(Event event, String type) throws Exception {
        eventStorage.create(event2Info(event, type));
    }

    /**
     * 对持久化Event加锁
     *
     * @param eventInfo
     * @return
     * @throws
     */
    @Override
    public EventInfo lock(EventInfo eventInfo) throws Exception {
        if (lockOptimistic) {
            // 乐观锁直接返回
            return null;
        } else {
            try {
                // 手动加锁
                return eventStorage.lockById(eventInfo.getId());
            } catch (Exception e){
                if (e.getMessage().contains("ORA-00054")
                        || e.getMessage().contains("could not obtain lock")) {
                    // Oracle和Postgresql环境下获取锁失败Exception
                    throw new PersistentEventLockException(e);
                }
                // 其它Exception
                throw e;
            }
        }
    }

    /**
     * 持久化Event处理失败时的处理
     *
     * @param eventInfo
     */
    @Override
    public void fail(EventInfo eventInfo){
        if (eventInfo.getDefaultRetriedCount() == -1) {
            // 无限重试时继续重试
            retry(eventInfo);
        }
        if (eventInfo.getCurrentRetriedCount() == eventInfo.getDefaultRetriedCount()) {
            // TODO 超过重试次数的Event通过MQ发送到后管系统
            // EventBusFactory.getInstance().post();

            // 更新处理状态为 "3:等待人工处理"
            EventInfo updInfo = new EventInfo();
            updInfo.setId(eventInfo.getId());
            updInfo.setStatus(EventStatusEnum.MANUAL_WAIT.getCode());
            update(updInfo);
        } else {
            // 继续重试
            retry(eventInfo);
        }
    }

    /**
     * 持久化Event处理成功时的处理
     *
     * @param eventInfo
     */
    @Override
    public void finish(EventInfo eventInfo){
        // 更新处理状态为 "2:处理成功"
        EventInfo updInfo = new EventInfo();
        updInfo.setId(eventInfo.getId());
        updInfo.setStatus(EventStatusEnum.SUCCESS.getCode());
        update(updInfo);
    }

    private void retry(EventInfo eventInfo){
        EventInfo updInfo = new EventInfo();
        updInfo.setId(eventInfo.getId());
        if (eventInfo.getStatus() == EventStatusEnum.WAIT.getCode()) {
            // 更新状态为 "1:处理中"
            updInfo.setStatus(EventStatusEnum.PROCESSING.getCode());
        }

        updInfo.setCurrentRetriedCount(eventInfo.getCurrentRetriedCount() + 1);
        if (eventInfo.getDefaultRetriedCount() == -1) {
            // 无限重试时
            updInfo.setNextRetryTime(DateUtil.getPrevSecTimestamp(retryInterval));
        } else {
            // 设置有重试次数时
            updInfo.setNextRetryTime(getNextRetryTime(eventInfo.getCurrentRetriedCount()));
        }
        // 更新重试信息
        update(updInfo);
    }

    private void update(EventInfo updInfo) {
        if (lockOptimistic) {
            // 采用乐观锁时更新操作
            eventStorage.optimisticUpdate(updInfo);
        } else {
            // 采用悲观锁或其它情况下的更新操作
            eventStorage.update(updInfo);
        }
    }

    private Timestamp getNextRetryTime(int currentRetriedCount){
        // 获取下一次重试时间，默认使用重试次数的9次方
        return new Timestamp(System.currentTimeMillis()
                + Math.round(Math.pow(9, currentRetriedCount))
                * 1000);
    }

    private EventInfo event2Info(Event event, String type){
        EventInfo info = new EventInfo();
        // Tag
        info.setTag(random.nextInt(10));
        // 系统名
        info.setSystemId(systemId);
        // 业务流水号
        info.setBusinessId(event.getBusinessId());
        // 业务类型
        info.setBusinessType(event.getBusinessType());
        // 事件类型
        info.setEventType(type);
        // 事件状态
        info.setStatus(EventStatusEnum.PROCESSING.getCode());
        // 事件内容
        info.setPayload(event.getPayload());
        // 重试次数
        if (event.isPersistent() && event.getRetry() == 0) {
            // 持久化事件且没有设定重试次数的情况下，使用默认次数
            info.setDefaultRetriedCount(defaultRetry);
        } else {
            info.setDefaultRetriedCount(event.getRetry());
        }
        return info;
    }
}

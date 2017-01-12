package com.arto.event.service;

import com.arto.event.common.EventStatusEnum;
import com.arto.event.domain.EventInfo;
import com.arto.event.exception.PersistentEventLockException;
import com.arto.event.storage.EventStorage;
import com.arto.event.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

/**
 * Created by xiong.j on 2017/1/4.
 */
@Slf4j
@Service("persistentEventService")
public class PersistentEventServiceImpl implements PersistentEventService {

    /** 无限次重试时的重试间隔为5分钟 */
    @Value("${kafka.retry.interval}")
    private int retryInterval = 5 * 60;

    /** 处理持久化消息时，默认采用悲观锁 */
    @Value("${event.persistent.lock.optimistic}")
    private boolean lockOptimistic = false;

    @Autowired
    private EventStorage eventStorage;

    /**
     * 对持久化Event加锁
     *
     * @param eventInfo
     * @return
     */
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
                    throw new PersistentEventLockException(e);
                }
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

            // 更新处理状态为 3:等待人工处理
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
        // 更新处理状态为 2:处理成功
        EventInfo updInfo = new EventInfo();
        updInfo.setId(eventInfo.getId());
        updInfo.setStatus(EventStatusEnum.SUCCESS.getCode());
        update(updInfo);
    }

    private void retry(EventInfo eventInfo){
        EventInfo updInfo = new EventInfo();
        updInfo.setId(eventInfo.getId());
        if (eventInfo.getStatus() == EventStatusEnum.WAIT.getCode()) {
            // 更新状态为"处理中"
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
        update(eventInfo);
    }

    private void update(EventInfo updInfo) {
        if (lockOptimistic) {
            eventStorage.optimisticUpdate(updInfo);
        } else {
            eventStorage.update(updInfo);
        }
    }

    private Timestamp getNextRetryTime(int currentRetriedCount){
        return new Timestamp(System.currentTimeMillis()
                + Math.round(Math.pow(9, currentRetriedCount))
                * 1000);
    }

}

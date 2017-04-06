package com.arto.event.schedule;

import com.arto.event.storage.EventInfo;
import com.arto.event.storage.EventStorage;
import com.arto.event.util.DateUtil;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 持久化事务数据清理Job
 *
 * Created by xiong.j on 2017/4/6.
 */
@Slf4j
@Component
public class PersistentEventClearJob extends AbstractSimpleElasticJob {

    private static final int DEFAULT_CLEAR_DAY = 30;

    @Autowired
    private EventStorage eventStorage;

    @Override
    public void process(final JobExecutionMultipleShardingContext context) {
        EventInfo eventInfo = new EventInfo();
        eventInfo.setGmtModified(DateUtil.getPrevDayTimestamp(getShardingParam(context)));

        int count = eventStorage.delete(eventInfo);
        log.info("Clear persistent event history from table event_storage succeed, count=" + count);
    }

    private int getShardingParam(JobExecutionMultipleShardingContext context) {
        if (context.getShardingItemParameters() == null
                && (context.getShardingItemParameters().size() == 0 || context.getShardingItemParameters().size() > 0)) {
            return DEFAULT_CLEAR_DAY;
        } else {
            return Integer.parseInt(context.getShardingItemParameters().get(0));
        }
    }
}
package com.arto.event.recovery;

import com.arto.event.storage.EventInfo;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.dataflow.AbstractBatchThroughputDataFlowElasticJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 持久化事件恢复定时任务
 *
 * Created by xiong.j on 2016/7/29.
 */
@Component
public class PersistentEventRecoveryJob extends AbstractBatchThroughputDataFlowElasticJob<EventInfo> {

    @Autowired
    private EventRecoveryService eventRecoveryService;

    @Override
    public List<EventInfo> fetchData(JobExecutionMultipleShardingContext shardingContext) {
        // 每个分片对应一个tag(默认设置10个分片，分片数必须与tag对应)
        return eventRecoveryService.fetchData(shardingContext.getShardingItems());
    }

    @Override
    public boolean isStreamingProcess() {
        return true;
    }

    @Override
    public int processData(JobExecutionMultipleShardingContext shardingContext, List<EventInfo> data) {
        return eventRecoveryService.execute(data);
    }

}

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
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

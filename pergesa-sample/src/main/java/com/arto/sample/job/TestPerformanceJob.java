package com.arto.sample.job;

import com.arto.core.annotation.Producer;
import com.arto.core.common.MessagePriorityEnum;
import com.arto.core.producer.MqProducer;
import com.arto.sample.domain.OrderDO;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import org.springframework.stereotype.Component;

/**
 * Created by xiong.j on 2017/3/6.
 */
@Component
public class TestPerformanceJob extends AbstractSimpleElasticJob {

    @Producer(destination = "pegesa-test-low", priority = MessagePriorityEnum.LOW)
    private MqProducer<OrderDO> lowProducer;

    @Override
    public void process(final JobExecutionMultipleShardingContext context) {
        OrderDO bean = new OrderDO();
        bean.setOrderId(1);
        bean.setStatus("OrderDO.test");
        for (int i = 0; i < 10; i++) {
            bean.setOrderId(System.currentTimeMillis());
            lowProducer.send(bean);
        }
    }
}
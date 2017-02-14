package com.arto.kafka.event;

import com.arto.core.common.MessageRecord;
import com.arto.event.service.PersistentEventService;
import com.arto.kafka.common.Constants;
import common.DefaultTestCase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by xiong.j on 2017/2/13.
 */
public class KafkaProduceEventTest extends DefaultTestCase{

    @Autowired
    private PersistentEventService persistentEventService;

    @Test
    public void test() throws Exception {
        MessageRecord<String> record = new MessageRecord<String>();
        record.setMessageId("messageId");
        record.setMessage("message");
        KafkaProduceEvent event = new KafkaProduceEvent();
        event.setPartition(1);
        event.setDestination("pegesa-test");
        event.setKey("key");
        event.setBusinessId("businessId");
        event.setBusinessType("businessType");
        event.setPersistent(true);
        event.setPayload(record);
        persistentEventService.persist(event, Constants.KAFKA_EVENT_BEAN);
    }

}

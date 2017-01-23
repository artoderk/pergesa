package com.arto.kafka.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.arto.core.common.MessageRecord;
import com.arto.core.consumer.MqListener;
import com.arto.kafka.event.KafkaConsumeEvent;
import common.TestMessageBean;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * KafkaConsumeEventListener Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>һ�� 22, 2017</pre>
 */
public class KafkaConsumeEventListenerTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: listen(KafkaConsumeEvent event)
     */
    @Test
    public void testListen() throws Exception {
        List<String> list = new ArrayList<String>();
        list.add("Test1");
        list.add("Test2");

        TestMessageBean bean = new TestMessageBean();
        bean.setId(1);
        bean.setName("TestMessageBean");
        bean.setList(list);

        MessageRecord record = new MessageRecord();
        record.setMessage(bean);
        record.setMessageId("K0145");

        KafkaConsumeEvent event = new KafkaConsumeEvent();
        event.setPayload(JSON.toJSONString(record));
        event.setGroup("Kafka");
        event.setDestination("topic1");

        System.out.println(JSON.toJSONString(event));

        MqListener<TestMessageBean> listener = new MqListener<TestMessageBean>() {
            @Override
            public void onMessage(MessageRecord<TestMessageBean> record) {
                System.out.println("onMessage" + record);
            }

            @Override
            public boolean checkRedeliver(MessageRecord record) {
                return true;
            }
        };

        listener.onMessage(JSON.parseObject(event.getPayload(), new TypeReference<MessageRecord<TestMessageBean>>(){}));
    }


} 

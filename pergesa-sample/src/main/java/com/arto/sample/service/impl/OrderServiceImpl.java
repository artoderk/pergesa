package com.arto.sample.service.impl;

import com.arto.core.annotation.Producer;
import com.arto.core.annotation.TxMessage;
import com.arto.core.common.MessagePriorityEnum;
import com.arto.core.common.MessageRecord;
import com.arto.core.producer.MqCallback;
import com.arto.core.producer.MqProducer;
import com.arto.sample.dao.OrderDao;
import com.arto.sample.domain.OrderDO;
import com.arto.sample.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by xiong.j on 17/2/15.
 */
@Service
public class OrderServiceImpl implements OrderService{

    @Producer(destination = "pegesa-test", priority = MessagePriorityEnum.HIGH)
    private MqProducer<OrderDO> producer;

//    private KafkaProducerConfig config = new KafkaProducerConfig("pegesa-test", MessagePriorityEnum.HIGH);
//    private MqProducer producer = MqClient.buildProducer(config);

    @Autowired
    private OrderDao dao;

    @Override
    @TxMessage
    @Transactional
    public boolean addOrder(OrderDO orderDO) {
        int result = dao.addOrder(orderDO);
        sendMessage(orderDO);
        return 0 < result;
    }

    @Override
    @Transactional
    public boolean cancelOrder(OrderDO orderDO){
        return 0 < dao.deleteOrder(orderDO);
    }

    private void sendMessage(OrderDO orderDO) {
        producer.send(new MessageRecord<OrderDO>("oid" + orderDO.getOrderId(), "order", orderDO));
        // 如果已开启事务发送，但有消息不需要事务发送时可使用sendNonTx方法
        //producer.sendNonTx(new MessageRecord<OrderDO>("oid" + orderDO.getOrderId(), "order", orderDO));
    }

    private class OrderCallback implements MqCallback<OrderDO>{

        @Override
        public void onCompletion(OrderDO orderDO) {
            System.out.println("callback:" + orderDO.toString());
        }
    }
}

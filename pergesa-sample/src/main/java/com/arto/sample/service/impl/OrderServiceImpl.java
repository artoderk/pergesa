package com.arto.sample.service.impl;

import com.arto.core.bootstrap.MqClient;
import com.arto.core.common.MessagePriorityEnum;
import com.arto.core.common.MessageRecord;
import com.arto.core.producer.MqProducer;
import com.arto.kafka.producer.binding.KafkaProducerConfig;
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

    private KafkaProducerConfig config = new KafkaProducerConfig("pegesa-test", MessagePriorityEnum.HIGH);


    private MqProducer producer = MqClient.buildProducer(config);;

    @Autowired
    private OrderDao dao;

    @Override
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
        producer.send(new MessageRecord("bid01", "btype01", orderDO));
    }
}

package com.arto.sample.rest;

import com.arto.core.bootstrap.MqClient;
import com.arto.core.common.DataPipeline;
import com.arto.core.common.MqTypeEnum;
import com.arto.core.event.MqEvent;
import com.arto.sample.domain.OrderDO;
import com.arto.sample.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 下单(事务消息)
 *
 * Created by xiong.j on 17/2/15.
 */
@Controller
@RequestMapping(value = "/rest")
public class OrderRest {

    /** 下单处理Service */
    @Autowired
    private OrderService orderService;

    @RequestMapping(value="/{amount}")
    @ResponseBody
    public boolean purchase(@PathVariable long amount)throws Exception {
        long orderId = System.currentTimeMillis();
        long userId = 1;
        long productId = 1;
        String status= "success";
    	OrderDO orderDO= new OrderDO(orderId, productId, userId, status, amount);
    	orderService.addOrder(orderDO);

        return true;
    }

    @RequestMapping(value="/times/{times}")
    @ResponseBody
    public String times(@PathVariable long times)throws Exception {
        long userId = 1;
        long productId = 1;
        String status= "success";

        long start = System.currentTimeMillis();
        OrderDO orderDO= new OrderDO(1, productId, userId, status, 1);
        for (int i = 1; i <= times; i++) {
            orderDO.setOrderId(i);
            orderService.addOrder(orderDO);
        }

        DataPipeline<MqEvent> pipeline = MqClient.getPipeline(MqTypeEnum.KAFKA.getMemo());
        while (true){
            if (pipeline.size() == 0) {
                break;
            } else {
                Thread.sleep(50);
            }
        }
        long end = System.currentTimeMillis();
        return "start time=" + start + ", end time=" + end + ", spent time=" + (end - start);
    }
}
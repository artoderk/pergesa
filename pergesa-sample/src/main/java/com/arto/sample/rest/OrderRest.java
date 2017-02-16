package com.arto.sample.rest;

import com.arto.sample.domain.OrderDO;
import com.arto.sample.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 下单
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
}
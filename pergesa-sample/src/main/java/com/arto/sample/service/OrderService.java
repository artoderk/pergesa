package com.arto.sample.service;

import com.arto.sample.domain.OrderDO;

/**
 * Created by xiong.j on 17/2/15.
 */
public interface OrderService {
    /**
     * 增加订单操作
     * @param orderDO 订单对象
     * @return
     */
    boolean addOrder(OrderDO orderDO);

    boolean cancelOrder(OrderDO orderDO);
}

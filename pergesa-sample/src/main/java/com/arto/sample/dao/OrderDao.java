package com.arto.sample.dao;

import com.arto.sample.domain.OrderDO;

/**
 * Created by xiong.j on 17/2/15.
 */
public interface OrderDao {

    /**
     * 增加订单,订单的id需要程序中给出,不要自动生成
     * @param orderDO
     * @return
     */
    int addOrder(OrderDO orderDO);

    /**
     * 删除订单
     * @param orderDO
     * @return
     */
    int deleteOrder(OrderDO orderDO);
};

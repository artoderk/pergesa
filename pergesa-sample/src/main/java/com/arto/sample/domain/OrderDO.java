package com.arto.sample.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by xiong.j on 17/2/15.
 */
@Getter
@Setter
@ToString
public class OrderDO implements Serializable {

    private long orderId;

    private long productId;

    private long userId;

    private String status;

    private long amount;

    public OrderDO(){
    }

    public OrderDO(long orderId, long productId, long userId, String status, long amount) {
        this.orderId = orderId;
        this.productId = productId;
        this.userId = userId;
        this.status = status;
        this.amount = amount;
    }
}

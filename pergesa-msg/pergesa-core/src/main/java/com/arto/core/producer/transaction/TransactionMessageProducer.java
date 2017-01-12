package com.arto.core.producer.transaction;

/**
 * Created by xiong.j on 2016/12/28.
 */
public interface TransactionMessageProducer<T> {

    T send(Object producer, Object transactionMessage);

}

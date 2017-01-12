package com.arto.core.producer.transaction;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by xiong.j on 2016/12/28.
 */
public class TransactionMessageProducerFactory {

    private final ConcurrentHashMap<String, TransactionMessageProducer> maps = new ConcurrentHashMap<String, TransactionMessageProducer>();

    private TransactionMessageProducerFactory() {}

    private static class TransactionMessageProducerFactoryHolder{
        public static TransactionMessageProducerFactory instance = new TransactionMessageProducerFactory();
    }

    public static TransactionMessageProducerFactory getInstance(){
        return TransactionMessageProducerFactoryHolder.instance;
    }

    // TODO
    public TransactionMessageProducer getProducer() {
        return null;
    }

}

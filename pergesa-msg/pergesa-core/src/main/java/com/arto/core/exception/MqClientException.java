package com.arto.core.exception;

/**
 * Created by xiong.j on 2016/7/21
 */
public class MqClientException extends RuntimeException {

    public MqClientException(String message) {
        super(message);
    }

    public MqClientException(Throwable e) {
        super(e);
    }

    public MqClientException(String message, Throwable e){
        super(message,e);
    }
}

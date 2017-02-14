package com.arto.event.exception;

/**
 * Created by xiong.j on 2016/7/21
 */
public class PersistentEventLockException extends EventException {

    public PersistentEventLockException(String message) {
        super(message);
    }

    public PersistentEventLockException(Throwable e) {
        super(e);
    }

    public PersistentEventLockException(String message, Throwable e){
        super(message,e);
    }
}

package com.arto.core.common;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by xiong.j on 2017/3/3.
 */
public class DataPipeline<T> {

    private final BlockingQueue<T> pipeline;

    public DataPipeline(int size) {
        this.pipeline = new LinkedBlockingQueue<T>(size);
    }

    public boolean offer(T t) {
        return pipeline.offer(t);
    }

    public boolean offerAll(Collection<T> c) {
        boolean modified = false;
        for (T t : c) {
            if(offer(t))
                modified = true;
        }
        return modified;
    }

    public T poll(long timeout, TimeUnit unit) throws InterruptedException {
        return pipeline.poll(timeout, unit);
    }

    public int size(){
        return pipeline.size();
    }

    public void clear() {
        pipeline.clear();
    }
}

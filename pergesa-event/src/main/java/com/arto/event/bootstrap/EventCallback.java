package com.arto.event.bootstrap;

/**
 * Created by xiong.j on 2017/1/11.
 */
public interface EventCallback<T> {

    /**
     * 异步事件处理完成时的回调接口
     *
     * @param t
     */
    void onCompletion(T t);
}

package com.arto.core.bootstrap;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by xiong.j on 2017/1/12.
 */
@Setter
@Getter
@ToString
public class MqConfig<T> {

    /** 消息中间件类型 */
    private String type;

    /** 目的地 */
    private String destination;

}

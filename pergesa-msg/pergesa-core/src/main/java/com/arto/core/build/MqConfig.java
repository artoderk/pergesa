package com.arto.core.build;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by xiong.j on 2017/1/12.
 */
@Setter
@Getter
public class MqConfig {

    /** 消息中间件类型 */
    private String type;

    /** 目的地 */
    private String destination;

}

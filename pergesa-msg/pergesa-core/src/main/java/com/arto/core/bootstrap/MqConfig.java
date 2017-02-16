package com.arto.core.bootstrap;

import com.arto.core.common.MqTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by xiong.j on 2017/1/12.
 */
@Setter
@Getter
@ToString
public class MqConfig {

    /** 消息中间件类型 */
    private MqTypeEnum type = MqTypeEnum.KAFKA;

    /** 目的地 */
    private String destination;

}

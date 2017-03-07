package com.arto.kafka.event;

import com.arto.core.common.MqTypeEnum;
import com.arto.core.event.MqEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by xiongjie on 2016/12/21.
 */
@Setter
@Getter
@ToString(callSuper = true)
public class KafkaProduceEvent extends MqEvent {

    /** 主键用来负载均衡 */
    private String key;

    /** 分区 */
    private int partition = -1;

    public KafkaProduceEvent(){
        this.setType(MqTypeEnum.KAFKA.getMemo());
    }

}

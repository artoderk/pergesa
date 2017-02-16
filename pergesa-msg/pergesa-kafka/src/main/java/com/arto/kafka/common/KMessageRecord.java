package com.arto.kafka.common;

import com.arto.core.common.MessageRecord;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by xiong.j on 2017/1/13.
 */
@Setter
@Getter
@ToString(callSuper = true)
public class KMessageRecord<T> extends MessageRecord{

    /** 主键 */
    transient private String key;

    /** 分区 */
    transient private int partition = -1;

    public KMessageRecord(T message) {
        super(message);
    }

    public KMessageRecord(String businessId, String businessType, T message){
        super(businessId, businessType, message);
    }

}

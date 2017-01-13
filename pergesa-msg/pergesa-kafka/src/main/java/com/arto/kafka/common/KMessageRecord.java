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
@ToString
public class KMessageRecord extends MessageRecord{

    /** 主键 */
    private String key;

    /** 分区 */
    private int partition = -1;

    public KMessageRecord(){};

    public KMessageRecord(Object message) {
        this.setMessage(message);
    }
}

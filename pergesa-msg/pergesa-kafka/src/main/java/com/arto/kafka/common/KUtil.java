package com.arto.kafka.common;

import com.arto.core.common.MessageRecord;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * Created by xiong.j on 2017/1/20.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KUtil {

    public static String buildMessageId(final ConsumerRecord<String, MessageRecord> record) {
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.K).append("-");
        sb.append(record.topic()).append("-");
        sb.append(record.partition()).append(record.offset());
        return sb.toString();
    }

}

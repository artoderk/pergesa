package com.arto.kafka.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * Created by xiong.j on 2017/1/20.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KUtil {

    /**
     * 获取消息ID
     *  K + '-' + topic + '-' + partition + '-' + offset
     *
     * @param record
     * @return
     */
    public static String buildMessageId(final ConsumerRecord<String, String> record) {
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.K).append("-");
        sb.append(record.topic()).append("-");
        sb.append(record.partition()).append("-").append(record.offset());
        return sb.toString();
    }

}

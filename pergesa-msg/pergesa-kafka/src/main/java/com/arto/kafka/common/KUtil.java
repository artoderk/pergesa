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
     *  topic + '_' + partition + '_' + offset
     *
     * @param record
     * @return
     */
    public static String buildMessageId(final ConsumerRecord<String, String> record) {
        StringBuilder sb = new StringBuilder();
        sb.append(record.topic()).append("_");
        sb.append(record.partition()).append("_").append(record.offset());
        return sb.toString();
    }

    /**
     * 从消息ID中获取Topic命名
     *
     * @param messageId
     * @return
     */
    public static String extractTopic(String messageId){
        // 避免"_"在topic命名中出现，采用从后向前截取的方式
        String val = messageId.substring(0, messageId.lastIndexOf("_"));
        return val.substring(0, val.lastIndexOf("_"));
    }

    public static void main(String args[]){
        String messageId = "pegesa-test_0_35";
        System.out.println(extractTopic(messageId));
    }
}

package com.arto.kafka.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Created by xiong.j on 2017/1/20.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KUtil {

    /**
     * 获取消息ID
     *  'K' + partition + '_' + offset
     *
     * @param partition
     * @param offset
     * @return
     */
    public static String buildMessageId(final int partition, final long offset) {
        StringBuilder sb = new StringBuilder();
        sb.append(partition).append("_");
        sb.append(offset);
        return sb.toString();
    }
}

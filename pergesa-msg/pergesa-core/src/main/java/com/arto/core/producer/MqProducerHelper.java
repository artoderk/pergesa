package com.arto.core.producer;

import com.arto.core.common.MessageRecord;
import com.arto.core.common.MqTypeEnum;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * TODO
 * Created by xiong.j on 2017/2/13.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MqProducerHelper {

    public static void send(MqTypeEnum mqType, String destination, Object message) {

    }

    public static void send(MqTypeEnum mqType, String destination, MessageRecord record) {

    }

    public static void sendNonTx(MqTypeEnum mqType, String destination, MessageRecord record) {

    }
}

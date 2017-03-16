/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
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

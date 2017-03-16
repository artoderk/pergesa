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
package com.arto.core.common;

/**
 * Created by xiong.j on 2017/2/13.
 */
public enum MessagePriorityEnum {
    UNKNOWN(-1, "unknown"),

    HIGH(1, "高"),

    MEDIUM(2, "中"),

    LOW(3, "低");

    private int    code;

    private String memo;

    /**
     * @param code
     * @param memo
     */
    private MessagePriorityEnum(int code, String memo) {
        this.code = code;
        this.memo = memo;
    }

    public int getCode() {
        return code;
    }

    public String getMemo() {
        return memo;
    }

    public static String getMemo(int code) {
        for(MessagePriorityEnum type: MessagePriorityEnum.values()){
            if(type.code == code){
                return type.memo;
            }
        }
        return UNKNOWN.getMemo();
    }

    public static MessagePriorityEnum getEnum(int code) {
        for (MessagePriorityEnum item : values()) {
            //不区分大小写
            if (code == item.getCode()) {
                return item;
            }
        }
        return UNKNOWN;
    }
}

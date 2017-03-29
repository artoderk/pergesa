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
package com.arto.amq.common;

/**
 * Created by xiongjie on 2016/7/21.
 */

public enum DestTypeEnum {

    UNKNOWN(9, "unknown"),

    QUEUE(0, "queue"),

    TOPIC(1, "topic");

    private int    code;

    private String memo;

    /**
     * @param code
     * @param memo
     */
    private DestTypeEnum(int code, String memo) {
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
    	for(DestTypeEnum type: DestTypeEnum.values()){
    		if(type.code == code){
    			return type.memo;
    		}
    	}
    	return UNKNOWN.getMemo();
    }

    public static DestTypeEnum getEnum(int code) {
        for (DestTypeEnum item : values()) {
            //不区分大小写
            if (code == item.getCode()) {
                return item;
            }
        }
        return UNKNOWN;
    }
}

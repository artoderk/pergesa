package com.arto.event.common;

/**
 * Created by xiongjie on 2016/7/21.
 */

public enum EventStatusEnum {
    //SUCCESS(0, "正常完成"),

    UNKNOWN(9, "unknown"),

    WAIT(0, "等待处理"),

    PROCESSING(1, "处理中"),

    SUCCESS(2, "处理成功"),

    // 达到重试次数，需手动确认
    MANUAL_WAIT(3, "等待人工处理");

    private int    code;

    private String memo;

    /**
     * @param code
     * @param memo
     */
    private EventStatusEnum(int code, String memo) {
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
    	for(EventStatusEnum type: EventStatusEnum.values()){
    		if(type.code == code){
    			return type.memo;
    		}
    	}
    	return UNKNOWN.getMemo();
    }

    public static EventStatusEnum getEnum(int code) {
        for (EventStatusEnum item : values()) {
            //不区分大小写
            if (code == item.getCode()) {
                return item;
            }
        }
        return UNKNOWN;
    }
}

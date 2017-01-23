package com.arto.kafka.common;

/**
 * Created by xiongjie on 2016/7/21.
 */

public enum KAcksEnum {

    UNKNOWN(9, "unknown"),

    ACK_NOWAIT(0, "不等待确认"),

    ACK_LEADER(1, "等待主确认"),

    ACK_ALL(-1, "等待全部服务器确认");

    private int    code;

    private String memo;

    /**
     * @param code
     * @param memo
     */
    private KAcksEnum(int code, String memo) {
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
    	for(KAcksEnum type: KAcksEnum.values()){
    		if(type.code == code){
    			return type.memo;
    		}
    	}
    	return UNKNOWN.getMemo();
    }

    public static KAcksEnum getEnum(int code) {
        for (KAcksEnum item : values()) {
            //不区分大小写
            if (code == item.getCode()) {
                return item;
            }
        }
        return UNKNOWN;
    }
}

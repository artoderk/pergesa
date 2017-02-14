package com.arto.core.common;

/**
 * Created by xiong.j on 2017/2/13.
 */
public enum MqTypeEnum {
    UNKNOWN(-1, "unknown"),

    KAFKA(1, "kafka"),

    ACTIVEMQ(2, "activemq");

    private int    code;

    private String memo;

    /**
     * @param code
     * @param memo
     */
    private MqTypeEnum(int code, String memo) {
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
        for(MqTypeEnum type: MqTypeEnum.values()){
            if(type.code == code){
                return type.memo;
            }
        }
        return UNKNOWN.getMemo();
    }

    public static MqTypeEnum getEnum(int code) {
        for (MqTypeEnum item : values()) {
            //不区分大小写
            if (code == item.getCode()) {
                return item;
            }
        }
        return UNKNOWN;
    }
}

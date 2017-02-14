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

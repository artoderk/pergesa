package com.arto.core.common;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xiong.j on 2017/1/13.
 */
@Setter
@Getter
@ToString
public class MessageRecord<T> {

    /** 业务凭证流水号 */
    private String businessId;

    /** 业务类型 */
    private String businessType;

    /** 自定义消息头*/
    private Map<String, Object> properties;

    /** 消息选择Key */
    @Deprecated
    private String selectKey;

    /** 消息中件间生成的Id
     *  Kafka: K + 两位分区号 + offset
     *  AMQ  : messageId
     **/
    private String messageId;

    /** 消息内容 */
    private T message;

    /** 事务 启用后模拟消息两阶段提交 */
    transient private boolean transaction;

    public MessageRecord() {
        this.properties = new HashMap<String, Object>();
    }

    public boolean getBooleanProperty(String key){
        return (Boolean)(properties.get(key));
    }

    public int getIntProperty(String key){
        return (Integer)(properties.get(key));
    }

    public long getLongProperty(String key){
        return (Long)(properties.get(key));
    }

    public String getStringProperty(String key){
        return (String)(properties.get(key));
    }

    public Object getObjectProperty(String key){
        return properties.get(key);
    }
}

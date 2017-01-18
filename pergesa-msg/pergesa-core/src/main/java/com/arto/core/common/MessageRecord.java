package com.arto.core.common;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by xiong.j on 2017/1/13.
 */
@Setter
@Getter
@ToString
public class MessageRecord {

    /** 业务凭证流水号 */
    private String businessId;

    /** 业务类型 */
    private String businessType;

    /** 消息选择Key */
    @Deprecated
    private String selectKey;

    /** 消息内容 */
    private Object message;

    /** 事务 启用后模拟消息两阶段提交 */
    transient private boolean transaction;
}

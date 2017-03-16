package com.arto.event.bootstrap;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by xiongjie on 2016/12/21.
 */
@Setter
@Getter
@ToString
public class Event<T> {

    /** 事件分组 */
    private transient Class group;

    /** 业务凭证流水号 */
    private transient String businessId;

    /** 业务类型 */
    private transient String businessType;

    /** 事件内容 */
    private T payload;

    /** 事件回调 */
    private transient EventCallback<T> callback;

    /** 是否持久化 */
    private transient boolean isPersistent;

    /** 事件重试次数 为-1时无限重试(慎用) */
    private transient int retry;

    /** 消息上下文 */
    private transient EventContext eventContext;
}

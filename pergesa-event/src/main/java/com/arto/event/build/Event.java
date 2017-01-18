package com.arto.event.build;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by xiongjie on 2016/12/21.
 */
@Setter
@Getter
@ToString
public class Event {

    /** 事件分组 */
    private String group;

    /** 业务凭证流水号 */
    private String businessId;

    /** 业务类型 */
    private String businessType;

    /** 事件内容 */
    private String payload;

    /** 事件回调 */
    private EventCallback callback;

    /** 是否持久化 */
    private boolean isPersistent;

    /** 事件重试次数 */
    private int retry;

    /** 消息上下文 */
    private EventContext eventContext;
}

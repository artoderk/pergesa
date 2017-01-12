package com.arto.event.build;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by xiongjie on 2016/12/21.
 */
@Setter
@Getter
public class Event {

    /** 事件分组 */
    private String group;

    /** 事件内容 */
    private String payload;

    /** 事件内容 */
    private EventCallback callback;

    /** 消息上下文 */
    private EventContext eventContext;
}

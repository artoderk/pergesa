package com.arto.event.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;

/**
 * Created by xiongjie on 2016/12/21.
 */
@Setter
@Getter
@ToString
public class EventInfo {

    private long id = -1L;

    private int tag = -1;

    /** (子)系统ID */
    private String systemId;

    /** 业务凭证流水号 */
    private String businessId;

    /** 业务类型 */
    private String businessType;

    /** 事件类型 */
    private String eventType;

    /** 处理状态 */
    private int status = -1;

    /** 事件内容 */
    private String payload;

    /** 下次重试时间 */
    private Timestamp nextRetryTime;

    /** 默认重试次数 */
    private int defaultRetriedCount = -1;

    /** 当前重试次数 */
    private int currentRetriedCount = -1;

    /** 备注 */
    private String memo;

    /** 创建时间 */
    private Timestamp gmtCreated;

    /** 更新时间 */
    private Timestamp gmtModified;

}

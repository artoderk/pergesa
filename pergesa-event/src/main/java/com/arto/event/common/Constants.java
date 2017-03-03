package com.arto.event.common;

import java.text.SimpleDateFormat;

/**
 * Created by xiong.j on 2016/7/26.
 */
public interface Constants {

    String ORACLE = "oracle";

    String POSTGRESQL = "postgresql";

    String DEFAULT_SYSTEM_ID = "webapp";

    /** 主键重复 */
    String KEY_23505 = "23505";

    String PG_DATE_SQL = "current_timestamp(0)::timestamp without time zone";

    String ORACLE_DATE_SQL = "sysdate";

    /** 默认时间解析模板 **/
    SimpleDateFormat DEFAULT_SDF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    int MAX_RECOVERY_DAYS = 7;

    /** 默认报告的目的地 */
    String REPORT_DEST = "pergesa-event-failed-report";

    /** 默认报告的事件类型 */
    String REPORT_EVENT = "com.arto.kafka.event.KafkaReportEvent";
}
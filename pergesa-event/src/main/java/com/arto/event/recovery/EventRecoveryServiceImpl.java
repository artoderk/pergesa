package com.arto.event.recovery;

import com.arto.event.config.ConfigManager;
import com.arto.event.processor.PersistentEventProcessor;
import com.arto.event.storage.EventInfo;
import com.arto.event.storage.EventStorage;
import com.arto.event.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by xiong.j on 2017/1/4.
 */
@Slf4j
@Service
public class EventRecoveryServiceImpl implements EventRecoveryService {

    @Autowired
    private EventStorage eventStorage;

    @Autowired
    private PersistentEventProcessor persistentEventProcessor;

    @Override
    public List<EventInfo> fetchData(List<Integer> tags) {
        // 默认恢复7天前的数据
        Timestamp searchDate = DateUtil.getPrevDayTimestamp(ConfigManager.getInt("event.start.day", 7));

        // id 升序
        // TODO 分页处理, 一次取1000条
        return eventStorage.findSince(ConfigManager.getString("sar.name", "webapp"), tags, searchDate);
    }

    public int execute(List<EventInfo> infos) {
        int successCount = 0;
        for(EventInfo info : infos) {
            persistentEventProcessor.router(info);
            successCount++;
        }
        log.info("Event recovery service executed, data count=" + infos.size() + ", success count=" + successCount);
        return successCount;
    }
}

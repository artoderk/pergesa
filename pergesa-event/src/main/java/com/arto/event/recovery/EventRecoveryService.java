package com.arto.event.recovery;

import com.arto.event.domain.EventInfo;

import java.util.List;

/**
 * 持久化事件的恢复服务
 *
 * Created by xiong.j on 2016/12/21.
 */
public interface EventRecoveryService {

    /**
     * 获取待恢复事务
     *
     * @param tag
     * @return
     */
    List<EventInfo> fetchData(List<Integer> tag);

    /**
     * 处理待恢复事务
     *
     * @param infos
     * @return
     */
    public int execute(List<EventInfo> infos);
}

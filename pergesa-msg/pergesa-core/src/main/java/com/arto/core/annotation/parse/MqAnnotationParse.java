package com.arto.core.annotation.parse;

import com.arto.core.common.MqTypeEnum;
import com.arto.core.config.MqConfigManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by xiong.j on 17/2/15.
 */
@Slf4j
public abstract class MqAnnotationParse implements InitializingBean, BeanPostProcessor {

    protected Set<String> topicKeySet = new HashSet<String>();

    protected String defaultType;

    public void afterPropertiesSet() throws Exception {
        defaultType = MqConfigManager.getString("default.mq.type", MqTypeEnum.KAFKA.getMemo());
    }

    protected Object getTargetBean(Object bean) {
        if (bean instanceof Advised) {
            try {
                return ((Advised) bean).getTargetSource().getTarget();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return bean;
    }

}

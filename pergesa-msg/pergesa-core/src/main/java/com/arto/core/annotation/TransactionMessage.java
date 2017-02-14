package com.arto.core.annotation;

import java.lang.annotation.*;

/**
 * 事务消息入口，模拟消息两阶段提交。
 * JDBC事务有效期内发送的消息默认都将启用事务发送，如确认该消息不用事务发送，可手动调用非事务消息发送接口sendNonTx
 * TODO 拦截器与事务上下文
 * Created by xiong.j on 2017/2/13.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@Documented
@Inherited
public @interface TransactionMessage {
}

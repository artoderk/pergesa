package com.arto.event.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

/**
 * 以静态变量保存Spring的BeanFactory
 *
 * Created by xiong.j on 2017/1/13.
 */
@Component
public class SpringContextHolder implements BeanFactoryPostProcessor {

	/**
	 * BeanFactory静态变量.
	 */
	private static BeanFactory beanFactory;

	/**
	 * 根据命名从BeanFactory静态变量中取得Bean, 自动转型为所赋值对象的类型.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getBean(String name) {
		checkApplicationContext();
		return (T) beanFactory.getBean(name);
	}

	/**
	 * 根据类型从BeanFactory静态变量中取得Bean, 自动转型为所赋值对象的类型.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getBean(Class<T> clazz) {
		checkApplicationContext();
		return (T) beanFactory.getBean(clazz);
	}

	private static void checkApplicationContext() {
		if (beanFactory == null) {
			throw new IllegalStateException(
					"BeanFactory not found.");
		}
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		SpringContextHolder.beanFactory = beanFactory;
	}
}

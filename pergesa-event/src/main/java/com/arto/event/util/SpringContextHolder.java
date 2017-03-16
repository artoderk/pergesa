/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
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

	public static BeanFactory getBeanFactory(){
		checkApplicationContext();
		return beanFactory;
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

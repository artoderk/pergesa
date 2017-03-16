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
package com.arto.event.service;

import com.arto.event.bootstrap.Event;

/**
 * Created by xiong.j on 2017/1/4.
 */
public interface EventAdviceService {

    /**
     * Event处理前时的处理
     *
     * @param event
     * @return
     * @throws
     */
    public Event before(Event event) throws Exception;

    /**
     * Event处理后时的处理
     *
     * @param event
     */
    public void after(Event event);

    /**
     * Event处理失败时的处理
     *
     * @param event
     * @param throwable
     */
    public void fail(Event event, Throwable throwable);

}

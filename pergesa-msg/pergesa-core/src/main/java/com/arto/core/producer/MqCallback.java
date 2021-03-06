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
package com.arto.core.producer;

import com.arto.event.bootstrap.EventCallback;

/**
 * Created by xiong.j on 2017/1/11.
 */
public interface MqCallback<T> extends EventCallback<T> {

//    /**
//     * 异步事件处理完成时的回调接口
//     *
//     * @param event
//     */
//    public void onCompletion(Event event){
//        onCompletion((MessageRecord)event.getPayload());
//    }
//
//    /**
//     * 异步消息处理完成时的回调接口
//     *
//     * @param record
//     */
//    protected abstract void onCompletion(MessageRecord record);

}

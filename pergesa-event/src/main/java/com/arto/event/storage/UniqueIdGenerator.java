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
package com.arto.event.storage;

import com.sohu.idcenter.IdWorker;

/**
 * Created by xiong.j on 2016/7/22.
 */
public class UniqueIdGenerator {
    private final static UniqueIdGenerator UNIQUE_ID_GENERATOR = new UniqueIdGenerator();

    private IdWorker idWorker;

    private UniqueIdGenerator() {
        getIdWorker();
    }

    private IdWorker getIdWorker() {
        long idepo = System.currentTimeMillis() - 3600 * 1000L;
        idWorker = new IdWorker(idepo);
        return idWorker;
    }

    public static long next() {
        return UNIQUE_ID_GENERATOR.idWorker.getId();
    }

    private static boolean isWindowsOS() {
        boolean isWindowsOS = false;
        String osName = System.getProperty("os.name");
        if (osName.toLowerCase().indexOf("windows") > -1) {
            isWindowsOS = true;
        }
        return isWindowsOS;
    }

    public static void main(String args[]){
        System.out.println(UniqueIdGenerator.next());

    }
}

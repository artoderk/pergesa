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

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtil {
 
	private static final long SECOND = 1000;
	
	private static final long MINUTE = 60 * SECOND;
	
	private static final long HOUR = 60 * MINUTE;
	
	private static final long DAY = 24 * HOUR;
	
	private DateUtil(){}
	
	public static Timestamp getPrevDayTimestamp(int interval){
		return new Timestamp(getPrevDayMillis(interval));
	}

	private static long getPrevDayMillis(int interval){
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis() - DAY * interval);
		cal.set(Calendar.HOUR, -12);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		return cal.getTimeInMillis();
	}

	public static Date getPrevSecDate(int interval){
		return new Date(System.currentTimeMillis() - SECOND * interval);
	}

	public static Timestamp getPrevSecTimestamp(int interval){
		return new Timestamp(System.currentTimeMillis() - SECOND * interval);
	}

	public static void main(String args[]){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");		
		System.out.println(sdf.format(new Date(System.currentTimeMillis())));
		System.out.println(sdf.format(DateUtil.getPrevDayTimestamp(5)));


	}
}

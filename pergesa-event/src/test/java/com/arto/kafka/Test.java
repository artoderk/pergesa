package com.arto.kafka;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by xiong.j on 2017/1/9.
 */
public class Test {

    public Date getNextRetryTime(int defaultRetriedCount, int currentRetriedCount){
        // TODO 重试次数间隔
        return new Date(System.currentTimeMillis()
                + Math.round(Math.pow(9, defaultRetriedCount - currentRetriedCount))
                * 1000);
    }

    public static void main(String args[]){
        Test t = new Test();
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        Timestamp timestamp = new Timestamp(time);
        System.out.println("util date:" + new java.util.Date(time));
        System.out.println("Sql date:" + new Timestamp(time));
        System.out.println(date.getTime() + "@@" + timestamp.getTime());
        for (int i = 0; i <= 3; i++) {
            System.out.println(Math.round(Math.pow(9, 3 - i))
                    * 1000);

            System.out.println(t.getNextRetryTime(3, i).getTime());
        }
    }
}

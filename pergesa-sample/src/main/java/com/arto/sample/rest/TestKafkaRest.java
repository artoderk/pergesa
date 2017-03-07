package com.arto.sample.rest;

import com.alibaba.fastjson.JSON;
import com.arto.core.annotation.Producer;
import com.arto.core.common.MessagePriorityEnum;
import com.arto.core.producer.MqProducer;
import com.arto.sample.domain.OrderDO;
import com.arto.sample.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 下单
 *
 * Created by xiong.j on 17/2/15.
 */
@Controller
@RequestMapping(value = "/test")
public class TestKafkaRest {

    /** 下单处理Service */
    @Autowired
    private OrderService orderService;

    @Producer(destination = "pegesa-test", priority = MessagePriorityEnum.HIGH)
    private MqProducer<OrderDO> highProducer;

    @Producer(destination = "pegesa-test-medium", priority = MessagePriorityEnum.MEDIUM)
    private MqProducer<OrderDO> mediumProducer;

    @Producer(destination = "pegesa-test-low", priority = MessagePriorityEnum.LOW)
    private MqProducer<OrderDO> lowProducer;

    /**
     * 并发测试接口，同时发送三个TOPIC
     *
     * @param amount
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/con/{amount}")
    @ResponseBody
    public String concurrence(@PathVariable int amount)throws Exception {
        return "high" + send(highProducer, amount, 0) + ", medium" +  send(mediumProducer, amount, 0)
                + ", low:" + send(lowProducer, amount, 0);
    }


    /**
     * 高优先级接口，同步2复制因子，性能最慢，单线程约300/S
     *
     * @param amount
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/high/{amount}")
    @ResponseBody
    public String highTest(@PathVariable int amount)throws Exception {
        return send(highProducer, amount, 0);
    }

    /**
     * 中优先级接口，同步无复制，性能普通，单线程约500/S
     *
     * @param amount
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/medium/{amount}")
    @ResponseBody
    public String mediumTest(@PathVariable int amount)throws Exception {
        return send(mediumProducer, amount, 0);
    }

    /**
     * 低优先级接口，异步无复制，性能最高，单线程约2W/S
     *
     * @param amount
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/low/{amount}")
    @ResponseBody
    public String lowTest(@PathVariable int amount)throws Exception {
        return send(lowProducer, amount, 0);
    }

    /**
     * 多线程测试
     *
     * @param priority 优先级
     * @param size 线程数量
     * @param amount 发送总量
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/thread/{priority}/{size}/{amount}")
    @ResponseBody
    public String threadTest(@PathVariable int priority, @PathVariable int size, @PathVariable int amount)throws Exception {
        MqProducer<OrderDO> producer = null;
        if (priority == 1) {
            producer = highProducer;
        } else if (priority == 2) {
            producer = mediumProducer;
        } else if (priority == 3) {
            producer = lowProducer;
        }
        return thread(producer, size, amount);
    }

    private String send(MqProducer<OrderDO> producer, int amount, int id) {
        OrderDO bean = new OrderDO();
        bean.setOrderId(1);
        bean.setStatus("OrderDO.test");

        long start = System.currentTimeMillis();
        try {
            for (int i = 0; i< amount; i++) {
                bean.setOrderId(i + id);
                producer.send(bean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        long end = System.currentTimeMillis();

        return "start time=" + start + ", end time=" + end + ", spent time=" + (end - start);
    }

    private String thread(final MqProducer<OrderDO> producer, final int size, final int amount){
        ExecutorService executor = Executors.newFixedThreadPool(size);
        ArrayList<Future<Boolean>> resultList = new ArrayList<Future<Boolean>>();
        Future<Boolean> future = null;

        long start = System.currentTimeMillis();

        final int piece = amount / size;

        for (int i = 1; i <= size; i++) {
            final int n = i;
            future = executor.submit(new Callable<Boolean>() {
                public Boolean call() throws Exception{
                    send(producer, piece, piece * (n - 1) + 1);
                    return true;
                }
            });
            resultList.add(future);
        }
        try {
            for (Future<Boolean> f : resultList) {
                System.out.println("Thread result:" + f.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        executor.shutdown();
        return "start time=" + start + ", end time=" + end + ", spent time=" + (end - start);
    }

    public static void main(String args[]) throws Exception{
        TestKafkaRest t = new TestKafkaRest();
//        t.mediumProducer = new MqProducer<OrderDO>() {
//            @Override
//            public void send(OrderDO message) throws MqClientException {
//                System.out.println("Thread:" + Thread.currentThread().getName() + ", message:" + message);
//            }
//
//            @Override
//            public void send(MessageRecord<OrderDO> record) throws MqClientException {
//
//            }
//
//            @Override
//            public void sendNonTx(MessageRecord<OrderDO> record) throws MqClientException {
//
//            }
//        };
//        t.threadTest(2, 5, 10);

        OrderDO bean = new OrderDO();
        bean.setOrderId(1);
        bean.setStatus("OrderDO.test");
        bean.setOrderId(726319);
        System.out.println(JSON.toJSONString(bean).getBytes("utf-8").length);
    }
}
package com.arto.event.event;

import com.arto.event.bootstrap.Event;
import com.arto.event.bootstrap.EventBusFactory;
import com.arto.event.bootstrap.EventListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * EventBusFactory Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>12, 30, 2016</pre>
 */
public class EventBusFactoryTest {

    private ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: getInstance()
     */
    @Test
    public void testGetInstance() throws Exception {
        EventBusFactory.getInstance().register(Event.class, new EventListener<Event>() {
            @Override
            public void listen(Event traceEvent) {
                System.out.println("Thread:" + Thread.currentThread().getId() + ", This is event:" + traceEvent.toString());
            }

            @Override
            public String getIdentity() {
                return "listener1";
            }
        });

      //  for (int i = 0; i < 10; i++) {

            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    Event e = new Event();
                    e.setPayload("testEvent");
                    System.out.println("Thread:" + Thread.currentThread().getId() + ", post event:" + e.toString());
                    EventBusFactory.getInstance().post(e);
                }
            });
       // }
        executorService.shutdown();
    }

    /**
     * Method: register(final String group, final EventListener listener)
     */
    @Test
    public void testRegisterForGroupListener() throws Exception {
    }

    /**
     * Method: post(final Event event)
     */
    @Test
    public void testPostEvent() throws Exception {
    }

    /**
     * Method: clearListeners(final String jobName)
     */
    @Test
    public void testClearListenersJobName() throws Exception {
    }

    /**
     * Method: register(final EventListener listener)
     */
    @Test
    public void testRegisterListener() throws Exception {
    }

    /**
     * Method: clearListeners()
     */
    @Test
    public void testClearListeners() throws Exception {
    }


    public static void main(String args[]) {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        EventBusFactory.getInstance().register(Event.class, new EventListener<Event>() {
            @Override
            public void listen(Event traceEvent) {
                System.out.println("Thread:" + Thread.currentThread().getId() + ", This is event:" + traceEvent.toString());
            }

            @Override
            public String getIdentity() {
                return "listener1";
            }
        });

        for (int i = 0; i < 10; i++) {
            executorService.submit(new Executor(i));
        }

        executorService.shutdown();
    }

}

class Executor implements Runnable{
    private int i;

    public Executor(final int i){
        this.i = i;
    }
    @Override
    public void run() {
        Event e = new Event();
        e.setPayload("testEvent" + i);
        if (i == 2) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
        System.out.println("Thread:" + Thread.currentThread().getId() + ", post event:" + e.toString());
        EventBusFactory.getInstance().post(e);
    }
}

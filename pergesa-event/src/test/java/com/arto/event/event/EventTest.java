package com.arto.event.event;

import com.arto.event.build.Event;
import com.arto.event.build.EventBusFactory;
import com.arto.event.build.EventListener;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.junit.Test;

/**
 * Created by xiong.j on 2017/1/23.
 */
public class EventTest {

    @Test
    public void sendEvent(){
        EventBusFactory.getInstance().register(TestBeanEvent1.class, new EventListener<TestBeanEvent1>() {
            @Override
            @Subscribe
            @AllowConcurrentEvents
            public void listen(TestBeanEvent1 event) {
                System.out.println("This is event:" + event);
            }

            @Override
            public String getIdentity() {
                return "listener1";
            }
        });

        EventBusFactory.getInstance().post(new TestBeanEvent1(1, "TestBeanEvent1"));

        EventBusFactory.getInstance().post(new TestBeanEvent2(1, "TestBeanEvent2"));
    }

    @Test
    public void sendEventOrigin(){
        EventBus eventBus = new EventBus();
        eventBus.register(new EventListener1());
        eventBus.register(new EventListener2());

        eventBus.post(new TestBeanEvent1(1, "TestBeanEvent1"));

        eventBus.post(new TestBeanEvent2(1, "TestBeanEvent2"));
    }

    private static class EventListener1 implements EventListener<TestBeanEvent1>{
        @Subscribe
        @AllowConcurrentEvents
        public void listen(TestBeanEvent1 event) {
            System.out.println("This is EventListener1:" + event);
        }

        public String getIdentity() {
            return null;
        }
    }

    private static class EventListener2 implements EventListener<TestBeanEvent2> {
        @Subscribe
        @AllowConcurrentEvents
        public void listen(TestBeanEvent2 event) {
            System.out.println("This is EventListener2:" + event);
        }

        public String getIdentity() {
            return null;
        }
    }

    private static class TestBeanEvent1 extends Event{
        int id;
        String name;

        public TestBeanEvent1(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private static class TestBeanEvent2 extends Event{
        int id;
        String name;

        public TestBeanEvent2(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }


}

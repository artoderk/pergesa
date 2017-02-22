import com.arto.core.annotation.Producer;
import com.arto.core.producer.MqCallback;
import com.arto.core.producer.MqProducer;

import java.lang.reflect.Constructor;

/**
 * Created by xiong.j on 2017/2/20.
 */
public class TestInnerClass {

    @Producer(destination = "test")
    private MqProducer producer;

    private static class InnerClass implements MqCallback{

        @Override
        public void onCompletion(Object o) {
            System.out.println("onCompletion");
        }
    }

    public static void main(String args[]) throws Exception {
        Class clz = InnerClass.class;
        System.out.println(InnerClass.class.getDeclaredConstructors().length);
        Constructor c = InnerClass.class.getDeclaredConstructors()[0];
        //将c设置成可访问
        c.setAccessible(true);
        //用构造函数初始化内部类
        MqCallback callback = (MqCallback) c.newInstance();
        callback.onCompletion("TEST");
        System.out.println(clz);
    }
}

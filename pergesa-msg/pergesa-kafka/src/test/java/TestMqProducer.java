import com.arto.core.bootstrap.MqClient;
import com.arto.core.common.MessagePriorityEnum;
import com.arto.core.producer.MqProducer;
import com.arto.kafka.producer.binding.KafkaProducerConfig;
import common.DefaultTestCase;
import common.TestMessageBean;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiong.j on 2017/1/16.
 */
public class TestMqProducer extends DefaultTestCase {

    public static void main(String args[]){
//        KMessageRecord m = new KMessageRecord();
//
//        m.setMessage("Message");
//        m.setKey("test");
//        System.out.println(JSON.toJSONString(m));

    }

    @Test
    public void sendOnce() throws Exception {
        MqProducer producer = MqClient.buildProducer(new KafkaProducerConfig("pegesa-test-low", MessagePriorityEnum.LOW));

        List<String> list = new ArrayList<String>();
        list.add("Test1");
        list.add("Test2");
        TestMessageBean bean = new TestMessageBean();
        bean.setId(1);
        bean.setName("TestMessageBean");
        bean.setList(list);

        try {
            producer.send(bean);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void send10000() throws Exception {
        MqProducer producer = MqClient.buildProducer(new KafkaProducerConfig("pegesa-test-low", MessagePriorityEnum.LOW));

        List<String> list = new ArrayList<String>();
        list.add("Test1");
        list.add("Test2");
        TestMessageBean bean = new TestMessageBean();
        bean.setId(1);
        bean.setName("TestMessageBean");
        bean.setList(list);

        long start = System.currentTimeMillis();
        try {
            for (int i = 1; i< 10000; i++) {
                bean.setId(i);
                producer.send(bean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Producer是懒加载，计数不准
        System.out.println(System.currentTimeMillis() - start);
    }

    @Test
    public void sendTx() throws Exception {
        MqProducer producer = MqClient.buildProducer(new KafkaProducerConfig("test", MessagePriorityEnum.HIGH));

        List<String> list = new ArrayList<String>();
        list.add("Test1");
        list.add("Test2");
        TestMessageBean bean = new TestMessageBean();
        bean.setId(1);
        bean.setName("TestMessageBean");
        bean.setList(list);
        try {
            producer.send(bean);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}


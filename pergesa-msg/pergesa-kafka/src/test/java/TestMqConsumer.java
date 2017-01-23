import com.arto.core.build.MqClient;
import com.arto.core.common.MessageRecord;
import com.arto.core.consumer.MqConsumer;
import com.arto.core.consumer.MqListener;
import com.arto.kafka.consumer.binding.KafkaConsumerConfig;
import common.TestMessageBean;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by xiong.j on 2017/1/23.
 */
public class TestMqConsumer /*extends DefaultTestCase*/ {

    public void testConsumer() {
        MqConsumer consumer = MqClient.buildConsumer(new KafkaConsumerConfig("pegesa-test"));
        consumer.receive(TestMessageBean.class, new MqListener() {
            @Override
            public void onMessage(MessageRecord record) {
                System.out.println("############ onMessage:" + record);
            }

            @Override
            public boolean checkRedeliver(MessageRecord record) {
                return false;
            }
        });
    }

    public static void main(String args[]){
        new ClassPathXmlApplicationContext("classpath:pergesa-test.xml");
        TestMqConsumer t = new TestMqConsumer();
        t.testConsumer();
    }
}

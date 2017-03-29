import com.arto.core.bootstrap.MqClient;
import com.arto.core.common.MessagePriorityEnum;
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
        KafkaConsumerConfig config = new KafkaConsumerConfig("pegesa-test");
        config.setListener(new MqListener<TestMessageBean>() {
            @Override
            public void onMessage(MessageRecord<TestMessageBean> record) {
                System.out.println("############ Listener onMessage:" + record);
            }

            @Override
            public boolean checkRedeliver(MessageRecord<TestMessageBean> record) {
                return false;
            }
        });
        config.setPriority(MessagePriorityEnum.HIGH.getCode());
        config.setBatchSize(10);
        MqConsumer consumer = MqClient.buildConsumer(config);
    }

    public static void main(String args[]){
        new ClassPathXmlApplicationContext("classpath:pergesa-test.xml");
        TestMqConsumer t = new TestMqConsumer();
        t.testConsumer();
        //SpringThreadPoolUtil.getNewPool("pegesa-test",  2, 2, 50, null);
    }
}

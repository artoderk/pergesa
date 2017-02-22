import com.arto.core.bootstrap.MqClient;
import com.arto.core.common.MessageRecord;
import com.arto.core.consumer.MqListener;
import com.arto.event.recovery.EventRecoveryService;
import com.arto.event.storage.EventInfo;
import com.arto.event.util.SpringContextHolder;
import com.arto.kafka.consumer.binding.KafkaConsumerConfig;
import common.TestMessageBean;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiong.j on 2017/1/25.
 */
public class TestRecover {

    public static void main(String args[]){
        new ClassPathXmlApplicationContext("classpath:pergesa-test.xml");
        MqClient.buildConsumer(new KafkaConsumerConfig("pegesa-test", new MqListener<TestMessageBean>() {
            @Override
            public void onMessage(MessageRecord<TestMessageBean> record) {
                System.out.println("############ Listener onMessage:" + record);
            }

            @Override
            public boolean checkRedeliver(MessageRecord<TestMessageBean> record) {
                return false;
            }
        }));

        EventRecoveryService eventRecoveryService = SpringContextHolder.getBean(EventRecoveryService.class);
        List<Integer> tag = new ArrayList<Integer>();
        tag.add(1);
        tag.add(4);
        tag.add(6);
        tag.add(8);
        List<EventInfo> list = eventRecoveryService.fetchData(tag);
        eventRecoveryService.execute(list);
    }

}

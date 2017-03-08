import com.alibaba.fastjson.JSON;
import com.arto.core.bootstrap.MqClient;
import com.arto.core.common.MessagePriorityEnum;
import com.arto.core.common.MessageRecord;
import com.arto.core.consumer.MqConsumer;
import com.arto.core.consumer.MqListener;
import com.arto.core.producer.MqProducer;
import com.arto.event.util.TypeReferenceUtil;
import com.arto.kafka.consumer.binding.KafkaConsumerConfig;
import com.arto.kafka.producer.binding.KafkaProducerConfig;
import common.ComplexJsonBean;

/**
 * Created by xiong.j on 2017/1/23.
 */
public class TestComplexJson /*extends DefaultTestCase*/ {

    public void testConsumer() {
        KafkaConsumerConfig config = new KafkaConsumerConfig("pegesa-test-low");
        config.setListener(new MqListener<ComplexJsonBean<ComplexJsonBean.AA, ComplexJsonBean.BB, ComplexJsonBean.CC<ComplexJsonBean.AA, ComplexJsonBean.BB>>>() {
            @Override
            public void onMessage(MessageRecord<ComplexJsonBean<ComplexJsonBean.AA, ComplexJsonBean.BB, ComplexJsonBean.CC<ComplexJsonBean.AA, ComplexJsonBean.BB>>> record) {
                //System.out.println("############ Listener onMessage:" + record);
            }

            @Override
            public boolean checkRedeliver(MessageRecord<ComplexJsonBean<ComplexJsonBean.AA, ComplexJsonBean.BB, ComplexJsonBean.CC<ComplexJsonBean.AA, ComplexJsonBean.BB>>> record) {
                return false;
            }
        });
        config.setPriority(MessagePriorityEnum.LOW.getCode());
        config.setAckSize(10);
        MqConsumer consumer = MqClient.buildConsumer(config);
    }

    public void sendOnce() throws Exception {
        MqProducer producer = MqClient.buildProducer(new KafkaProducerConfig("pegesa-test-low", MessagePriorityEnum.LOW));

        String jsonStr = "{\"testa\": {\"aa\": \"aaaa\"},\"testc\": {\"a\": {\"aa\": \"aaaa\"},\"b\": {\"bb\": [\"b\",\"b\",\"b\"]}}}";

        try {
            producer.send(jsonStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) throws Exception {
//        new ClassPathXmlApplicationContext("classpath:pergesa-test.xml");
//        TestComplexJson t = new TestComplexJson();
//        t.sendOnce();
//        t.testConsumer();


        String jsonStr = "{\"message\":{\"testa\":{\"aa\":\"aaaa\"},\"testc\":{\"a\":{\"aa\":\"aaaa\"},\"b\":{\"bb\":[\"b\",\"b\",\"b\"]}}}}";
        MessageRecord<ComplexJsonBean<ComplexJsonBean.AA, ComplexJsonBean.BB, ComplexJsonBean.CC<ComplexJsonBean.AA, ComplexJsonBean.BB>>> obj
                = JSON.parseObject(jsonStr, TypeReferenceUtil.getType(
                        new MqListener<ComplexJsonBean<ComplexJsonBean.AA, ComplexJsonBean.BB, ComplexJsonBean.CC<ComplexJsonBean.AA, ComplexJsonBean.BB>>>() {
                            @Override
                            public void onMessage(MessageRecord<ComplexJsonBean<ComplexJsonBean.AA, ComplexJsonBean.BB, ComplexJsonBean.CC<ComplexJsonBean.AA, ComplexJsonBean.BB>>> record) {
                                //System.out.println("############ Listener onMessage:" + record);
                            }

                            @Override
                            public boolean checkRedeliver(MessageRecord<ComplexJsonBean<ComplexJsonBean.AA, ComplexJsonBean.BB, ComplexJsonBean.CC<ComplexJsonBean.AA, ComplexJsonBean.BB>>> record) {
                                return false;
                            }
                        }
                )
        );

        System.out.println(obj.getMessage().getTesta().getAa());
        System.out.println(obj.getMessage().getTestc().getB().getBb());
    }
}

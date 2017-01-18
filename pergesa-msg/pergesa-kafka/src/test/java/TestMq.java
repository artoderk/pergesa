import com.arto.core.build.MqClient;
import com.arto.core.producer.MqProducer;
import com.arto.kafka.producer.binding.KafkaProducerConfig;
import common.DefaultTestCase;
import org.junit.Test;

/**
 * Created by xiong.j on 2017/1/16.
 */
public class TestMq extends DefaultTestCase {

    public static void main(String args[]){
//        KMessageRecord m = new KMessageRecord();
//
//        m.setMessage("Message");
//        m.setKey("test");
//        System.out.println(JSON.toJSONString(m));

    }

    @Test
    public void send() throws Exception {
        MqProducer producer = MqClient.buildProducer(new KafkaProducerConfig("test"));
        try {
            producer.send(new TBean(1, "ttt11"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void sendTx() throws Exception {
        MqProducer producer = MqClient.buildProducer(new KafkaProducerConfig("test", true));
        try {
            producer.send(new TBean(1, "ttt11"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class TBean {

        private int id;

        private String value = "TBean.value";

        public TBean(int id, String value) {
            this.id = id;
            this.value = value;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}


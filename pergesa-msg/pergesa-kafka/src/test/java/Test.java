import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.arto.core.common.MessageRecord;
import com.arto.core.consumer.MqListener;
import com.arto.kafka.event.KafkaConsumeEvent;
import common.TestMessageBean;

import java.util.Map;

/**
 * Created by xiong.j on 2017/1/24.
 */
public class Test {

    public static void main(String args[]) throws Throwable {
//        String str = "{\"message\":{\"id\":1,\"list\":[\"Test1\",\"Test2\"],\"name\":\"TestMessageBean\"}}";
//        MessageRecord testMessageBean = (MessageRecord)parse(str);
//
//        System.out.println(testMessageBean);
//
//        Class c = TestMessageBean.class;
//        MessageRecord<TestMessageBean> t1 = JSON.parseObject(str, TypeReferenceUtil.getType(new TestListener(), "onMessage"));
//        Map<String, WeakReference<TestMessageBean>> map = new HashMap<String, WeakReference<TestMessageBean>>();
//        map.put("test1", new WeakReference<TestMessageBean>(new TestMessageBean()));
//        while (map.get("test1").get() != null) {
//            try {
//                Thread.sleep(500);
//            } catch (InterruptedException ignored) {
//            }
//            System.out.println("Checking for empty");
//            System.gc();
//            System.out.println("empty=" + map.get("test1").get());
//        }
//        System.out.println(System.getProperty("zkServers"));
//        System.out.println(System.getProperties());
        String jstr = "{\"destination\":\"pegesa-test\",\"payload\":{\"message\":{\"amount\":0,\"orderId\":0,\"productId\":0,\"status\":\"OrderDO.test\",\"userId\":0}},\"priority\":1,\"type\":\"kafka\",\"messageId\":\"0_158010\"}";
        JSONObject jsonObject = (JSONObject) JSON.parseObject(jstr, KafkaConsumeEvent.class).getPayload();
        Map map = (Map) jsonObject.get("message");
        map.put("messageId", "mid399002");
        System.out.println(jsonObject.toString());


    }

    public static Object parse(String message) throws Throwable {
        return com.alibaba.fastjson.JSON.parseObject(message,
                new TypeReference<com.arto.core.common.MessageRecord<common.TestMessageBean>>() {
                });
    }

    /*public static TypeReference getTypeReference(){
        TypeReference typeReference = new TypeReference(){};
        ClassPool pool = ClassPool.getDefault();
        try {
            CtClass clz = pool.get(typeReference.getClass().getName());
            ClassFile srcFile = clz.getClassFile2();

            SignatureAttribute signatureAttribute = new
                    SignatureAttribute(srcFile.getConstPool(),
                    "()Ljava/util/List<Ljava/lang/String;>;");
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }*/

    private static class TestListener implements MqListener<TestMessageBean> {

        @Override
        public void onMessage(MessageRecord<TestMessageBean> record) {
            System.out.println("TestListener" + record);
        }

        @Override
        public boolean checkRedeliver(MessageRecord record) {
            return false;
        }
    }

}


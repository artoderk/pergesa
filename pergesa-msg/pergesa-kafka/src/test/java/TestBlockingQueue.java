import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by xiong.j on 2017/1/23.
 */
public class TestBlockingQueue {

    /** 拉取的消息集合(以Topic分类) */
    private Map<String, LinkedBlockingQueue<List<String>>> topicRecords
            = new ConcurrentHashMap<String, LinkedBlockingQueue<List<String>>>();


    public static void main(String args[]) throws InterruptedException {

        TestBlockingQueue testBlockingQueue = new TestBlockingQueue();
        // 初始化Topic消费线程
        LinkedBlockingQueue<List<String>> topicQueue
                = new LinkedBlockingQueue<List<String>>();
        testBlockingQueue.topicRecords.put("test", topicQueue);

        for (int i=1; i<4; i++) {
            testBlockingQueue.topicRecords.get("test").put(Collections.singletonList("record" + i));
        }

        System.out.println(testBlockingQueue.topicRecords.get("test"));
    }
}

import com.arto.event.recovery.EventRecoveryService;
import com.arto.event.storage.EventInfo;
import com.arto.event.util.SpringContextHolder;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.*;

/**
 * Created by xiong.j on 2017/1/25.
 */
public class TestRecover {

    public static void main(String args[]){
        new ClassPathXmlApplicationContext("classpath:pergesa-test.xml");
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

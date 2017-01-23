package common;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Created by xiong.j on 2017/1/22.
 */
@Getter
@Setter
@ToString
public class TestMessageBean {

    int id;

    String name;

    List<String> list;
}

package common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.List;

/**
 * Created by xiong.j on 2017/3/8.
 */
public class ComplexJsonBean<T, M, N> {

    private T testa;
    private N testc;

    public static void main(String[] args) {
        String jsonStr = "{\"testa\": {\"aa\": \"aaaa\"},\"testc\": {\"a\": {\"aa\": \"aaaa\"},\"b\": {\"bb\": [\"b\",\"b\",\"b\"]}}}";
        Object map = JSON.parseObject(jsonStr);
        System.out.println(map);

        ComplexJsonBean<AA, BB, CC<AA, BB>> clazz = JSON.parseObject(jsonStr,
                new TypeReference<ComplexJsonBean<AA, BB, CC<AA, BB>>>() {});
        System.out.println(clazz.getTesta().getAa());
        System.out.println(clazz.getTestc().getB().getBb());
    }

    public T getTesta() {
        return testa;
    }

    public void setTesta(T testa) {
        this.testa = testa;
    }

    public N getTestc() {
        return testc;
    }

    public void setTestc(N testc) {
        this.testc = testc;
    }

    public static class AA {
        private String aa;

        public String getAa() {
            return aa;
        }

        public void setAa(String aa) {
            this.aa = aa;
        }
    }

    public static class BB {
        private List<String> bb;

        public List<String> getBb() {
            return bb;
        }

        public void setBb(List<String> bb) {
            this.bb = bb;
        }
    }

    public static class CC<T, M> {
        private T a;
        private M b;

        public T getA() {
            return a;
        }

        public void setA(T a) {
            this.a = a;
        }

        public M getB() {
            return b;
        }

        public void setB(M b) {
            this.b = b;
        }


    }
}

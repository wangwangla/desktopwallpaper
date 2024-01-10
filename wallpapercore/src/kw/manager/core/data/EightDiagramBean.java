package kw.manager.core.data;

/**
 * @Auther jian xian si qi
 * @Date 2024/1/10 20:59
 */
public class EightDiagramBean {
    public void test(){
        String arr[] = {
               "111","011","101","001","110","010","100","000"
        };
        for (String s : arr) {
            for (String s1 : arr) {
                System.out.println(s+s1);
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        EightDiagramBean bean = new EightDiagramBean();
        bean.test();
    }
}

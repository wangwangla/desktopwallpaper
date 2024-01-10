package kw.manager.core.data;

import com.badlogic.gdx.utils.Array;

/**
 * @Auther jian xian si qi
 * @Date 2024/1/10 20:59
 */
public class EightDiagramData {
    public Array<String> eightData(){
        Array<String> array = new Array<>();
        String arr[] = {
               "111","011","101","001","110","010","100","000"
        };
        for (String s : arr) {
            for (String s1 : arr) {
                System.out.println(s+s1);
                array.add(s1+s);
            }
            System.out.println();
        }
        return array;
    }

    public static void main(String[] args) {
        EightDiagramData bean = new EightDiagramData();
        bean.eightData();
    }
}

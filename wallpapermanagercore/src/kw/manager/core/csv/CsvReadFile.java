package kw.manager.core.csv;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.wallper.csv.CsvReader;
import com.wallper.csv.ReadCvs;

public class CsvReadFile {
    private static ArrayMap<String,Array<DataBean>> chongwuMap;
    private static ArrayMap<String,Array<DataBean>> wallMap;

    public static void readWall(){
        if (wallMap!=null) {
            wallMap.clear();
        }else {
            wallMap = new ArrayMap<>();
        }

        ReadCvs readCvs = new ReadCvs();
        Array<DataBean> dataBeans = new Array<>();
        readCvs.readMethodMethod(dataBeans, Gdx.files.internal("wallResource/wall.csv").reader(), DataBean.class);

        for (DataBean dataBean : dataBeans) {
            Array<DataBean> strings = wallMap.get(dataBean.getType());
            if (strings == null){
                strings = new Array<>();
                wallMap.put(dataBean.getType(),strings);
            }
            strings.add(dataBean);
        }
    }


    public static void readCw(){
        if (chongwuMap!=null) {
            chongwuMap.clear();
        }else {
            chongwuMap = new ArrayMap<>();
        }

        ReadCvs readCvs = new ReadCvs();
        Array<DataBean> dataBeans = new Array<>();
        readCvs.readMethodMethod(dataBeans, Gdx.files.internal("wallResource/pet.csv").reader(), DataBean.class);

        for (DataBean dataBean : dataBeans) {
            Array<DataBean> strings = chongwuMap.get(dataBean.getType());
            if (strings == null){
                strings = new Array<>();
                chongwuMap.put(dataBean.getType(),strings);
            }
            strings.add(dataBean);
        }
    }

    public static ArrayMap<String, Array<DataBean>> getChongwuMap() {
        return chongwuMap;
    }

    public static ArrayMap<String, Array<DataBean>> getWallMap() {
        return wallMap;
    }
}

package kw.manager.core.progress;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;

import java.text.DecimalFormat;
import java.util.Calendar;

import kw.manager.core.Constant;
import kw.manager.core.asset.Asset;

public class Progress extends Group {
    private Calendar currentTime;
    private Calendar morning8am;
    private Calendar night10pm;
    private long totalDurationInMillis;
    private float lastProcessNum = 0;
    private Image progressImg;
    private Image progressBg;
    public Progress(){
        // 开始time
        currentTime = Calendar.getInstance();
        // 设置早上八点和晚上十点的时间
        int startHour = 9;
        int startFen = 30;
        int starM = 0;
        int endHour = 17;
        int endFen = 30;
        int endM = 0;

        morning8am = (Calendar) currentTime.clone();
        morning8am.set(Calendar.HOUR_OF_DAY, startHour);
        morning8am.set(Calendar.MINUTE, startFen);
        morning8am.set(Calendar.SECOND, starM);

        night10pm = (Calendar) currentTime.clone();
        night10pm.set(Calendar.HOUR_OF_DAY,endHour);
        night10pm.set(Calendar.MINUTE, endFen);
        night10pm.set(Calendar.SECOND, endM);

        // 计算总时长
        totalDurationInMillis = night10pm.getTimeInMillis() - morning8am.getTimeInMillis();



        progressBg = new Image(new NinePatch(Asset.getAsset().getTexture("process.png"),20,20,20,20));
        progressBg.setWidth(Constant.GAMEWIDTH-200);
        setSize(Constant.GAMEWIDTH,progressBg.getHeight());
        progressBg.setPosition(getWidth()/2.0f,getHeight()/2.0f, Align.center);
        addActor(progressBg);
        progressBg.setColor(Color.BLACK);

        progressImg = new Image(new NinePatch(Asset.getAsset().getTexture("process.png"),20,20,20,20));
        progressImg.setWidth(Constant.GAMEWIDTH-200);
        progressImg.setPosition(getWidth()/2.0f,getHeight()/2.0f, Align.center);
        addActor(progressImg);
        progressImg.setColor(Color.WHITE);



    }

    @Override
    public void act(float delta) {
        super.act(delta);
        // 计算当前时间与早上八点的时长
        long currentDurationInMillis = System.currentTimeMillis() - morning8am.getTimeInMillis();

        // 计算进度百分比
        double progress = (double) currentDurationInMillis / totalDurationInMillis * 100;

        // 格式化百分比
        DecimalFormat df = new DecimalFormat("0.00");
        String progressPercentage = df.format(progress);
        try {
            Float integer = Float.valueOf(progressPercentage);
            System.out.println(integer);
            progressImg.setWidth(integer * progressBg.getWidth() * 0.01f);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

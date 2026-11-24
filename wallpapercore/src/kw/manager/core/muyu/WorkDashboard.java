package kw.manager.core.muyu;

import kw.manager.core.WallpaperAssets;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import java.nio.file.Path;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

/** Live data is represented by Labels; all artwork is loaded PNG resources. */
final class WorkDashboard {
    static final int WIDTH=460,HEIGHT=640;
    private volatile WorkSettings settings;
    private final WorkTracker tracker;
    private final WallpaperAssets assets;
    private final Map<String,Label> labels=new LinkedHashMap<>();
    private final Group group=new Group(){@Override public void act(float delta){update();super.act(delta);}};
    private LocalDateTime previewTime;
    private long shownSecond=Long.MIN_VALUE;
    private int quote;
    private Image panel,pet,bar;
    private String petType;
    WorkDashboard(boolean interactive,WallpaperAssets assets){this.assets=assets;settings=interactive?WorkSettings.load():new WorkSettings();Path file=interactive?MuyuProgress.defaultFile().resolveSibling("work-pauses.properties"):null;tracker=new WorkTracker(file);}
    WorkSettings settings(){return settings.copy();}
    boolean visible(){return settings.visible;}
    void configure(WorkSettings value)throws java.io.IOException{value.validate();value.save();settings=value.copy();shownSecond=Long.MIN_VALUE;}
    void preview(LocalDateTime time){previewTime=time;shownSecond=Long.MIN_VALUE;}
    private LocalDateTime now(){return previewTime==null?LocalDateTime.now():previewTime;}
    private void togglePause()throws java.io.IOException{tracker.toggle(now());shownSecond=Long.MIN_VALUE;}
    private void talk(){quote++;shownSecond=Long.MIN_VALUE;}
    private Image image(String path,float x,float y,float w,float h,int rgb){Image image=assets.image(path);image.setBounds(x,y,w,h);image.setColor(WallpaperAssets.color(rgb));group.addActor(image);return image;}
    private Label label(String name,String text,float x,float baseline,float size,int rgb){Label label=assets.label(text,size,rgb);label.pack();label.setPosition(x,HEIGHT-baseline-5);labels.put(name,label);group.addActor(label);return label;}
    private void button(String name,float x,float top,float w,float h,Runnable action){Button button=new Button(new Button.ButtonStyle());button.setName(name);button.setBounds(x,HEIGHT-top-h,w,h);button.addListener(new ClickListener(){@Override public void clicked(InputEvent event,float x,float y){action.run();}});group.addActor(button);}
    Group view(Runnable controls,Consumer<String> status){if(panel!=null)return group;group.setSize(WIDTH,HEIGHT);
        panel=image("ui/panel.png",0,0,WIDTH,HEIGHT,settings.backgroundColor);panel.setName("work-artwork");
        label("title",settings.title,28,45,22,settings.textColor);label("settings","设置",386,43,12,0x917f69);
        label("english","WORK & LITTLE JOYS",28,66,10,0x917f69);label("clock","",28,99,13,0x917f69);
        image("ui/white.png",28,HEIGHT-116,404,1,0xd3c4ae);
        label("state","",28,152,27,0xa4683c);label("countdown-label","",28,181,13,0x917f69);label("countdown","",28,219,31,settings.textColor);
        label("earned-caption","今日已赚 · 按设置估算",28,253,13,0x917f69);label("earned","",28,296,39,0xa4683c);
        label("speed","",28,320,13,0x917f69);label("hours","",28,347,13,settings.textColor);
        image("ui/rounded.png",28,HEIGHT-364,404,6,0xe1d5c2);bar=image("ui/rounded.png",28,HEIGHT-364,0,6,0xa4683c);
        image("ui/rounded.png",28,HEIGHT-417,404,37,0xecdfcc);label("pause","暂停今日计时",161,404,14,settings.textColor);
        for(int i=0;i<3;i++){int x=new int[]{28,165,302}[i],w=i==2?130:125;image("ui/rounded.png",x,HEIGHT-490,w,56,0xe7ddcd);}
        label("rest-title","休息日",40,454,11,0x917f69);label("rest","",40,478,18,settings.textColor);
        label("pay-title","发薪日",177,454,11,0x917f69);label("pay","",177,478,18,settings.textColor);
        label("month-title","本月估算",314,454,11,0x917f69);label("month","",314,478,18,settings.textColor);
        pet=image("muyu/pet-"+settings.pet+".png",30,HEIGHT-593,100,90,0xffffff);petType=settings.pet;
        label("pet-name","",137,527,15,settings.textColor);label("saying","",137,550,12,0x917f69);label("pet-tip","点点宠物，换一句悄悄话",137,574,11,0x917f69);
        label("salary","",28,600,11,0x917f69);label("month-tip","月估算按过去工作日满勤；未计请假和节假日。",28,618,10,0x917f69);
        button("work-pause",28,380,404,37,()->{try{togglePause();}catch(Exception e){status.accept("暂停记录保存失败");}});
        button("work-pet",25,503,407,87,this::talk);button("work-settings",350,16,88,44,controls);update();return group;
    }
    private void text(String name,String text){labels.get(name).setText(text);}
    private void update(){if(panel==null)return;LocalDateTime time=now();long second=time.toEpochSecond(ZoneOffset.UTC);if(shownSecond==second)return;shownSecond=second;
        WorkSettings s=settings;WorkSchedule.Snapshot v=tracker.snapshot(s,time);panel.setColor(WallpaperAssets.color(s.backgroundColor));
        for(String name:new String[]{"title","countdown","hours","pause","rest","pay","month","pet-name"})labels.get(name).setColor(WallpaperAssets.color(s.textColor));
        text("title",s.title);text("clock",time.format(DateTimeFormatter.ofPattern("yyyy.MM.dd  E  HH:mm:ss",Locale.CHINA)));
        text("state",v.state);text("countdown-label",v.countdownLabel);text("countdown",WorkSchedule.duration(v.countdown));
        text("earned",String.format(Locale.CHINA,"¥ %.4f",v.earned));text("speed",String.format(Locale.CHINA,"当前速度  ¥ %.4f / 秒",v.speed));
        text("hours",String.format(Locale.CHINA,"今日工时  %.2f / %.2f 小时",v.worked/3600d,v.planned/3600d));bar.setWidth((float)(404*v.worked/Math.max(1,v.planned)));
        text("pause",v.paused?"继续今日计时":"暂停今日计时");text("rest",v.restDays<0?"未设置":v.restDays==0?"就是今天":v.restDays+" 天");text("pay",v.paydayDays==0?"就是今天":v.paydayDays+" 天");text("month",String.format(Locale.CHINA,"¥ %.0f",v.monthly));
        if(!s.pet.equals(petType)){pet.setDrawable(new TextureRegionDrawable(new TextureRegion(assets.texture("muyu/pet-"+s.pet+".png"))));petType=s.pet;}
        text("pet-name",s.petName+"陪你");String[] sayings={"慢慢来，今天也在向前走。","敲一下木鱼，把烦恼放一放。","起来喝口水，眼睛也歇一歇。","赚一点，开心一点。","下班以后，记得留点时间给自己。"};
        text("saying",sayings[Math.floorMod(quote+time.toLocalTime().toSecondOfDay()/18,sayings.length)]);
        text("salary",String.format(Locale.CHINA,"日薪 ¥ %.2f  ·  时薪 ¥ %.2f%s",s.dailyIncome,s.dailyIncome/(Math.max(1,v.planned)/3600d),s.configured?"":"  ·  示例"));
    }
    void dispose(){} // Assets belong to the Screen's Asset scope.
}

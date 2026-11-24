package kw.manager.core.seasons;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import kw.manager.core.WallpaperAssets;
import kw.manager.core.city.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/** Loaded artwork, with clouds and precipitation animated as ordinary Scene2D Images. */
final class SeasonalScene extends Group {
    private final WallpaperAssets assets;
    private final SeasonsScreen screen;
    private final Image landscape,nightTint,cloudTint,orb,weatherPanel;
    private final Image[] clouds=new Image[8],stars=new Image[85],particles=new Image[260],ripples=new Image[28],fog=new Image[6];
    private final Label eyebrow,termName,verse,date,condition,source,footer;
    private int term=-1;
    private String particleType="";
    private double elapsed;
    SeasonalScene(WallpaperAssets assets,SeasonsScreen screen){this.assets=assets;this.screen=screen;setTouchable(Touchable.childrenOnly);
        landscape=add("seasons/term-0.png");landscape.setScaling(Scaling.fill);
        nightTint=add("ui/white.png");cloudTint=add("ui/white.png");
        for(int i=0;i<stars.length;i++)stars[i]=add("weather/star.png");
        orb=add("weather/sun.png");
        for(int i=0;i<clouds.length;i++)clouds[i]=add("weather/cloud.png");
        for(int i=0;i<fog.length;i++)fog[i]=add("weather/cloud.png");
        for(int i=0;i<particles.length;i++)particles[i]=add("weather/snow.png");
        for(int i=0;i<ripples.length;i++)ripples[i]=add("weather/ripple.png");
        weatherPanel=add("ui/rounded.png");weatherPanel.setColor(1,.98f,.91f,.75f);
        eyebrow=label("岁 时 · 二 十 四 节 气",17);termName=label("",68);verse=label("",18);date=label("",14);
        condition=label("",19);source=label("",12);footer=label("四季流转，万物有时",12);
    }
    private Image add(String path){Image image=assets.image(path);addActor(image);return image;}
    private Label label(String text,int size){Label label=assets.label(text,size,0x354c4b);addActor(label);return label;}
    private void text(Label label,float x,float baseline){label.pack();label.setPosition(x,getHeight()-baseline-5);}
    void resetTime(){elapsed=5;}
    @Override public void act(float delta){
        super.act(delta);elapsed+=Math.min(delta,.15f);
        setSize(getStage().getViewport().getWorldWidth(),getStage().getViewport().getWorldHeight());float w=getWidth(),h=getHeight();
        SeasonSettings settings=screen.settings();WeatherData data=screen.weather.data.preview(settings.weatherMode);ZonedDateTime now=Astronomy.time(settings);
        int current=settings.previewTerm<0?SolarTerms.current(now.toLocalDate(),now.getZone()):settings.previewTerm;
        if(current!=term){landscape.setDrawable(new TextureRegionDrawable(new TextureRegion(assets.texture("seasons/term-"+current+".png"))));term=current;}
        landscape.setSize(w,h);
        double altitude=Astronomy.sun(now,settings.latitude,settings.longitude,data).altitude;float night=(float)Math.max(0,Math.min(.72,(-altitude+3)/26));
        nightTint.setBounds(0,0,w,h);nightTint.setColor(.06f,.13f,.24f,night);
        boolean raining=!data.snow()&&(data.precipitation>.05||data.weatherCode>=51);float gray=data.cloudCover/100f;
        cloudTint.setBounds(0,0,w,h);cloudTint.setColor(.27f,.35f,.4f,gray>.8f?.2f:0);
        Random random=new Random(9);
        for(int i=0;i<stars.length;i++){Image star=stars[i];star.setVisible(altitude< -3);star.setBounds(random.nextInt((int)w),h-random.nextInt(420),i%7==0?9:5,i%7==0?9:5);star.setColor(1,1,.92f,(float)(night*.7*(.65+.35*Math.sin(elapsed*.6+i))));}
        orb.setDrawable(new TextureRegionDrawable(new TextureRegion(assets.texture(altitude< -3?"weather/moon.png":"weather/sun.png"))));orb.setBounds(w-418,h-310,256,256);orb.setVisible(gray<.75&&!raining&&!data.fog());
        int cloudCount=gray<=.1?0:gray>.8?8:gray>.4?5:2;
        for(int i=0;i<clouds.length;i++){Image cloud=clouds[i];cloud.setVisible(i<cloudCount);float scale=1.2f+i%3*.4f;
            cloud.setBounds((float)((i*357+elapsed*(3+i%3))%(w+460))-230,h-100-(i*89)%240,144*scale,55*scale);float value=(gray>.8?.68f:.98f)-night*.3f;cloud.setColor(value,value,value,gray>.8?.6f:.8f);}
        for(int i=0;i<fog.length;i++){fog[i].setVisible(data.fog());fog[i].setBounds(-240+(float)Math.sin(elapsed*.03+i)*100,h-450-i*60,2100,140);fog[i].setColor(.92f,.95f,.94f,.25f);}
        int season=SolarTerms.season(term);String type=data.snow()?"snow":raining?"rain":season==0?"petal":season==2?"leaf":"star";
        if(!type.equals(particleType)){TextureRegionDrawable drawable=new TextureRegionDrawable(new TextureRegion(assets.texture("weather/"+type+".png")));for(Image image:particles)image.setDrawable(drawable);particleType=type;}
        int count=data.snow()?155:raining?260:season==0||season==2?35:season==1?14:0;random=new Random(340);
        for(int i=0;i<particles.length;i++){Image image=particles[i];image.setVisible(i<count);float x=(float)((random.nextInt((int)w)+Math.sin(elapsed*.3+i)*45+elapsed*(data.snow()?9:raining?-125:17))%(w+20));if(x<0)x+=w+20;
            float speed=data.snow()?38:raining?680:season==0?20:32;float y=h-(float)((random.nextInt((int)h)+elapsed*speed)%h);
            float size=data.snow()?9+random.nextInt(12):raining?25:14;image.setBounds(x,y,raining?10:size,size);image.setRotation(raining?0:(float)Math.sin(elapsed*.8+i)*85);
            if(type.equals("petal"))image.setColor(1,.84f,.88f,.8f);else if(type.equals("leaf"))image.setColor(.85f,.48f,.19f,.8f);else image.setColor(.95f,.97f,.94f,raining?.5f:.8f);
            if(season==1&&!raining&&!data.snow())image.setY(180+random.nextInt(110)+(float)Math.sin(elapsed+i)*9);
        }
        for(int i=0;i<ripples.length;i++){float cycle=(float)((elapsed*.7+i*.19)%1);ripples[i].setBounds((i*97+(float)elapsed*8)%w,45+i*8,20+cycle*60,4+cycle*10);ripples[i].setColor(.96f,.97f,.88f,.3f*(1-cycle));}
        Color ink=night>.3?new Color(.94f,.93f,.84f,1):WallpaperAssets.color(0x354c4b);
        for(Label label:new Label[]{eyebrow,termName,verse,date,footer})label.setColor(ink);
        termName.setText(SolarTerms.NAMES[term]);verse.setText(SolarTerms.SEASONS[season]+" / "+SolarTerms.VERSES[term]);date.setText(now.format(DateTimeFormatter.ofPattern("yyyy.MM.dd EEEE"))+(settings.previewTerm>=0?" · 节气预览":""));
        String weather=data.fetchedAt>0||!settings.weatherMode.equals("real")?data.description()+" "+Math.round(data.temperature)+"°":"天气待更新";
        condition.setText(settings.city+" · "+weather);source.setText(settings.weatherMode.equals("real")?screen.weather.status:"天气效果预览");
        text(eyebrow,86,76);text(termName,82,166);text(verse,87,207);text(date,88,259);footer.pack();footer.setPosition(88,45);
        weatherPanel.setBounds(w-430,45,342,96);condition.pack();condition.setPosition(w-407,100);source.pack();source.setPosition(w-407,72);
        for(Actor actor:new Actor[]{eyebrow,termName,verse,date,footer,condition,source,weatherPanel})actor.setVisible(settings.showInfo);
    }
}

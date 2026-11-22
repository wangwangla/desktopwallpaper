package kw.manager.core.muyu;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import java.awt.Color;
import java.nio.*;
import java.nio.file.*;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.*;

public final class MuyuScreen extends ScreenAdapter {
    public interface DesktopActions {void wallpaper();void preview();void quit();}
    public final MuyuProgress progress;
    private final ExtendViewport viewport=new ExtendViewport(1200,800);
    private final SpriteBatch batch=new SpriteBatch();
    private final Texture background=MuyuArtwork.background(),fish=MuyuArtwork.fish(),stick=MuyuArtwork.stick();
    private final MuyuStroke stroke=new MuyuStroke();
    private final WorkDashboard work;
    private final Texture title=MuyuArtwork.text("一 念",26,new Color(79,61,43),true),caption=MuyuArtwork.text("累 计 功 德",16,new Color(148,130,108),false),
        hint=MuyuArtwork.text("点击木鱼，或轻按空格",16,new Color(148,130,108),false),footer=MuyuArtwork.text("一声木鱼，片刻清静。",16,new Color(148,130,108),false),
        desktopHint=MuyuArtwork.text("轻点木鱼 · 把烦恼放一放",16,new Color(148,130,108),false),
        merit=MuyuArtwork.text("功德 +1",28,new Color(157,107,69),true);
    private Texture count,today,sessionText;private long shownTotal=-1,shownToday=-1,shownSession=-1;private volatile long session;
    private final List<Float> floats=new ArrayList<>();
    private float hitAnimation,automaticElapsed,saveDelay=-1;
    private boolean automatic,wallpaper;private Sound sound;private MuyuController controller;private long lastHit;
    public volatile String status="已保存到本机";
    public MuyuScreen(DesktopActions actions,boolean interactive) {
        work=new WorkDashboard(interactive);
        progress=new MuyuProgress(interactive?MuyuProgress.defaultFile():Paths.get(System.getProperty("java.io.tmpdir"),"muyu-preview-unused.properties"));
        if(interactive){createSound();javax.swing.SwingUtilities.invokeLater(()->{controller=new MuyuController(this,actions);controller.show();});}
        Gdx.input.setInputProcessor(new InputAdapter(){@Override public boolean touchDown(int x,int y,int pointer,int button){if(!wallpaper&&button==Input.Buttons.LEFT)click(x,y);return false;}
            @Override public boolean keyDown(int key){if(!wallpaper&&key==Input.Keys.SPACE){hit();return true;}if(key==Input.Keys.F1&&controller!=null){javax.swing.SwingUtilities.invokeLater(controller::show);return true;}return false;}});
    }
    public void setWallpaper(boolean value){wallpaper=value;status=value?"桌面壁纸 · 点击木鱼或右侧卡片":"窗口预览 · 点击木鱼或按空格";}
    public WorkSettings workSettings(){return work.settings();}
    public boolean configureWork(WorkSettings value){try{work.configure(value);status="作息设置已保存到本机";return true;}catch(Exception e){status="作息设置保存失败";System.err.println(e);return false;}}
    public void previewWorkTime(java.time.LocalDateTime time){work.preview(time);}
    private float woodCenter(){return viewport.getWorldWidth()/2-(work.visible()?250:0);}
    private float verticalOffset(){return (viewport.getWorldHeight()-800)/2;}
    private float cardX(){return viewport.getWorldWidth()/2+90;}
    private float cardY(){return (viewport.getWorldHeight()-WorkDashboard.HEIGHT)/2;}
    public void click(int x,int y){Vector2 p=viewport.unproject(new Vector2(x,y));
        if(work.visible()){float localX=p.x-cardX(),top=WorkDashboard.HEIGHT-(p.y-cardY());
            if(localX>=28&&localX<=432&&top>=380&&top<=417){try{work.togglePause();}catch(Exception e){status="暂停记录保存失败";}return;}
            if(localX>=25&&localX<=432&&top>=503&&top<=590){work.talk();return;}
            if(localX>=350&&localX<=438&&top>=16&&top<=60&&controller!=null){javax.swing.SwingUtilities.invokeLater(controller::showWork);return;}
        }
        float dx=(p.x-woodCenter())/180,dy=(p.y-407-verticalOffset())/128;if(dx*dx+dy*dy<=1)hit();}
    public void hit(){long now=System.nanoTime();if(now-lastHit<75_000_000)return;lastHit=now;stroke.request();}
    private void contact(){progress.hit(LocalDate.now());session++;hitAnimation=.14f;floats.add(0f);if(saveDelay<0)saveDelay=.5f;
        if(progress.sound&&sound!=null)sound.play(.7f,.96f+(float)Math.random()*.08f,0);}
    public boolean automatic(){return automatic;}
    public void automatic(boolean value){automatic=value;automaticElapsed=0;}
    public long session(){return session;}
    public void sound(boolean value){progress.sound=value;saveDelay=.2f;}
    public void interval(float value){progress.interval=value;saveDelay=.2f;}
    private void save(){try{progress.save();}catch(Exception error){status="记录保存失败，当前计数仍可继续";System.err.println(error);}}
    private void createSound(){try{
        Path file=MuyuProgress.defaultFile().resolveSibling("wood.wav");Files.createDirectories(file.getParent());int rate=22050,n=rate/3;
        ByteBuffer wav=ByteBuffer.allocate(44+n*2).order(ByteOrder.LITTLE_ENDIAN);wav.put("RIFF".getBytes("US-ASCII")).putInt(36+n*2).put("WAVEfmt ".getBytes("US-ASCII")).putInt(16).putShort((short)1).putShort((short)1).putInt(rate).putInt(rate*2).putShort((short)2).putShort((short)16).put("data".getBytes("US-ASCII")).putInt(n*2);
        Random random=new Random(9);for(int i=0;i<n;i++){double t=i/(double)rate,attack=Math.min(1,t/.002);double s=0;double[] f={510,830,1240,1870},amp={.6,.25,.13,.06},decay={.045,.027,.013,.008};
            for(int j=0;j<f.length;j++)s+=amp[j]*Math.sin(2*Math.PI*f[j]*(t-.025*t*t))*Math.exp(-t/decay[j]);s+=.14*(random.nextDouble()*2-1)*Math.exp(-t/.003);wav.putShort((short)(Math.max(-1,Math.min(1,s*attack)) * 26000));}
        Files.write(file,wav.array());sound=Gdx.audio.newSound(new FileHandle(file.toFile()));
    }catch(Exception error){status="音效暂不可用，仍可敲击计数";System.err.println(error);}}
    private void updateLabels(){if(shownTotal!=progress.total){if(count!=null)count.dispose();count=MuyuArtwork.text(String.format(Locale.CHINA,"%,d",progress.total),88,new Color(67,54,42),true);shownTotal=progress.total;}
        if(shownToday!=progress.today){if(today!=null)today.dispose();today=MuyuArtwork.text("今日 "+progress.today+" 次 · 慢慢来，不着急",17,new Color(148,130,108),false);shownToday=progress.today;}
        if(shownSession!=session){if(sessionText!=null)sessionText.dispose();sessionText=MuyuArtwork.text("本次已敲 "+session+" 下",16,new Color(122,103,80),false);shownSession=session;}}
    private void centered(Texture t,float y){float scale=Math.min(1,(work.visible()?590f:640f)/t.getWidth());batch.draw(t,woodCenter()-t.getWidth()*scale/2,y+verticalOffset(),t.getWidth()*scale,t.getHeight()*scale);}
    @Override public void render(float delta){delta=Math.min(delta,.1f);progress.rollDay(LocalDate.now());
        if(automatic){automaticElapsed+=delta;if(automaticElapsed>=progress.interval){automaticElapsed%=progress.interval;hit();}}
        hitAnimation=Math.max(0,hitAnimation-delta);
        int contacts=stroke.update(delta);for(int i=0;i<contacts;i++)contact();
        if(saveDelay>=0&&(saveDelay-=delta)<=0){save();saveDelay=-1;}
        updateLabels();viewport.apply();
        Gdx.gl.glClearColor(.96f,.94f,.9f,1);Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);batch.setProjectionMatrix(viewport.getCamera().combined);batch.begin();
        batch.setColor(1,1,1,1);batch.draw(background,0,0,viewport.getWorldWidth(),viewport.getWorldHeight());batch.draw(title,45,viewport.getWorldHeight()-70);centered(caption,674);centered(count,567);centered(today,547);
        float reaction=hitAnimation/.14f,scale=1-.025f*reaction,w=460*scale,h=337*scale;
        batch.draw(fish,woodCenter()-w/2,240+verticalOffset()+(337-h)/2-3*reaction,w,h);
        float headX=woodCenter()+stroke.headX(),headY=stroke.headY()+verticalOffset();
        batch.draw(stick,headX-27,headY-30,27,30,150,60,1,1,stroke.rotation(),0,0,stick.getWidth(),stick.getHeight(),false,false);
        for(int i=floats.size()-1;i>=0;i--){float age=floats.get(i)+delta;if(age>1){floats.remove(i);continue;}floats.set(i,age);batch.setColor(1,1,1,1-age);batch.draw(merit,woodCenter()-185-merit.getWidth()/2f,475+verticalOffset()+age*90);}
        batch.setColor(1,1,1,1);centered(wallpaper?desktopHint:hint,223);centered(sessionText,129);centered(footer,87);
        if(work.visible())batch.draw(work.image(),cardX(),cardY(),WorkDashboard.WIDTH,WorkDashboard.HEIGHT);
        batch.end();}
    @Override public void resize(int width,int height){viewport.update(width,height,true);}
    @Override public void dispose(){automatic=false;save();if(sound!=null)sound.dispose();batch.dispose();work.dispose();for(Texture t:new Texture[]{background,fish,stick,title,caption,hint,desktopHint,footer,merit,count,today,sessionText})if(t!=null)t.dispose();if(controller!=null)controller.dispose();Gdx.input.setInputProcessor(null);}
}

package kw.manager.core.muyu;

import com.badlogic.gdx.graphics.Texture;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.time.*;
import java.time.format.DateTimeFormatter;

final class WorkDashboard {
    static final int WIDTH=460, HEIGHT=640;
    private volatile WorkSettings settings;
    private final WorkTracker tracker;
    private Texture image;
    private LocalDateTime previewTime;
    private long shownSecond=Long.MIN_VALUE;
    private int quote;
    WorkDashboard(boolean interactive){settings=interactive?WorkSettings.load():new WorkSettings();Path file=interactive?MuyuProgress.defaultFile().resolveSibling("work-pauses.properties"):null;tracker=new WorkTracker(file);}
    WorkSettings settings(){return settings.copy();}
    boolean visible(){return settings.visible;}
    void configure(WorkSettings value) throws java.io.IOException {value.validate();value.save();settings=value.copy();shownSecond=Long.MIN_VALUE;}
    void preview(LocalDateTime time){previewTime=time;shownSecond=Long.MIN_VALUE;}
    LocalDateTime now(){return previewTime==null?LocalDateTime.now():previewTime;}
    void togglePause() throws java.io.IOException {tracker.toggle(now());shownSecond=Long.MIN_VALUE;}
    void talk(){quote++;shownSecond=Long.MIN_VALUE;}
    Texture image(){LocalDateTime now=now();long second=now.toEpochSecond(ZoneOffset.UTC);if(image==null||shownSecond!=second){shownSecond=second;Texture next=draw(settings,tracker.snapshot(settings,now),now);if(image!=null)image.dispose();image=next;}return image;}
    void dispose(){if(image!=null)image.dispose();}
    private void text(Graphics2D g,String text,int x,int y,int size,Color color,boolean bold){g.setFont(new Font("Microsoft YaHei",bold?Font.BOLD:Font.PLAIN,size));g.setColor(color);g.drawString(text,x,y);}
    private Texture draw(WorkSettings s,WorkSchedule.Snapshot v,LocalDateTime now){BufferedImage b=new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_ARGB);Graphics2D g=MuyuArtwork.graphics(b);Color ink=new Color(s.textColor),muted=new Color(145,127,105),accent=new Color(164,104,60);
        g.setColor(new Color(88,63,34,8));for(int i=0;i<8;i++)g.fillRoundRect(5-i/2,10-i/2,450+i,620+i,30,30);
        g.setColor(new Color(s.backgroundColor));g.fillRoundRect(3,3,454,625,28,28);g.setColor(new Color(195,174,143,100));g.drawRoundRect(3,3,453,624,28,28);
        text(g,s.title,28,45,Math.min(22,330/Math.max(1,s.title.length())),ink,true);text(g,"设置",386,43,12,muted,false);text(g,"WORK & LITTLE JOYS",28,66,10,muted,false);
        text(g,now.format(DateTimeFormatter.ofPattern("yyyy.MM.dd  E  HH:mm:ss",java.util.Locale.CHINA)),28,99,13,muted,false);
        g.setColor(new Color(211,196,174,120));g.drawLine(28,116,432,116);
        text(g,v.state,28,152,27,accent,true);text(g,v.countdownLabel,28,181,13,muted,false);
        text(g,WorkSchedule.duration(v.countdown),28,219,31,ink,true);
        text(g,"今日已赚 · 按设置估算",28,253,13,muted,false);text(g,String.format(java.util.Locale.CHINA,"¥ %.4f",v.earned),28,296,39,accent,true);
        text(g,String.format(java.util.Locale.CHINA,"当前速度  ¥ %.4f / 秒",v.speed),28,320,13,muted,false);
        text(g,String.format(java.util.Locale.CHINA,"今日工时  %.2f / %.2f 小时",v.worked/3600d,v.planned/3600d),28,347,13,ink,false);
        g.setColor(new Color(225,213,194));g.fillRoundRect(28,358,404,6,6,6);g.setColor(accent);g.fillRoundRect(28,358,(int)(404*v.worked/v.planned),6,6,6);
        g.setColor(new Color(236,223,204));g.fillRoundRect(28,380,404,37,10,10);text(g,v.paused?"继续今日计时":"暂停今日计时",161,404,14,ink,true);
        box(g,28,434,125,"休息日",v.restDays<0?"未设置":v.restDays==0?"就是今天":v.restDays+" 天",ink,muted);
        box(g,165,434,125,"发薪日",v.paydayDays==0?"就是今天":v.paydayDays+" 天",ink,muted);
        box(g,302,434,130,"本月估算",String.format(java.util.Locale.CHINA,"¥ %.0f",v.monthly),ink,muted);
        pet(g,s.pet,30,503);text(g,s.petName+"陪你",137,527,15,ink,true);
        String[] sayings={"慢慢来，今天也在向前走。","敲一下木鱼，把烦恼放一放。","起来喝口水，眼睛也歇一歇。","赚一点，开心一点。","下班以后，记得留点时间给自己。"};
        String saying=sayings[Math.floorMod(quote+(int)(now.toLocalTime().toSecondOfDay()/18),sayings.length)];text(g,saying,137,550,12,muted,false);text(g,"点点宠物，换一句悄悄话",137,574,11,muted,false);
        text(g,String.format(java.util.Locale.CHINA,"日薪 ¥ %.2f  ·  时薪 ¥ %.2f%s",s.dailyIncome,s.dailyIncome/(v.planned/3600d),s.configured?"":"  ·  示例"),28,600,11,muted,false);
        text(g,"月估算按过去工作日满勤；未计请假和节假日。",28,618,10,muted,false);
        g.dispose();return MuyuArtwork.texture(b);
    }
    private void box(Graphics2D g,int x,int y,int width,String title,String value,Color ink,Color muted){g.setColor(new Color(231,221,205,120));g.fillRoundRect(x,y,width,56,11,11);text(g,title,x+12,y+20,11,muted,false);text(g,value,x+12,y+44,18,ink,true);}
    private void pet(Graphics2D g,String type,int x,int y){Graphics2D p=(Graphics2D)g.create();p.translate(x,y);p.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);Color fur="panda".equals(type)?new Color(244,242,229):"hamster".equals(type)?new Color(205,180,145):new Color(217,163,100);
        p.setColor(new Color(227,216,197));p.fillOval(0,0,90,85);p.setColor(fur);p.fillOval(17,31,57,48);
        if("cat".equals(type)){Path2D ears=new Path2D.Float();ears.moveTo(19,26);ears.lineTo(17,4);ears.lineTo(37,17);ears.lineTo(59,17);ears.lineTo(79,4);ears.lineTo(76,29);ears.closePath();p.fill(ears);}else{p.setColor("panda".equals(type)?new Color(75,66,54):fur);p.fillOval(13,8,26,26);p.fillOval(59,8,26,26);}
        p.setColor(fur);p.fillOval(15,13,64,52);if("panda".equals(type)){p.setColor(new Color(79,70,59));p.fillOval(26,28,15,20);p.fillOval(54,28,15,20);}
        p.setColor(new Color(79,60,44));p.fillOval(32,34,5,7);p.fillOval(59,34,5,7);p.fillOval(45,45,6,4);p.drawArc(40,45,16,12,190,150);p.setColor(new Color(226,157,139,140));p.fillOval(24,44,10,6);p.fillOval(62,44,10,6);
        if("cat".equals(type)){p.setColor(new Color(174,114,62));p.setStroke(new BasicStroke(3));p.drawLine(40,17,42,25);p.drawLine(49,15,49,24);p.drawLine(58,17,56,25);}p.dispose();
    }
}

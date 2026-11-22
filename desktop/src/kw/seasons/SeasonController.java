package kw.seasons;

import com.badlogic.gdx.Gdx;
import kw.manager.core.city.*;
import kw.manager.core.seasons.SolarTerms;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.*;

/** Controls live outside the desktop layer and remain accessible from the Windows tray. */
public final class SeasonController {
    private final JFrame frame=new JFrame("四季节气 · 壁纸控制器");
    private final Supplier<SeasonSettings> settings;
    private final Consumer<SeasonSettings> configure;
    private final WeatherService weather;
    private final JLabel summary=new JLabel(),source=new JLabel();
    private final Timer timer;
    private TrayIcon tray;
    private final Runnable quit;
    private final String[] modes={"real","clear","cloudy","overcast","rain","snow","fog"};
    public SeasonController(Supplier<SeasonSettings> settings,Consumer<SeasonSettings> configure,WeatherService weather,Runnable attach,Runnable detach,Runnable quit) {
        this(settings,configure,weather,attach,detach,quit,true);
    }
    public SeasonController(Supplier<SeasonSettings> settings,Consumer<SeasonSettings> configure,WeatherService weather,Runnable attach,Runnable detach,Runnable quit,boolean showControls) {
        this.settings=settings;this.configure=configure;this.weather=weather;this.quit=quit;
        JPanel panel=new JPanel();panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));panel.setBorder(new EmptyBorder(24,24,24,24));
        JLabel title=new JLabel("四季流转，万物有时");panel.add(title);panel.add(Box.createVerticalStrut(18));
        panel.add(summary);panel.add(Box.createVerticalStrut(8));panel.add(source);panel.add(Box.createVerticalStrut(20));
        panel.add(new JLabel("节气与季节"));String[] terms=new String[25];terms[0]="自动 · 跟随今日节气";
        for(int i=0;i<24;i++)terms[i+1]=SolarTerms.SEASONS[SolarTerms.season(i)]+" · "+SolarTerms.NAMES[i];
        JComboBox<String> term=new JComboBox<>(terms);term.setSelectedIndex(settings.get().previewTerm+1);add(panel,term);
        term.addActionListener(e->{SeasonSettings s=settings.get().copy();s.previewTerm=term.getSelectedIndex()-1;apply(s);});
        panel.add(new JLabel("天气效果"));JComboBox<String> mode=new JComboBox<>(new String[]{"实时 · 跟随所在地天气","晴天 · 无云","多云","阴天","下雨","下雪","雾"});
        for(int i=0;i<modes.length;i++)if(modes[i].equals(settings.get().weatherMode))mode.setSelectedIndex(i);add(panel,mode);
        mode.addActionListener(e->{SeasonSettings s=settings.get().copy();s.weatherMode=modes[mode.getSelectedIndex()];apply(s);});
        panel.add(new JLabel("昼夜"));JComboBox<String> time=new JComboBox<>(new String[]{"自动 · 所在地时间","白天预览","夜晚预览"});
        time.setSelectedIndex(settings.get().previewMinutes<0?0:settings.get().previewMinutes>=1080?2:1);add(panel,time);
        time.addActionListener(e->{SeasonSettings s=settings.get().copy();s.previewMinutes=time.getSelectedIndex()==0?-1:time.getSelectedIndex()==1?720:1320;apply(s);});
        panel.add(new JLabel("天气地点"));JTextField city=new JTextField(settings.get().city);JButton search=new JButton("应用城市");
        JPanel row=new JPanel(new BorderLayout(8,0));row.add(city);row.add(search,BorderLayout.EAST);add(panel,row);
        search.addActionListener(e->{String query=city.getText().trim();if(query.isEmpty())return;search.setEnabled(false);search.setText("查找中…");
            CompletableFuture.supplyAsync(()->{try{return WeatherService.search(query);}catch(Exception error){throw new RuntimeException(error);}})
                .whenComplete((places,error)->SwingUtilities.invokeLater(()->{
                    search.setEnabled(true);search.setText("应用城市");
                    if(error!=null||places==null||places.isEmpty()){JOptionPane.showMessageDialog(frame,"城市查询失败，请检查网络或尝试英文名。也可直接填写经纬度。" );return;}
                    WeatherService.Place place=places.get(0);
                    if(places.size()>1){Object chosen=JOptionPane.showInputDialog(frame,"请选择地点","天气地点",JOptionPane.QUESTION_MESSAGE,null,places.toArray(),place);if(chosen==null)return;place=(WeatherService.Place)chosen;}
                    SeasonSettings s=settings.get().copy();s.city=place.name;s.latitude=place.latitude;s.longitude=place.longitude;s.timezone=place.timezone;city.setText(s.city);apply(s);
                }));});
        JButton coordinates=new JButton("直接填写经纬度 / 时区");panel.add(coordinates);panel.add(Box.createVerticalStrut(14));
        coordinates.addActionListener(e->coordinates());
        JCheckBox info=new JCheckBox("显示节气、日期与天气信息",settings.get().showInfo);panel.add(info);panel.add(Box.createVerticalStrut(18));
        info.addActionListener(e->{SeasonSettings s=settings.get().copy();s.showInfo=info.isSelected();apply(s);});
        JPanel buttons=new JPanel(new GridLayout(1,2,8,0));JButton wall=new JButton("设为桌面壁纸"),preview=new JButton("窗口预览");buttons.add(wall);buttons.add(preview);add(panel,buttons);
        wall.addActionListener(e->Gdx.app.postRunnable(attach));preview.addActionListener(e->Gdx.app.postRunnable(detach));
        JButton refresh=new JButton("刷新实时天气");panel.add(refresh);refresh.addActionListener(e->weather.refreshSoon());
        panel.add(Box.createVerticalStrut(12));JLabel credit=new JLabel("天气：Open-Meteo · 每 15 分钟更新");panel.add(credit);
        font(panel,new Font("Microsoft YaHei",Font.PLAIN,13));title.setFont(new Font("Microsoft YaHei",Font.BOLD,21));
        frame.setContentPane(panel);frame.setSize(460,680);frame.setResizable(false);frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);frame.setLocationRelativeTo(null);
        timer=new Timer(1000,e->update());timer.start();update();tray();if(showControls)show();
    }
    private void coordinates(){SeasonSettings s=settings.get();JTextField name=new JTextField(s.city),lat=new JTextField(""+s.latitude),lon=new JTextField(""+s.longitude),zone=new JTextField(s.timezone);
        JPanel p=new JPanel(new GridLayout(4,2,8,8));p.add(new JLabel("地点名称"));p.add(name);p.add(new JLabel("纬度 (-90~90)"));p.add(lat);p.add(new JLabel("经度 (-180~180)"));p.add(lon);p.add(new JLabel("时区"));p.add(zone);
        if(JOptionPane.showConfirmDialog(frame,p,"自定义天气地点",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        try{SeasonSettings c=s.copy();c.city=name.getText().trim();c.latitude=Double.parseDouble(lat.getText());c.longitude=Double.parseDouble(lon.getText());c.timezone=zone.getText().trim();ZoneId.of(c.timezone);
            if(c.city.isEmpty()||!Double.isFinite(c.latitude)||Math.abs(c.latitude)>90||!Double.isFinite(c.longitude)||Math.abs(c.longitude)>180)throw new IllegalArgumentException();apply(c);
        }catch(Exception e){JOptionPane.showMessageDialog(frame,"请填写有效经纬度和时区，例如 Asia/Shanghai。");}}
    private void apply(SeasonSettings s){Gdx.app.postRunnable(()->configure.accept(s));}
    private static void add(JPanel p,JComponent c){p.add(Box.createVerticalStrut(6));c.setMaximumSize(new Dimension(410,34));c.setAlignmentX(Component.LEFT_ALIGNMENT);p.add(c);p.add(Box.createVerticalStrut(14));}
    private static void font(Container p,Font f){for(Component c:p.getComponents()){c.setFont(f);if(c instanceof Container)font((Container)c,f);}}
    private void update(){SeasonSettings s=settings.get();ZonedDateTime time=Astronomy.time(s);int term=s.previewTerm<0?SolarTerms.current(time.toLocalDate(),time.getZone()):s.previewTerm;
        summary.setText(time.toLocalDate()+" · "+SolarTerms.NAMES[term]+" · "+SolarTerms.SEASONS[SolarTerms.season(term)]+"季");
        source.setText("real".equals(s.weatherMode)?weather.status:"正在预览天气效果");}
    private void tray(){if(!SystemTray.isSupported())return;BufferedImage image=new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB);Graphics2D g=image.createGraphics();
        g.setColor(new Color(106,151,108));g.fillRoundRect(0,0,32,32,8,8);g.setColor(new Color(249,227,182));g.fillOval(9,5,14,15);g.fillRect(15,16,3,11);g.dispose();
        PopupMenu menu=new PopupMenu();MenuItem open=new MenuItem("打开四季控制器"),exit=new MenuItem("退出四季壁纸");menu.add(open);menu.addSeparator();menu.add(exit);
        open.addActionListener(e->SwingUtilities.invokeLater(this::show));exit.addActionListener(e->Gdx.app.postRunnable(quit));
        tray=new TrayIcon(image,"四季节气壁纸",menu);tray.setImageAutoSize(true);tray.addActionListener(e->SwingUtilities.invokeLater(this::show));
        try{SystemTray.getSystemTray().add(tray);}catch(AWTException e){tray=null;}}
    public void show(){frame.setVisible(true);frame.toFront();}
    public void dispose(){SwingUtilities.invokeLater(()->{timer.stop();frame.dispose();if(tray!=null)SystemTray.getSystemTray().remove(tray);});}
}

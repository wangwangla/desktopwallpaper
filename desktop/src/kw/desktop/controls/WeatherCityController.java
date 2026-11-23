package kw.desktop.controls;

import kw.manager.core.city.*;

import com.badlogic.gdx.Gdx;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Native Chinese controls remain available from the tray when the scene is behind desktop icons.
 */
public final class WeatherCityController {
    private final WeatherCityScreen scene;
    private final kw.manager.core.WallpaperPlatform actions;
    private final JFrame frame = new JFrame("天气城市 · 控制器");
    private final JTextField cityName = new JTextField();
    private final JLabel condition = new JLabel(), clock = new JLabel(), sun = new JLabel(), source = new JLabel();
    private final JComboBox<String> mode = new JComboBox<>(new String[]{"实时天气", "晴天预览", "多云预览", "阴天预览", "雨天预览", "雾天预览", "雪天预览"});
    private final String[] modes = {"real", "clear", "cloudy", "overcast", "rain", "fog", "snow"};
    private final JCheckBox realTime = new JCheckBox("跟随所在地的实际时间");
    private final JSlider time = new JSlider(0, 1439, 720);
    private final Timer timer;
    private TrayIcon tray;
    private boolean initializing = true;

    public WeatherCityController(WeatherCityScreen scene, kw.manager.core.WallpaperPlatform actions) {
        this.scene = scene;
        this.actions = actions;
        Font font = new Font("Microsoft YaHei", Font.PLAIN, 14);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel title = new JLabel("随天气与时间变化的城市");
        title.setFont(font.deriveFont(Font.BOLD, 18));
        panel.add(title);
        panel.add(Box.createVerticalStrut(18));
        panel.add(new JLabel("天气地点"));
        panel.add(Box.createVerticalStrut(6));
        JPanel search = new JPanel(new BorderLayout(8, 0));
        cityName.setText(scene.settings().city);
        search.add(cityName);
        JButton apply = new JButton("应用城市");
        search.add(apply, BorderLayout.EAST);
        search.setMaximumSize(new Dimension(450, 34));
        panel.add(search);
        apply.addActionListener(event -> search(apply));
        panel.add(Box.createVerticalStrut(15));
        panel.add(condition);
        panel.add(Box.createVerticalStrut(6));
        panel.add(sun);
        panel.add(Box.createVerticalStrut(18));
        panel.add(new JLabel("天气效果"));
        panel.add(Box.createVerticalStrut(6));
        mode.setMaximumSize(new Dimension(450, 32));
        panel.add(mode);
        WeatherSettings settings = scene.settings();
        for (int i = 0; i < modes.length; i++)
            if (modes[i].equals(settings.weatherMode)) mode.setSelectedIndex(i);
        panel.add(Box.createVerticalStrut(18));
        realTime.setSelected(settings.previewMinutes < 0);
        panel.add(realTime);
        time.setValue(settings.previewMinutes < 0 ? 720 : settings.previewMinutes);
        time.setEnabled(!realTime.isSelected());
        time.setMajorTickSpacing(360);
        time.setPaintTicks(true);
        time.setMaximumSize(new Dimension(450, 45));
        panel.add(time);
        panel.add(clock);
        mode.addActionListener(event -> apply());
        realTime.addActionListener(event -> {
            time.setEnabled(!realTime.isSelected());
            apply();
        });
        time.addChangeListener(event -> {
            if (!time.getValueIsAdjusting()) apply();
        });
        panel.add(Box.createVerticalStrut(20));
        JPanel buttons = new JPanel(new GridLayout(1, 2, 8, 0));
        JButton wallpaper = new JButton("设为桌面壁纸"), preview = new JButton("窗口预览");
        buttons.add(wallpaper);
        buttons.add(preview);
        buttons.setMaximumSize(new Dimension(450, 36));
        panel.add(buttons);
        wallpaper.addActionListener(event -> Gdx.app.postRunnable(actions::wallpaper));
        preview.addActionListener(event -> Gdx.app.postRunnable(actions::preview));
        panel.add(Box.createVerticalStrut(18));
        panel.add(source);
        panel.add(Box.createVerticalStrut(5));
        JLabel credit = new JLabel("天气数据：Open-Meteo · 月相按日期近似计算");
        credit.setForeground(new Color(105, 112, 121));
        panel.add(credit);
        panel.add(Box.createVerticalStrut(12));
        JButton refresh = new JButton("刷新天气");
        panel.add(refresh);
        refresh.addActionListener(event -> scene.weather.refreshSoon());
        setFont(panel, font);
        title.setFont(font.deriveFont(Font.BOLD, 18));
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setSize(460, 620);
        frame.setMinimumSize(new Dimension(440, 590));
        Dimension desktop = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(Math.max(20, desktop.width - 480), Math.max(40, (desktop.height - 620) / 2));
        timer = new Timer(1000, event -> update());
        timer.start();
        initializing = false;
        update();
        installTray();
    }

    private void apply() {
        if (initializing) return;
        WeatherSettings settings = scene.settings();
        settings.weatherMode = modes[mode.getSelectedIndex()];
        settings.previewMinutes = realTime.isSelected() ? -1 : time.getValue();
        Gdx.app.postRunnable(() -> scene.configure(settings));
    }

    private void search(JButton button) {
        String name = cityName.getText().trim();
        if (name.isEmpty()) return;
        button.setEnabled(false);
        button.setText("查找中…");
        CompletableFuture.supplyAsync(() -> {
            try {
                return WeatherService.search(name);
            } catch (Exception error) {
                throw new RuntimeException(error);
            }
        }).whenComplete((places, error) -> SwingUtilities.invokeLater(() -> {
            button.setEnabled(true);
            button.setText("应用城市");
            if (error != null || places == null || places.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "暂时找不到这个地点，请检查网络或尝试城市的英文名。", "查找城市", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            WeatherService.Place place = places.get(0);
            if (places.size() > 1) {
                Object choice = JOptionPane.showInputDialog(frame, "请选择要跟随天气的地点：", "选择城市", JOptionPane.QUESTION_MESSAGE, null, places.toArray(), place);
                if (choice == null) return;
                place = (WeatherService.Place) choice;
            }
            WeatherSettings settings = scene.settings();
            settings.city = place.name;
            settings.latitude = place.latitude;
            settings.longitude = place.longitude;
            settings.timezone = place.timezone;
            cityName.setText(place.name);
            Gdx.app.postRunnable(() -> scene.configure(settings));
        }));
    }

    private void update() {
        condition.setText(scene.weatherSummary());
        clock.setText(scene.clockSummary());
        sun.setText(scene.sunSummary());
        source.setText("real".equals(scene.settings().weatherMode) ? scene.weather.status : "当前为天气效果预览");
    }

    private void installTray() {
        if (!SystemTray.isSupported()) return;
        BufferedImage icon = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = icon.createGraphics();
        g.setColor(new Color(32, 57, 85));
        g.fillRoundRect(0, 0, 32, 32, 8, 8);
        g.setColor(new Color(244, 190, 74));
        g.fillOval(4, 3, 11, 11);
        g.setColor(new Color(218, 228, 238));
        g.fillRect(6, 18, 5, 10);
        g.fillRect(14, 12, 6, 16);
        g.fillRect(23, 20, 4, 8);
        g.dispose();
        PopupMenu menu = new PopupMenu();
        MenuItem controls = new MenuItem("打开控制器"), quit = new MenuItem("退出天气城市");
        menu.add(controls);
        menu.addSeparator();
        menu.add(quit);
        controls.addActionListener(event -> SwingUtilities.invokeLater(this::show));
        quit.addActionListener(event -> Gdx.app.postRunnable(actions::quit));
        tray = new TrayIcon(icon, "天气城市", menu);
        tray.setImageAutoSize(true);
        tray.addActionListener(event -> SwingUtilities.invokeLater(this::show));
        try {
            SystemTray.getSystemTray().add(tray);
        } catch (AWTException ignored) {
            tray = null;
        }
    }

    private static void setFont(Container parent, Font font) {
        for (Component child : parent.getComponents()) {
            child.setFont(font);
            if (child instanceof Container) setFont((Container) child, font);
        }
    }

    public void show() {
        frame.setVisible(true);
        frame.toFront();
    }

    public void dispose() {
        SwingUtilities.invokeLater(() -> {
            timer.stop();
            frame.dispose();
            if (tray != null) SystemTray.getSystemTray().remove(tray);
        });
    }
}

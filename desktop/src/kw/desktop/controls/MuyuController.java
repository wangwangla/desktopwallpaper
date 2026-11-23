package kw.desktop.controls;

import kw.manager.core.muyu.*;

import com.badlogic.gdx.Gdx;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public final class MuyuController {
    private final JFrame frame = new JFrame("一念木鱼 · 壁纸控制器");
    private final Timer timer;
    private TrayIcon tray;
    private final JTabbedPane tabs = new JTabbedPane();

    public MuyuController(MuyuScreen scene, kw.manager.core.WallpaperPlatform actions) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));
        panel.setBackground(new Color(247, 243, 233));
        JLabel title = new JLabel("一念 · 木鱼"), count = new JLabel(), today = new JLabel(), status = new JLabel();
        title.setFont(new Font("Microsoft YaHei", Font.BOLD, 24));
        panel.add(title);
        panel.add(Box.createVerticalStrut(20));
        panel.add(count);
        panel.add(Box.createVerticalStrut(8));
        panel.add(today);
        panel.add(Box.createVerticalStrut(22));
        JButton hit = new JButton("敲一下 · 功德 +1");
        hit.setAlignmentX(0);
        hit.setMaximumSize(new Dimension(340, 44));
        panel.add(hit);
        hit.addActionListener(e -> Gdx.app.postRunnable(scene::hit));
        panel.add(Box.createVerticalStrut(18));
        JCheckBox sound = new JCheckBox("敲击声音", scene.progress.sound), auto = new JCheckBox("自动敲击", false);
        sound.setOpaque(false);
        auto.setOpaque(false);
        panel.add(sound);
        panel.add(auto);
        sound.addActionListener(e -> Gdx.app.postRunnable(() -> scene.sound(sound.isSelected())));
        auto.addActionListener(e -> Gdx.app.postRunnable(() -> scene.automatic(auto.isSelected())));
        JLabel pace = new JLabel();
        JSlider slider = new JSlider(4, 30, Math.round(scene.progress.interval * 10));
        slider.setOpaque(false);
        slider.setMaximumSize(new Dimension(340, 42));
        panel.add(Box.createVerticalStrut(12));
        panel.add(pace);
        panel.add(slider);
        Runnable paceLabel = () -> pace.setText(String.format("敲击间隔：%.1f 秒", slider.getValue() / 10f));
        paceLabel.run();
        slider.addChangeListener(e -> {
            paceLabel.run();
            if (!slider.getValueIsAdjusting())
                Gdx.app.postRunnable(() -> scene.interval(slider.getValue() / 10f));
        });
        panel.add(Box.createVerticalStrut(22));
        JPanel buttons = new JPanel(new GridLayout(1, 2, 10, 0));
        buttons.setOpaque(false);
        JButton wall = new JButton("设为桌面壁纸"), preview = new JButton("窗口预览");
        buttons.add(wall);
        buttons.add(preview);
        buttons.setMaximumSize(new Dimension(340, 38));
        panel.add(buttons);
        wall.addActionListener(e -> Gdx.app.postRunnable(actions::wallpaper));
        preview.addActionListener(e -> Gdx.app.postRunnable(actions::preview));
        panel.add(Box.createVerticalStrut(20));
        panel.add(status);
        panel.add(Box.createVerticalStrut(12));
        JLabel tip = new JLabel("关闭面板后，可从托盘找回控制器。");
        tip.setForeground(new Color(140, 123, 102));
        panel.add(tip);
        setFont(panel, new Font("Microsoft YaHei", Font.PLAIN, 14));
        title.setFont(new Font("Microsoft YaHei", Font.BOLD, 24));
        count.setFont(new Font("Microsoft YaHei", Font.PLAIN, 20));
        tabs.addTab("木鱼", panel);
        tabs.addTab("上班鼓励", workPanel(scene));
        tabs.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        frame.setContentPane(tabs);
        frame.setSize(490, 710);
        frame.setLocation(60, 60);
        timer = new Timer(300, e -> {
            count.setText("累计功德：" + scene.progress.total);
            today.setText("今日 " + scene.progress.today + " 次 · 本次 " + scene.session() + " 下");
            status.setText(scene.status);
        });
        timer.start();
        if (SystemTray.isSupported()) {
            BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            g.setColor(new Color(154, 104, 57));
            g.fillOval(3, 6, 26, 23);
            g.setColor(new Color(61, 35, 20));
            g.drawArc(7, 12, 18, 12, 185, 170);
            g.dispose();
            PopupMenu menu = new PopupMenu();
            MenuItem open = new MenuItem("打开木鱼控制器"), quit = new MenuItem("退出木鱼壁纸");
            menu.add(open);
            menu.add(quit);
            open.addActionListener(e -> SwingUtilities.invokeLater(this::show));
            quit.addActionListener(e -> Gdx.app.postRunnable(actions::quit));
            tray = new TrayIcon(image, "一念木鱼", menu);
            tray.setImageAutoSize(true);
            tray.addActionListener(e -> SwingUtilities.invokeLater(this::show));
            try {
                SystemTray.getSystemTray().add(tray);
            } catch (AWTException e) {
                tray = null;
            }
        }
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (tray != null) frame.setVisible(false);
                else Gdx.app.postRunnable(actions::quit);
            }
        });
    }

    private static void setFont(Container panel, Font font) {
        for (Component child : panel.getComponents()) {
            child.setFont(font);
            if (child instanceof Container) setFont((Container) child, font);
        }
    }

    private JScrollPane workPanel(MuyuScreen scene) {
        WorkSettings s = scene.workSettings();
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(16, 16, 16, 16));
        form.setBackground(new Color(247, 243, 233));
        int row = 0;
        JCheckBox visible = new JCheckBox("在壁纸中显示上班鼓励卡片", s.visible);
        visible.setOpaque(false);
        row = field(form, row, "显示", visible);
        JTextField name = new JTextField(s.title);
        row = field(form, row, "组件名称", name);
        JPanel days = new JPanel(new GridLayout(2, 4, 4, 4));
        days.setOpaque(false);
        JCheckBox[] day = new JCheckBox[7];
        String[] labels = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        for (int i = 0; i < 7; i++) {
            day[i] = new JCheckBox(labels[i], (s.weekdays & (1 << i)) != 0);
            day[i].setOpaque(false);
            days.add(day[i]);
        }
        JButton weekdays = new JButton("周一至五");
        weekdays.addActionListener(e -> {
            for (int i = 0; i < 7; i++) day[i].setSelected(i < 5);
        });
        days.add(weekdays);
        row = field(form, row, "工作日", days);
        JTextField start = new JTextField(time(s.start), 5), end = new JTextField(time(s.end), 5);
        row = field(form, row, "工作时间", pair(start, "至", end));
        JCheckBox lunch = new JCheckBox("扣除休息时间", s.breakStart != s.breakEnd);
        lunch.setOpaque(false);
        row = field(form, row, "午休", lunch);
        JTextField breakStart = new JTextField(time(s.breakStart), 5), breakEnd = new JTextField(time(s.breakEnd), 5);
        row = field(form, row, "休息时间", pair(breakStart, "至", breakEnd));
        breakStart.setEnabled(lunch.isSelected());
        breakEnd.setEnabled(lunch.isSelected());
        lunch.addActionListener(e -> {
            breakStart.setEnabled(lunch.isSelected());
            breakEnd.setEnabled(lunch.isSelected());
        });
        JTextField income = new JTextField(String.format(java.util.Locale.US, "%.2f", s.dailyIncome));
        row = field(form, row, "每日收入 / 元", income);
        JSpinner payday = new JSpinner(new SpinnerNumberModel(s.payday, 1, 31, 1));
        row = field(form, row, "每月发薪日", payday);
        JComboBox<String> pet = new JComboBox<>(new String[]{"小猫", "仓鼠", "熊猫"});
        pet.setSelectedIndex("hamster".equals(s.pet) ? 1 : "panda".equals(s.pet) ? 2 : 0);
        row = field(form, row, "陪伴宠物", pet);
        JTextField petName = new JTextField(s.petName);
        row = field(form, row, "宠物名称", petName);
        final Color[] colors = {new Color(s.textColor), new Color(s.backgroundColor)};
        JButton textColor = new JButton("字体颜色"), bgColor = new JButton("背景颜色");
        JPanel colorRow = new JPanel(new GridLayout(1, 2, 8, 0));
        colorRow.setOpaque(false);
        colorRow.add(textColor);
        colorRow.add(bgColor);
        textColor.addActionListener(e -> {
            Color c = JColorChooser.showDialog(frame, "选择卡片字体颜色", colors[0]);
            if (c != null) {
                colors[0] = c;
                textColor.setForeground(c);
            }
        });
        bgColor.addActionListener(e -> {
            Color c = JColorChooser.showDialog(frame, "选择卡片背景颜色", colors[1]);
            if (c != null) {
                colors[1] = c;
                bgColor.setBackground(c);
            }
        });
        row = field(form, row, "卡片配色", colorRow);
        JLabel hint = new JLabel("<html>时间格式 09:00。默认日薪 200 元是示例。<br>今日收入按作息估算，午休和暂停期间不增长。<br>月累计假定过去工作日满勤，未计请假和节假日。<br>发薪日若遇小月，按当月最后一天计算。</html>");
        hint.setForeground(new Color(133, 116, 96));
        row = field(form, row, "说明", hint);
        JLabel result = new JLabel("所有设置仅保存在本机");
        JButton apply = new JButton("保存并应用布局");
        row = field(form, row, "", apply);
        field(form, row, "", result);
        apply.addActionListener(e -> {
            try {
                WorkSettings value = scene.workSettings();
                value.visible = visible.isSelected();
                value.title = name.getText().trim();
                value.weekdays = 0;
                for (int i = 0; i < 7; i++) if (day[i].isSelected()) value.weekdays |= 1 << i;
                value.start = minutes(start.getText());
                value.end = minutes(end.getText());
                value.breakStart = lunch.isSelected() ? minutes(breakStart.getText()) : value.start;
                value.breakEnd = lunch.isSelected() ? minutes(breakEnd.getText()) : value.start;
                value.dailyIncome = Double.parseDouble(income.getText().trim());
                value.payday = (Integer) payday.getValue();
                value.pet = new String[]{"cat", "hamster", "panda"}[pet.getSelectedIndex()];
                value.petName = petName.getText().trim();
                value.textColor = colors[0].getRGB() & 0xffffff;
                value.backgroundColor = colors[1].getRGB() & 0xffffff;
                value.configured = true;
                value.validate();
                apply.setEnabled(false);
                Gdx.app.postRunnable(() -> {
                    boolean ok = scene.configureWork(value);
                    SwingUtilities.invokeLater(() -> {
                        apply.setEnabled(true);
                        result.setText(ok ? "已保存，壁纸布局已更新" : "保存失败，请检查本机文件权限");
                    });
                });
            } catch (Exception error) {
                JOptionPane.showMessageDialog(frame, error instanceof java.time.format.DateTimeParseException ? "时间请使用 24 小时制，如 09:00、18:00。" : error.getMessage(), "请检查设置", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        setFont(form, new Font("Microsoft YaHei", Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        return scroll;
    }

    private static String time(int minute) {
        return String.format("%02d:%02d", minute / 60, minute % 60);
    }

    private static int minutes(String text) {
        java.time.LocalTime time = java.time.LocalTime.parse(text.trim());
        return time.getHour() * 60 + time.getMinute();
    }

    private static JPanel pair(JComponent first, String label, JComponent second) {
        JPanel p = new JPanel(new GridLayout(1, 3, 6, 0));
        p.setOpaque(false);
        p.add(first);
        p.add(new JLabel(label, SwingConstants.CENTER));
        p.add(second);
        return p;
    }

    private static int field(JPanel panel, int row, String name, JComponent value) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = row;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(7, 0, 7, 10);
        panel.add(new JLabel(name), c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 0, 4, 0);
        panel.add(value, c);
        return row + 1;
    }

    public void show() {
        frame.setVisible(true);
        frame.toFront();
    }

    public void showWork() {
        tabs.setSelectedIndex(1);
        show();
    }

    public void dispose() {
        SwingUtilities.invokeLater(() -> {
            timer.stop();
            frame.dispose();
            if (tray != null) SystemTray.getSystemTray().remove(tray);
        });
    }
}

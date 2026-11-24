package kw.desktop.controls;

import com.badlogic.gdx.Gdx;

import javax.swing.*;

import java.awt.*;

import kw.manager.core.WallpaperPlatform;

/**
 * 控制弹窗
 */
public class DemoController {
    //背景设置Demo
    private final JFrame frame = new JFrame("图标背景 Demo · 控制器");

    public DemoController(WallpaperPlatform platform) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(new JLabel("图标下方显示背景，移动后缓动跟随，删除后消失。"), BorderLayout.NORTH);
        JPanel buttons = new JPanel(new GridLayout(1, 3, 8, 0));
        JButton wall = new JButton("设为壁纸");
        JButton preview = new JButton("窗口预览");
        JButton quit = new JButton("退出 Demo");
        buttons.add(wall);
        buttons.add(preview);
        buttons.add(quit);
        panel.add(buttons);

        wall.addActionListener(e -> Gdx.app.postRunnable(platform::wallpaper));
        preview.addActionListener(e -> Gdx.app.postRunnable(platform::preview));
        quit.addActionListener(e -> Gdx.app.postRunnable(platform::quit));

        frame.setContentPane(panel);
        frame.setSize(510, 180);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

    public void show() {
        frame.setVisible(true);
        frame.toFront();
    }

    public void dispose() {
        frame.dispose();
    }
}

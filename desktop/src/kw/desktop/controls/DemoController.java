package kw.desktop.controls;

import com.badlogic.gdx.Gdx;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import kw.manager.core.WallpaperPlatform;

public class DemoController {
    private JFrame frame = new JFrame("Demo Controller");
    public DemoController(WallpaperPlatform wallpaperPlatform) {
        JPanel jPanel = new JPanel();
        JButton setWallerButton = new JButton("Set Waller");
        setWallerButton.addActionListener(e -> {});
        JButton windowShow = new JButton("Set Waller 2");
        windowShow.addActionListener(e -> {
            wallpaperPlatform.preview();
        });
        setWallerButton.addActionListener(e -> {
            wallpaperPlatform.wallpaper();
        });
        jPanel.add(setWallerButton);
        jPanel.add(windowShow);
        frame.setContentPane(jPanel);
        frame.setSize(400, 200);
        frame.setVisible(true);
    }
}

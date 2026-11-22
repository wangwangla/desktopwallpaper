package kw.wallpaper;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.imageio.ImageIO;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * One library and one controller for every wallpaper launcher.
 */
public final class WallpaperManager {
    private static final Color BG = new Color(245, 247, 249), INK = new Color(33, 46, 59), MUTED = new Color(111, 125, 137), GREEN = new Color(46, 128, 96);
    private final JFrame frame = new JFrame("桌面壁纸 · 统一控制器");
    private final JPanel cards = new JPanel(new GridLayout(0, 3, 18, 18));
    private final JLabel current = new JLabel("正在读取当前壁纸…"), count = new JLabel(), message = new JLabel("选择壁纸，即可预览或设为桌面背景。");
    private final JTextField query = new JTextField();
    private final WallpaperRuntime runtime = new WallpaperRuntime();
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "wallpaper-manager-worker");
        t.setDaemon(true);
        return t;
    });
    private final AtomicBoolean refreshing = new AtomicBoolean();
    private List<WallpaperCatalog.Entry> catalog = List.of();
    private List<WallpaperRuntime.Running> running = List.of();
    private final Map<String, Card> byId = new HashMap<>();
    private final javax.swing.Timer timer;
    private final List<JButton> operations = new ArrayList<>();
    private boolean busy;
    private TrayIcon tray;
    private WallpaperSession session;

    public WallpaperManager() throws IOException {
        this(false);
    }

    public WallpaperManager(boolean offline) throws IOException {
        UIManager.put("Label.font", new Font("Microsoft YaHei", Font.PLAIN, 13));
        UIManager.put("Button.font", new Font("Microsoft YaHei", Font.PLAIN, 13));
        UIManager.put("TextField.font", new Font("Microsoft YaHei", Font.PLAIN, 13));
        JPanel root = new JPanel(new BorderLayout(0, 22));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(30, 30, 22, 30));
        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel eyebrow = new JLabel("DESKTOP  /  WALLPAPERS");
        eyebrow.setFont(new Font("Segoe UI", Font.BOLD, 11));
        eyebrow.setForeground(MUTED);
        titles.add(eyebrow);
        titles.add(Box.createVerticalStrut(8));
        JLabel title = new JLabel("我的壁纸库");
        title.setFont(new Font("Microsoft YaHei", Font.BOLD, 30));
        title.setForeground(INK);
        titles.add(title);
        titles.add(Box.createVerticalStrut(10));
        current.setForeground(GREEN);
        titles.add(current);
        heading.add(titles, BorderLayout.WEST);
        JPanel tools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 9, 10));
        tools.setOpaque(false);
        query.setPreferredSize(new Dimension(185, 34));
        query.setToolTipText("搜索壁纸名称或功能");
        tools.add(query);
        JButton refresh = button("刷新列表", false), add = button("＋ 添加启动器", true);
        tools.add(refresh);
        tools.add(add);
        heading.add(tools, BorderLayout.EAST);
        refresh.addActionListener(e -> refresh());
        add.addActionListener(e -> addLauncher());
        operations.add(add);
        query.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }
        });
        root.add(heading, BorderLayout.NORTH);
        cards.setBackground(BG);
        JPanel library = new JPanel(new BorderLayout(0, 14));
        library.setOpaque(false);
        count.setForeground(MUTED);
        library.add(count, BorderLayout.NORTH);
        JPanel shelf = new JPanel(new BorderLayout());
        shelf.setOpaque(false);
        shelf.add(cards, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(shelf);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUnitIncrement(22);
        library.add(scroll);
        root.add(library);
        JPanel footer = new JPanel(new BorderLayout(10, 0));
        footer.setOpaque(false);
        message.setForeground(MUTED);
        footer.add(message);
        JPanel footerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footerButtons.setOpaque(false);
        JButton logs = button("查看日志", false), stop = button("停止全部壁纸", false);
        footerButtons.add(logs);
        footerButtons.add(stop);
        footer.add(footerButtons, BorderLayout.EAST);
        stop.addActionListener(e -> act("停止全部壁纸", () -> {
            for (var active : runtime.running(catalog)) runtime.stop(active);
            WallpaperAutostart.clear(null);
        }));
        operations.add(stop);
        logs.addActionListener(e -> {
            try {
                Desktop.getDesktop().open(WallpaperCatalog.directory().toFile());
            } catch (IOException error) {
                error(error);
            }
        });
        root.add(footer, BorderLayout.SOUTH);
        frame.setContentPane(root);
        frame.setSize(1160, 680);
        frame.setMinimumSize(new Dimension(1000, 620));
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        timer = new javax.swing.Timer(2000, e -> refresh());
        if (!offline) {
            session = new WallpaperSession("manager", () -> "manager", SwingUtilities::invokeLater, command -> {
                if (command.equals("stop")) quit();
                else show();
            });
            installTray();
            timer.start();
            refresh();
        }
    }

    private static JButton button(String text, boolean primary) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setForeground(primary ? Color.WHITE : INK);
        b.setBackground(primary ? new Color(43, 68, 88) : Color.WHITE);
        b.setOpaque(true);
        b.setBorder(new CompoundBorder(new LineBorder(primary ? new Color(43, 68, 88) : new Color(222, 228, 232), 1, true), new EmptyBorder(9, 12, 9, 12)));
        return b;
    }

    private void refresh() {
        if (!refreshing.compareAndSet(false, true)) return;
        worker.execute(() -> {
            try {
                List<WallpaperCatalog.Entry> all = WallpaperCatalog.discover();
                List<WallpaperRuntime.Running> active = runtime.running(all);
                SwingUtilities.invokeLater(() -> {
                    boolean rebuild = !all.equals(catalog);
                    catalog = all;
                    running = active;
                    if (rebuild) {
                        byId.clear();
                        for (var e : all) byId.put(e.id(), new Card(e));
                        filter();
                    }
                    for (var card : byId.values()) card.update();
                    updateCurrent();
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> error(e));
            } finally {
                refreshing.set(false);
            }
        });
    }

    private void filter() {
        cards.removeAll();
        String search = query.getText().trim().toLowerCase(Locale.ROOT);
        int visible = 0;
        for (var e : catalog)
            if ((e.title() + e.description()).toLowerCase(Locale.ROOT).contains(search)) {
                cards.add(byId.get(e.id()).panel);
                visible++;
            }
        count.setText("壁纸列表  ·  " + visible + " / " + catalog.size() + " 个壁纸");
        cards.revalidate();
        cards.repaint();
    }

    private void updateCurrent() {
        List<String> active = running.stream().filter(r -> r.mode().equals("wallpaper")).map(r -> r.entry().title()).distinct().toList();
        current.setText((active.isEmpty() ? "当前桌面：尚未设置动态壁纸" : "当前桌面：" + String.join("、", active))
                + (WallpaperAutostart.isEnabled() ? "  ·  开机自启已开启" : "  ·  开机自启已关闭"));
    }

    @FunctionalInterface
    private interface Operation {
        void run() throws Exception;
    }

    private void act(String description, Operation operation) {
        if (busy) return;
        busy = true;
        enable();
        message.setText(description + "…");
        message.setForeground(MUTED);
        worker.execute(() -> {
            try {
                operation.run();
                SwingUtilities.invokeLater(() -> {
                    message.setText(description + "完成");
                    message.setForeground(GREEN);
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> error(e));
            } finally {
                SwingUtilities.invokeLater(() -> {
                    busy = false;
                    enable();
                    refresh();
                });
            }
        });
    }

    private void enable() {
        for (JButton button : operations) button.setEnabled(!busy);
        for (Card card : byId.values()) card.update();
    }

    private void error(Exception e) {
        message.setForeground(new Color(171, 65, 62));
        message.setText("操作失败：" + e.getMessage());
        message.setToolTipText(e.toString());
    }

    private void addLauncher() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("添加现有壁纸启动器");
        chooser.setFileFilter(new FileNameExtensionFilter("壁纸启动器 (*.exe, *.jar)", "exe", "jar"));
        if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) return;
        Path path = chooser.getSelectedFile().toPath();
        String title = JOptionPane.showInputDialog(frame, "壁纸名称", path.getFileName().toString());
        if (title == null || title.isBlank()) return;
        act("添加壁纸", () -> WallpaperCatalog.add(path, title.trim()));
    }

    private final class Card {
        final WallpaperCatalog.Entry entry;
        final JPanel panel = new JPanel(new BorderLayout());
        final JLabel state = new JLabel();
        final JButton wall, preview, controls, stop;

        Card(WallpaperCatalog.Entry entry) {
            this.entry = entry;
            panel.setBackground(Color.WHITE);
            panel.setBorder(new LineBorder(new Color(224, 231, 235), 1, true));
            JLabel artwork = new JLabel(new ImageIcon(thumbnail(entry)));
            artwork.setOpaque(true);
            artwork.setBackground(new Color(entry.accent()));
            panel.add(artwork, BorderLayout.NORTH);
            JPanel details = new JPanel();
            details.setBackground(Color.WHITE);
            details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
            details.setBorder(new EmptyBorder(18, 18, 18, 18));
            JPanel top = new JPanel(new BorderLayout(8, 0));
            top.setOpaque(false);
            JLabel title = new JLabel(entry.title());
            title.setFont(new Font("Microsoft YaHei", Font.BOLD, 19));
            title.setForeground(INK);
            top.add(title);
            state.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
            top.add(state, BorderLayout.EAST);
            top.setAlignmentX(Component.LEFT_ALIGNMENT);
            details.add(top);
            details.add(Box.createVerticalStrut(10));
            JLabel description = new JLabel("<html><div style='width:230px'>" + html(entry.description()) + "</div></html>");
            description.setForeground(MUTED);
            description.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
            description.setAlignmentX(Component.LEFT_ALIGNMENT);
            details.add(description);
            details.add(Box.createVerticalStrut(20));
            wall = button(entry.builtIn() ? "设为壁纸" : "启动壁纸", true);
            preview = button("窗口预览", false);
            controls = button("壁纸设置", false);
            stop = button("停止", false);
            JPanel first = new JPanel(new GridLayout(1, 2, 8, 0));
            first.setOpaque(false);
            first.add(wall);
            first.add(preview);
            first.setAlignmentX(Component.LEFT_ALIGNMENT);
            details.add(first);
            details.add(Box.createVerticalStrut(8));
            JPanel second = new JPanel(new GridLayout(1, 2, 8, 0));
            second.setOpaque(false);
            second.add(controls);
            second.add(stop);
            second.setAlignmentX(Component.LEFT_ALIGNMENT);
            details.add(second);
            if (!entry.builtIn()) {
                preview.setVisible(false);
                controls.setText("打开启动器");
                description.setToolTipText("外部启动器的桌面设置由它自己的控制器提供");
            }
            panel.add(details);
            wall.addActionListener(e -> act("切换到「" + entry.title() + "」", () -> runtime.activate(entry, catalog)));
            preview.addActionListener(e -> act("预览「" + entry.title() + "」", () -> runtime.preview(entry, catalog)));
            controls.addActionListener(e -> act("打开「" + entry.title() + "」设置", () -> runtime.controls(entry, catalog)));
            stop.addActionListener(e -> act("停止「" + entry.title() + "」", () -> runtime.stopEntry(entry, catalog)));
            update();
        }

        void update() {
            List<WallpaperRuntime.Running> matches = running.stream().filter(r -> r.entry().id().equals(entry.id())).toList();
            boolean active = matches.stream().anyMatch(r -> r.mode().equals("wallpaper"));
            state.setText(active ? "● 桌面使用中" : matches.isEmpty() ? "未运行" : "● 已启动");
            state.setForeground(matches.isEmpty() ? MUTED : GREEN);
            wall.setEnabled(!busy && !active);
            preview.setEnabled(!busy);
            controls.setEnabled(!busy);
            stop.setEnabled(!busy && !matches.isEmpty());
            state.setToolTipText(matches.stream().anyMatch(r -> !r.managed()) ? "已识别独立启动的壁纸，切换时会接入统一控制" : "");
        }
    }

    private static String html(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static BufferedImage thumbnail(WallpaperCatalog.Entry e) {
        BufferedImage output = new BufferedImage(344, 194, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = output.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        BufferedImage source = null;
        try (var stream = WallpaperManager.class.getClassLoader().getResourceAsStream(e.thumbnail())) {
            if (stream != null) source = ImageIO.read(stream);
        } catch (IOException ignored) {
        }
        if (source != null) {
            double scale = Math.max(344.0 / source.getWidth(), 194.0 / source.getHeight());
            int w = (int) (source.getWidth() * scale), h = (int) (source.getHeight() * scale);
            g.drawImage(source, (344 - w) / 2, (194 - h) / 2, w, h, null);
        } else {
            g.setPaint(new GradientPaint(0, 0, new Color(e.accent()), 344, 194, new Color(223, 232, 233)));
            g.fillRect(0, 0, 344, 194);
            g.setColor(new Color(255, 255, 255, 65));
            g.fillOval(160, -60, 240, 240);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Microsoft YaHei", Font.BOLD, 27));
            g.drawString(e.title(), 25, 105);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g.drawString("DESKTOP WALLPAPER", 27, 136);
        }
        g.dispose();
        return output;
    }

    private void installTray() {
        if (!SystemTray.isSupported()) return;
        BufferedImage icon = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = icon.createGraphics();
        g.setColor(new Color(43, 68, 88));
        g.fillRoundRect(0, 0, 32, 32, 8, 8);
        g.setColor(new Color(189, 215, 205));
        g.fillRect(6, 7, 20, 15);
        g.fillRect(13, 23, 6, 2);
        g.dispose();
        PopupMenu menu = new PopupMenu();
        MenuItem open = new MenuItem("打开壁纸库"), stop = new MenuItem("停止全部壁纸"), exit = new MenuItem("退出管理器（保留壁纸）");
        menu.add(open);
        menu.add(stop);
        menu.addSeparator();
        menu.add(exit);
        open.addActionListener(e -> SwingUtilities.invokeLater(this::show));
        stop.addActionListener(e -> SwingUtilities.invokeLater(() -> act("停止全部壁纸", () -> {
            for (var active : runtime.running(catalog)) runtime.stop(active);
            WallpaperAutostart.clear(null);
        })));
        exit.addActionListener(e -> SwingUtilities.invokeLater(this::quit));
        tray = new TrayIcon(icon, "桌面壁纸 · 统一控制器", menu);
        tray.setImageAutoSize(true);
        tray.addActionListener(e -> SwingUtilities.invokeLater(this::show));
        try {
            SystemTray.getSystemTray().add(tray);
        } catch (AWTException e) {
            tray = null;
        }
    }

    public void show() {
        frame.setVisible(true);
        frame.setState(Frame.NORMAL);
        frame.toFront();
    }

    public void quit() {
        timer.stop();
        if (session != null) session.close();
        if (tray != null) SystemTray.getSystemTray().remove(tray);
        worker.shutdownNow();
        frame.dispose();
    }

    public void snapshot(Path file) throws Exception {
        catalog = WallpaperCatalog.discover();
        running = runtime.running(catalog);
        for (var e : catalog) byId.put(e.id(), new Card(e));
        filter();
        updateCurrent();
        frame.setSize(1160, 680);
        frame.setVisible(true);
        frame.validate();
        Container content = frame.getContentPane();
        BufferedImage image = new BufferedImage(content.getWidth(), content.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        content.printAll(g);
        g.dispose();
        Files.createDirectories(file.toAbsolutePath().getParent());
        ImageIO.write(image, "png", file.toFile());
        quit();
    }
}

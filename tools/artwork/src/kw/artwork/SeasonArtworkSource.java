package kw.artwork;
import kw.manager.core.seasons.*;

import kw.manager.core.city.*;
import kw.manager.core.seasons.SolarTerms;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Deterministic, layered illustration. The landscape is cached; only weather and drifting particles animate.
 */
public final class SeasonArtworkSource {
    public static final int W = 1600, H = 900;
    private BufferedImage landscape;
    private final BufferedImage canvas = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
    private int cachedTerm = -1;
    private static final int[][] PALETTES = {
            {0xafd9da, 0xf6ecda, 0x86ad64, 0x4c7b45, 0xf2b2c1, 0xffd9df, 0xd987a6},
            {0x8bcbd0, 0xe7ecd1, 0x8baa43, 0x3e743e, 0x7cac39, 0xc4d95d, 0x4f893b},
            {0xe9c47d, 0xffecc5, 0xc7a04d, 0x9e6e35, 0xd37b32, 0xf8be55, 0xb65332},
            {0xa3bdcf, 0xe8eff1, 0xdce9e9, 0x9fb8c4, 0xe7f0ed, 0xffffff, 0xb4cad5}};

    private static Color color(int rgb) {
        return new Color(rgb);
    }

    private static void oval(Graphics2D g, double x, double y, double w, double h) {
        g.fill(new Ellipse2D.Double(x, y, w, h));
    }

    private static void quality(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    public BufferedImage frame(SeasonSettings settings, WeatherData weather, String status, double elapsed) {
        ZonedDateTime now = Astronomy.time(settings);
        int term = settings.previewTerm < 0 ? SolarTerms.current(now.toLocalDate(), now.getZone()) : settings.previewTerm;
        if (cachedTerm != term) {
            landscape = landscape(term);
            cachedTerm = term;
        }
        BufferedImage image = canvas;
        Graphics2D g = image.createGraphics();
        quality(g);
        g.drawImage(landscape, 0, 0, null);
        double altitude = Astronomy.sun(now, settings.latitude, settings.longitude, weather).altitude;
        float night = (float) Math.max(0, Math.min(.72, (-altitude + 3) / 26));
        if (night > 0) {
            g.setColor(new Color(.06f, .13f, .24f, night));
            g.fillRect(0, 0, W, H);
        }
        if (altitude < -3) stars(g, elapsed, night);
        boolean rain = !weather.snow() && (weather.precipitation > .05 || weather.weatherCode >= 51);
        if (weather.cloudCover < 75 && !rain && !weather.fog()) orb(g, altitude < -3, elapsed);
        float cloud = weather.cloudCover / 100f;
        if (cloud > .1) {
            if (cloud > .8) {
                g.setColor(new Color(.27f, .35f, .40f, .20f));
                g.fillRect(0, 0, W, H);
            }
            for (int i = 0; i < (cloud > .8 ? 8 : cloud > .4 ? 5 : 2); i++) {
                double x = ((i * 357 + elapsed * (3 + i % 3)) % (W + 460)) - 230;
                cloud(g, x, 100 + (i * 89) % 240, 1.2 + i % 3 * .4, cloud > .8, night);
            }
        }
        if (weather.fog()) {
            for (int i = 0; i < 6; i++) {
                g.setColor(new Color(235, 242, 240, 38));
                oval(g, -240 + Math.sin(elapsed * .03 + i) * 100, 350 + i * 60, 2100, 140);
            }
        }
        particles(g, term, elapsed, rain, weather.snow());
        if (rain) {
            g.setColor(new Color(219, 238, 239, 130));
            g.setStroke(new BasicStroke(1.3f));
            for (int i = 0; i < 260; i++) {
                double x = (i * 137.31 - elapsed * 125) % (W + 80);
                if (x < 0) x += W + 80;
                double y = (i * 79.71 + elapsed * 680) % H;
                g.draw(new Line2D.Double(x, y, x - 8, y + 23));
            }
            g.setStroke(new BasicStroke(1));
            for (int i = 0; i < 26; i++) {
                double p = (elapsed * .9 + i * .19) % 1;
                g.setColor(new Color(220, 239, 235, (int) (85 * (1 - p))));
                g.draw(new Ellipse2D.Double((i * 173) % W, 640 + (i * 31) % 230, 55 * p, 12 * p));
            }
        }
        if (weather.weatherCode >= 95 && (elapsed % 9) < .12) {
            g.setColor(new Color(255, 255, 245, 100));
            g.fillRect(0, 0, W, H);
        }
        // Slow ripples on the reflected trees.
        g.setStroke(new BasicStroke(1));
        for (int i = 0; i < 28; i++) {
            g.setColor(new Color(244, 247, 225, i % 3 == 0 ? 75 : 30));
            double x = (i * 97 + elapsed * 8) % W;
            double y = 641 + i * 8;
            g.draw(new Line2D.Double(x, y, x + 35 + i % 5 * 16, y));
        }
        if (settings.showInfo) information(g, settings, weather, status, now, term, night > .3);
        g.dispose();
        return image;
    }

    public BufferedImage landscape(int term) {
        int season = SolarTerms.season(term);
        int[] p = PALETTES[season];
        Random r = new Random(9417);
        BufferedImage image = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        quality(g);
        g.setPaint(new GradientPaint(0, 0, color(p[0]), 0, 620, color(p[1])));
        g.fillRect(0, 0, W, H);
        // A quiet far shore and tiny silhouettes give the central grove depth.
        Path2D hill = new Path2D.Double();
        hill.moveTo(0, 565);
        hill.curveTo(250, 495, 330, 550, 560, 542);
        hill.curveTo(860, 505, 1250, 475, 1600, 540);
        hill.lineTo(1600, 630);
        hill.lineTo(0, 630);
        hill.closePath();
        g.setColor(new Color(color(p[2]).getRed(), color(p[2]).getGreen(), color(p[2]).getBlue(), 65));
        g.fill(hill);
        for (int i = 0; i < 20; i++) {
            double x = r.nextInt(W);
            g.setColor(new Color(color(p[3]).getRed(), color(p[3]).getGreen(), color(p[3]).getBlue(), 35));
            oval(g, x, 520 - r.nextInt(30), 30 + r.nextInt(20), 45);
        }
        BufferedImage grove = new BufferedImage(W, 640, BufferedImage.TYPE_INT_ARGB);
        Graphics2D t = grove.createGraphics();
        quality(t);
        tree(t, 430, 603, .78, term, 22);
        tree(t, 827, 603, 1.2, term, 23);
        tree(t, 1210, 603, .69, term, 24);
        t.dispose();
        g.setPaint(new GradientPaint(0, 625, color(p[0]), 0, H, color(p[1])));
        g.fillRect(0, 625, W, H - 625);
        g.setComposite(AlphaComposite.SrcOver.derive(.32f));
        g.drawImage(grove, 0, 626, W, H, 0, 620, W, 330, null);
        g.setComposite(AlphaComposite.SrcOver);
        g.drawImage(grove, 0, 0, null);
        g.setPaint(new GradientPaint(0, 578, color(p[2]), 0, 622, color(p[3])));
        g.fillRect(0, 584, W, 42);
        // Individually drawn grass, flowers and pebbles, with a fixed seed to avoid flicker.
        for (int i = 0; i < 4700; i++) {
            int x = r.nextInt(W), y = 585 + r.nextInt(36);
            g.setColor(season == 3 ? new Color(248, 250, 245, 150) : new Color(210 + r.nextInt(40), 205 + r.nextInt(45), 95 + r.nextInt(90), 110));
            g.drawLine(x, y, x + r.nextInt(5) - 2, y - r.nextInt(10));
        }
        for (int i = 0; i < 270; i++) {
            int x = r.nextInt(W), y = 594 + r.nextInt(25);
            int c = i % 4 == 0 ? 0xf8eacd : i % 4 == 1 ? p[4] : i % 4 == 2 ? 0xf6d66e : p[6];
            g.setColor(color(c));
            oval(g, x, y, 2 + r.nextInt(5), 2 + r.nextInt(4));
        }
        if (season == 3) {
            g.setColor(new Color(248, 252, 248));
            g.fillRect(0, 583, W, 6);
        }
        g.setColor(new Color(251, 246, 214, 190));
        g.fillRect(0, 626, W, 3);
        g.setColor(new Color(255, 255, 250, 90));
        g.fillRect(0, 634, W, 2);
        // Fine paper grain keeps the illustration soft without requiring external bitmap assets.
        for (int i = 0; i < 125000; i++) {
            int x = r.nextInt(W), y = r.nextInt(H);
            g.setColor(i % 2 == 0 ? new Color(255, 252, 231, 15) : new Color(52, 77, 76, 8));
            g.fillRect(x, y, 1 + r.nextInt(2), 1);
        }
        g.dispose();
        return image;
    }

    private void tree(Graphics2D g, double x, double ground, double scale, int term, long seed) {
        int s = SolarTerms.season(term);
        int[] p = PALETTES[s];
        Random r = new Random(seed);
        Graphics2D t = (Graphics2D) g.create();
        t.translate(x, ground);
        t.scale(scale, scale);
        t.setPaint(new GradientPaint(-14, 0, new Color(103, 75, 50), 16, 0, new Color(167, 119, 69)));
        Path2D trunk = new Path2D.Double();
        trunk.moveTo(-22, 0);
        trunk.curveTo(-4, -54, -3, -121, -8, -168);
        trunk.lineTo(8, -168);
        trunk.curveTo(5, -114, 11, -39, 24, 0);
        trunk.closePath();
        t.fill(trunk);
        t.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        t.setColor(new Color(121, 83, 57));
        for (int i = 0; i < 11; i++) {
            double endX = (r.nextDouble() - .5) * 180, endY = -160 - r.nextInt(115);
            t.draw(new QuadCurve2D.Double(0, -68 - r.nextInt(60), endX * .25, -165, endX, endY));
        }
        // Overlapping translucent clumps form an irregular, airy crown.
        for (int i = 0; i < 105; i++) {
            double angle = r.nextDouble() * Math.PI * 2, rad = Math.sqrt(r.nextDouble());
            double cx = Math.cos(angle) * 125 * rad, cy = -222 + Math.sin(angle) * 103 * rad;
            int rgb = p[4 + i % 3];
            Color c = color(rgb);
            t.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 110 + r.nextInt(95)));
            double size = 30 + r.nextInt(38);
            oval(t, cx - size / 2, cy - size / 2, size, size * .88);
        }
        float growth = (term % 6) / 5f;
        // Early spring has buds; late autumn has more visible branches.
        int flecks = s == 0 ? 1050 + (int) (growth * 800) : s == 2 ? 1500 - (int) (growth * 650) : s == 1 ? 1500 + (int) (growth * 350) : 1600 + (int) (growth * 150);
        for (int i = 0; i < flecks; i++) {
            double a = r.nextDouble() * Math.PI * 2, rad = Math.sqrt(r.nextDouble());
            double cx = Math.cos(a) * 132 * rad, cy = -225 + Math.sin(a) * 106 * rad;
            Color c = color(p[4 + r.nextInt(3)]);
            t.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 125 + r.nextInt(100)));
            double size = 2 + r.nextDouble() * 8;
            oval(t, cx, cy, size, size * .7);
            if (i % 12 == 0) {
                t.setColor(new Color(255, 248, 211, 170));
                oval(t, cx, cy, 3, 3);
            }
        }
        if (s == 3) {
            t.setColor(new Color(253, 255, 252, 235));
            for (int i = 0; i < 19; i++)
                oval(t, -112 + i * 12, -275 + Math.sin(i * .9) * 22, 32, 12);
        }
        t.dispose();
    }

    private void orb(Graphics2D g, boolean moon, double elapsed) {
        int x = 1310, y = 182;
        for (int i = 6; i > 0; i--) {
            g.setColor(moon ? new Color(235, 246, 242, 5) : new Color(255, 223, 129, 7));
            oval(g, x - 49 - i * 13, y - 49 - i * 13, 98 + i * 26, 98 + i * 26);
        }
        if (!moon) {
            g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(243, 191, 73, 160));
            for (int i = 0; i < 12; i++) {
                double a = i * Math.PI / 6;
                g.draw(new Line2D.Double(x + Math.cos(a) * 62, y + Math.sin(a) * 62, x + Math.cos(a) * 76, y + Math.sin(a) * 76));
            }
        }
        g.setColor(moon ? new Color(241, 245, 220) : new Color(249, 207, 89));
        oval(g, x - 43, y - 43, 86, 86);
        if (moon) {
            g.setColor(new Color(204, 222, 209, 95));
            oval(g, x - 14, y - 15, 19, 22);
            oval(g, x + 6, y + 10, 11, 12);
        }
    }

    private void cloud(Graphics2D g, double x, double y, double scale, boolean dark, float night) {
        Graphics2D c = (Graphics2D) g.create();
        c.translate(x, y);
        c.scale(scale, scale);
        int v = dark ? 174 : 252;
        v -= night * 80;
        c.setColor(new Color(v, Math.min(255, v + 3), Math.min(255, v + 1), dark ? 105 : 175));
        oval(c, 0, 10, 110, 35);
        oval(c, 21, -9, 58, 48);
        oval(c, 60, 0, 65, 44);
        oval(c, 90, 18, 54, 25);
        c.dispose();
    }

    private void stars(Graphics2D g, double elapsed, float night) {
        Random r = new Random(9);
        for (int i = 0; i < 85; i++) {
            int x = r.nextInt(W), y = r.nextInt(420);
            int a = (int) (night * 180 * (.65 + .35 * Math.sin(elapsed * .6 + i)));
            g.setColor(new Color(245, 246, 220, Math.max(0, a)));
            oval(g, x, y, i % 7 == 0 ? 3 : 1.5, i % 7 == 0 ? 3 : 1.5);
        }
    }

    private void particles(Graphics2D g, int term, double elapsed, boolean rain, boolean snow) {
        int s = SolarTerms.season(term);
        Random r = new Random(340);
        int count = snow ? 155 : (s == 0 || s == 2 ? 35 : 14);
        for (int i = 0; i < count; i++) {
            double x = (r.nextInt(W) + Math.sin(elapsed * .3 + i) * 45 + elapsed * (snow ? 9 : 17)) % (W + 20);
            double y = (r.nextInt(H) + elapsed * (snow ? 38 : s == 0 ? 20 : s == 2 ? 32 : 0)) % H;
            if (snow) {
                g.setColor(new Color(255, 255, 250, 190));
                double size = 2 + r.nextInt(5);
                oval(g, x, y, size, size);
            } else if (s == 0 || s == 2) {
                Graphics2D leaf = (Graphics2D) g.create();
                leaf.translate(x, y);
                leaf.rotate(Math.sin(elapsed * .8 + i) * 1.5);
                leaf.setColor(s == 0 ? new Color(255, 215, 223, 200) : new Color(202 + i % 3 * 15, 111 + i % 4 * 12, 44, 200));
                oval(leaf, -4, -2, 10, 5);
                leaf.dispose();
            } else if (s == 1 && !rain) {
                g.setColor(new Color(244, 236, 148, 100));
                oval(g, x, 530 + r.nextInt(110) + Math.sin(elapsed + i) * 9, 3, 3);
            }
        }
    }

    private void information(Graphics2D g, SeasonSettings settings, WeatherData data, String status, ZonedDateTime now, int term, boolean dark) {
        Color ink = dark ? new Color(240, 237, 214) : new Color(53, 76, 75);
        g.setColor(ink);
        g.setFont(new Font("Microsoft YaHei", Font.PLAIN, 17));
        g.drawString("岁 时  ·  二 十 四 节 气", 86, 76);
        g.setFont(new Font("Microsoft YaHei", Font.PLAIN, 68));
        g.drawString(SolarTerms.NAMES[term], 82, 166);
        g.setFont(new Font("Microsoft YaHei", Font.PLAIN, 18));
        g.drawString(SolarTerms.SEASONS[SolarTerms.season(term)] + "  /  " + SolarTerms.VERSES[term], 87, 207);
        g.setColor(new Color(ink.getRed(), ink.getGreen(), ink.getBlue(), 180));
        g.setStroke(new BasicStroke(1));
        g.drawLine(88, 231, 345, 231);
        g.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        String date = now.format(DateTimeFormatter.ofPattern("yyyy.MM.dd  EEEE"));
        g.drawString(date + (settings.previewTerm >= 0 ? "  · 节气预览" : ""), 88, 259);
        g.setColor(new Color(248, 249, 232, 185));
        g.fillRoundRect(1170, 759, 342, 96, 22, 22);
        g.setColor(new Color(53, 76, 75));
        g.setFont(new Font("Microsoft YaHei", Font.PLAIN, 19));
        String weather = data.fetchedAt > 0 || !"real".equals(settings.weatherMode) ? data.description() + "  " + Math.round(data.temperature) + "°" : "天气待更新";
        g.drawString(settings.city + "  ·  " + weather, 1193, 794);
        g.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        String state = "real".equals(settings.weatherMode) ? status : "天气效果预览";
        g.drawString(state.length() > 25 ? state.substring(0, 25) : state, 1193, 823);
        g.setColor(new Color(ink.getRed(), ink.getGreen(), ink.getBlue(), 190));
        g.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        g.drawString("四季流转，万物有时", 88, 842);
    }
}

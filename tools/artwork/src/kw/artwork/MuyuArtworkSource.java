package kw.artwork;




import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import java.util.Random;

final class MuyuArtworkSource {
    static Graphics2D graphics(BufferedImage image) {
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g;
    }

    static BufferedImage text(String text, int size, Color color, boolean serif) {
        Font font = new Font(serif ? "Serif" : "Microsoft YaHei", Font.PLAIN, size);
        BufferedImage probe = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D pg = graphics(probe);
        pg.setFont(font);
        FontMetrics metrics = pg.getFontMetrics();
        int width = Math.max(1, metrics.stringWidth(text) + 8), height = metrics.getHeight() + 8;
        pg.dispose();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = graphics(image);
        g.setFont(font);
        g.setColor(color);
        g.drawString(text, 4, 4 + metrics.getAscent());
        g.dispose();
        return image;
    }

    static BufferedImage background() {
        BufferedImage image = new BufferedImage(1200, 800, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = graphics(image);
        g.setPaint(new RadialGradientPaint(new Point2D.Float(600, 340), 800, new float[]{0, 1}, new Color[]{new Color(255, 252, 245), new Color(237, 227, 209)}));
        g.fillRect(0, 0, 1200, 800);
        Random r = new Random(17);
        g.setColor(new Color(113, 87, 53, 7));
        for (int i = 0; i < 12000; i++) g.fillRect(r.nextInt(1200), r.nextInt(800), 1, 1);
        g.dispose();
        return image;
    }

    static BufferedImage fish() {
        BufferedImage image = new BufferedImage(600, 440, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = graphics(image);
        g.setColor(new Color(66, 43, 24, 13));
        for (int i = 0; i < 14; i++)
            g.fill(new Ellipse2D.Float(113 - i * 2, 359 - i / 2f, 362 + i * 4, 20 + i));
        Shape body = new Ellipse2D.Float(96, 66, 401, 291);
        g.setPaint(new RadialGradientPaint(new Point2D.Float(215, 112), 350, new float[]{0, .5f, 1}, new Color[]{new Color(210, 163, 100), new Color(159, 104, 51), new Color(76, 43, 25)}));
        g.fill(body);
        Shape clip = g.getClip();
        g.clip(body);
        Random random = new Random(12);
        g.setStroke(new BasicStroke(1.3f));
        for (int i = 0; i < 44; i++) {
            g.setColor(new Color(i % 3 == 0 ? 243 : 75, i % 3 == 0 ? 197 : 44, i % 3 == 0 ? 133 : 24, 25));
            float y = 80 + i * 7;
            g.draw(new CubicCurve2D.Float(64, y, 170, y - 35 - random.nextInt(15), 400, y - 22, 532, y + 40));
        }
        g.setClip(clip);
        g.setColor(new Color(245, 205, 149, 60));
        g.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Arc2D.Float(131, 91, 244, 117, 55, 104, Arc2D.OPEN));
        Path2D mouth = new Path2D.Float();
        mouth.moveTo(134, 245);
        mouth.curveTo(245, 297, 371, 292, 465, 238);
        g.setColor(new Color(84, 47, 27));
        g.setStroke(new BasicStroke(28, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(mouth);
        g.setColor(new Color(44, 25, 18));
        g.setStroke(new BasicStroke(12, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(mouth);
        g.setColor(new Color(221, 168, 95, 140));
        g.setStroke(new BasicStroke(3));
        g.draw(new Arc2D.Float(137, 215, 323, 103, 192, 154, Arc2D.OPEN));
        g.setColor(new Color(88, 49, 26));
        g.setStroke(new BasicStroke(7, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Arc2D.Float(237, 164, 49, 55, 20, 145, Arc2D.OPEN));
        g.draw(new Arc2D.Float(297, 164, 49, 55, 15, 145, Arc2D.OPEN));
        g.fill(new Ellipse2D.Float(287, 214, 11, 7));
        g.dispose();
        return image;
    }

    static BufferedImage stick() {
        BufferedImage image = new BufferedImage(200, 80, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = graphics(image);
        g.setPaint(new GradientPaint(70, 31, new Color(219, 182, 125), 70, 49, new Color(127, 80, 43)));
        g.fill(new RoundRectangle2D.Float(40, 31, 157, 18, 18, 18));
        g.setColor(new Color(143, 89, 45));
        g.fill(new Ellipse2D.Float(8, 10, 56, 60));
        g.setColor(new Color(192, 138, 79));
        g.fill(new Ellipse2D.Float(10, 9, 48, 54));
        g.setColor(new Color(240, 200, 142, 145));
        g.setStroke(new BasicStroke(3));
        g.draw(new Arc2D.Float(17, 16, 32, 34, 50, 105, Arc2D.OPEN));
        g.dispose();
        return image;
    }
}

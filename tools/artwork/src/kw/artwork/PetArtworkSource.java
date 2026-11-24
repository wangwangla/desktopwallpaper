package kw.artwork;
import java.awt.*;
import java.awt.geom.*;
public final class PetArtworkSource {
    public void pet(Graphics2D g, String type, int x, int y) {
        Graphics2D p = (Graphics2D) g.create();
        p.translate(x, y);
        p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color fur = "panda".equals(type) ? new Color(244, 242, 229) : "hamster".equals(type) ? new Color(205, 180, 145) : new Color(217, 163, 100);
        p.setColor(new Color(227, 216, 197));
        p.fillOval(0, 0, 90, 85);
        p.setColor(fur);
        p.fillOval(17, 31, 57, 48);
        if ("cat".equals(type)) {
            Path2D ears = new Path2D.Float();
            ears.moveTo(19, 26);
            ears.lineTo(17, 4);
            ears.lineTo(37, 17);
            ears.lineTo(59, 17);
            ears.lineTo(79, 4);
            ears.lineTo(76, 29);
            ears.closePath();
            p.fill(ears);
        } else {
            p.setColor("panda".equals(type) ? new Color(75, 66, 54) : fur);
            p.fillOval(13, 8, 26, 26);
            p.fillOval(59, 8, 26, 26);
        }
        p.setColor(fur);
        p.fillOval(15, 13, 64, 52);
        if ("panda".equals(type)) {
            p.setColor(new Color(79, 70, 59));
            p.fillOval(26, 28, 15, 20);
            p.fillOval(54, 28, 15, 20);
        }
        p.setColor(new Color(79, 60, 44));
        p.fillOval(32, 34, 5, 7);
        p.fillOval(59, 34, 5, 7);
        p.fillOval(45, 45, 6, 4);
        p.drawArc(40, 45, 16, 12, 190, 150);
        p.setColor(new Color(226, 157, 139, 140));
        p.fillOval(24, 44, 10, 6);
        p.fillOval(62, 44, 10, 6);
        if ("cat".equals(type)) {
            p.setColor(new Color(174, 114, 62));
            p.setStroke(new BasicStroke(3));
            p.drawLine(40, 17, 42, 25);
            p.drawLine(49, 15, 49, 24);
            p.drawLine(58, 17, 56, 25);
        }
        p.dispose();
    }
}

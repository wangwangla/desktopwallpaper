package kw.manager.core.muyu;

/**
 * One contact event per queued swing, independent of the render frame rate.
 */
public final class MuyuStroke {
    public static final float CONTACT = .16f, DURATION = .34f;
    private float elapsed;
    private boolean active, contacted;
    private int queued;

    public void request() {
        queued++;
        if (!active) start();
    }

    private void start() {
        queued--;
        elapsed = 0;
        active = true;
        contacted = false;
    }

    public int update(float delta) {
        int contacts = 0;
        while (active && delta > 0) {
            float step = Math.min(delta, DURATION - elapsed);
            elapsed += step;
            delta -= step;
            if (!contacted && elapsed + 0.000001f >= CONTACT) {
                contacts++;
                contacted = true;
            }
            if (elapsed >= DURATION) {
                active = false;
                if (queued > 0) start();
            }
        }
        return contacts;
    }

    private float pose(float rest, float raised, float contact, float rebound) {
        if (!active) return rest;
        if (elapsed < .08f) return blend(rest, raised, elapsed / .08f);
        if (elapsed < CONTACT) return blend(raised, contact, (elapsed - .08f) / (CONTACT - .08f));
        if (elapsed < .22f) return blend(contact, rebound, (elapsed - CONTACT) / (.22f - CONTACT));
        return blend(rebound, rest, (elapsed - .22f) / (DURATION - .22f));
    }

    private static float blend(float a, float b, float t) {
        t = t * t * (3 - 2 * t);
        return a + (b - a) * t;
    }

    public float headX() {
        return pose(112, 185, 110, 143);
    }

    public float headY() {
        return pose(321, 529, 506, 536);
    }

    public float rotation() {
        return pose(26, -42, -18, -35);
    }
}

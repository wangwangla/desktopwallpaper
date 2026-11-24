package kw.manager.core.demo;

import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.math.Interpolation;

import kw.manager.core.DesktopIconSnapshot;

import java.util.*;
import java.util.function.Supplier;

/**
 * One Actor per icon. Actions follow changed targets without restarting on every poll.
 */
public final class IconBackdropLayer extends Group {
    private record Entry(Actor actor, float x, float y, float width, float height) {
    }

    private final Map<String, Entry> backgrounds = new LinkedHashMap<>();
    private final Supplier<? extends Actor> factory;

    public IconBackdropLayer(Supplier<? extends Actor> factory) {
        this.factory = factory;
        setTouchable(Touchable.disabled);
    }

    public Actor backdrop(String id) {
        Entry entry = backgrounds.get(id);
        return entry == null ? null : entry.actor();
    }

    public int iconCount() {
        return backgrounds.size();
    }

    public void synchronize(DesktopIconSnapshot snapshot, float worldWidth, float worldHeight) {
        setSize(worldWidth, worldHeight);
        if (!snapshot.available() || snapshot.width() <= 0 || snapshot.height() <= 0) return;
        float sx = worldWidth / snapshot.width(), sy = worldHeight / snapshot.height();
        Set<String> present = new HashSet<>();
        for (var icon : snapshot.icons()) {
            if (icon.width() <= 0 || icon.height() <= 0) continue;
            present.add(icon.id());
            float x = (icon.x() - 8) * sx, y = worldHeight - (icon.y() + icon.height() + 8) * sy, w = (icon.width() + 16) * sx, h = (icon.height() + 16) * sy;
            Entry old = backgrounds.get(icon.id());
            Actor actor;
            if (old == null) {
                actor = factory.get();
                actor.setName("desktop-icon-" + icon.id());
                actor.setTouchable(Touchable.disabled);
                actor.setBounds(x, y, w, h);
                addActor(actor);
            } else {
                actor = old.actor();
                if (Math.abs(old.x() - x) > .01f || Math.abs(old.y() - y) > .01f || Math.abs(old.width() - w) > .01f || Math.abs(old.height() - h) > .01f) {
                    actor.clearActions();
                    actor.addAction(Actions.parallel(Actions.moveTo(x, y, .4f, Interpolation.sineOut), Actions.sizeTo(w, h, .4f, Interpolation.sineOut)));
                }
            }
            backgrounds.put(icon.id(), new Entry(actor, x, y, w, h));
        }
        for (var iterator = backgrounds.entrySet().iterator(); iterator.hasNext(); ) {
            var entry = iterator.next();
            if (!present.contains(entry.getKey())) {
                entry.getValue().actor().clearActions();
                entry.getValue().actor().remove();
                iterator.remove();
            }
        }
    }
}

package kw.learn.core;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.utils.ScreenUtils;

import kw.learn.core.listener.WindowListener;

/**
 * @Auther jian xian si qi
 * @Date 2024/1/18 21:36
 */
public class WindowGame extends Game {
    private WindowListener listener;
    public WindowGame(WindowListener listener){
        this.listener = listener;
    }
    @Override
    public void create() {
        listener.windowForward();
        listener.moveWindowPosition(0,0);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.0f, .0f, 0.0f, 0.2f, false);
        super.render();
//        listener.moveWindowPosition(10,10);
    }
}

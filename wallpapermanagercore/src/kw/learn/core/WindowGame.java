package kw.learn.core;

import kw.learn.core.base.BaseGame;
import kw.learn.core.listener.WindowListener;

/**
 * @Auther jian xian si qi
 * @Date 2024/1/16 12:13
 */
public class WindowGame extends BaseGame {
    public WindowGame(WindowListener listener) {
        super();
        Constant.windowListener = listener;
    }

    @Override
    protected void loadingView() {
        super.loadingView();
        setScreen(new MainScreen(this));
        Constant.windowListener.moveWindowPosition(0,0);
    }
}

package kw.manager.core;

import kw.manager.core.base.ManagerBaseGame;
import kw.manager.core.listener.ManagerListener;

/**
 * @Auther jian xian si qi
 * @Date 2024/1/16 12:13
 */
public class WindowManagerGame extends ManagerBaseGame {
    public WindowManagerGame(ManagerListener listener) {
        super();
        ManagerConstant.windowListener = listener;
    }

    @Override
    protected void loadingView() {
        super.loadingView();
        setScreen(new ManagerMainScreen(this));
        ManagerConstant.windowListener.moveWindowPosition(0,0);
    }
}

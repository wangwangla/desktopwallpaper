package kw.manager.core;

import com.wallper.constant.Constant;
import com.wallper.screen.BasePetGame;

import kw.manager.core.listener.ManagerListener;

/**
 * @Auther jian xian si qi
 * @Date 2024/1/16 12:13
 */
public class WindowManagerGame extends BasePetGame {
    public WindowManagerGame(ManagerListener listener) {
        super();
        ManagerConstant.windowListener = listener;
        Constant.HIGHT = 1500;
        Constant.WIDTH = 2000;
    }

    @Override
    protected void loadingView() {
        super.loadingView();
        setScreen(new ManagerMainScreen(this));
    }
}

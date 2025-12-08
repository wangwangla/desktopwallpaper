package kw.manager.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.wallper.constant.Constant;
import com.wallper.listener.WindowListener;
import com.wallper.screen.BasePetGame;

import kw.manager.core.event.EventListener;


/**
 * @Auther jian xian si qi
 * @Date 2024/1/16 12:13
 */
public class WindowGame extends BasePetGame {
    public WindowGame(WindowListener listener) {
        super();
        Constant.windowListener = listener;
    }

    @Override
    protected void loadingView() {
        super.loadingView();
        setScreen(new MainScreen(this));
        Constant.windowListener.moveWindowPosition(0,0);
        Constant.windowListener.setWallpaper();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(Constant.viewColor.r,Constant.viewColor.g,Constant.viewColor.b,Constant.viewColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        super.render();
    }

    public void setMousePosition(int x, int y) {
        EventListener.getInstance().emit("mousePosisiton",x,y);
    }
}

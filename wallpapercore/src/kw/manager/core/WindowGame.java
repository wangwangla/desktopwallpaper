package kw.manager.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

import kw.manager.core.base.BaseGame;
import kw.manager.core.listener.WindowListener;

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

    @Override
    public void render() {
        Gdx.gl.glClearColor(Constant.viewColor.r,Constant.viewColor.g,Constant.viewColor.b,Constant.viewColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        super.render();
    }
}

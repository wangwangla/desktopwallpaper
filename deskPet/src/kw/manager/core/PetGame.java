package kw.manager.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

import kw.manager.core.base.BaseGame;
import kw.manager.core.constant.Constant;
/**
 * @Auther jian xian si qi
 * @Date 2024/1/16 12:13
 */
public class PetGame extends BaseGame {
    public PetGame() {
        super();
    }

    @Override
    protected void loadingView() {
        super.loadingView();
        setScreen(new PetScreen(this));
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 0);   // 透明背景
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        super.render();
    }
}

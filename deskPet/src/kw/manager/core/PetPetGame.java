package kw.manager.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

import kw.manager.core.base.BasePetGame;
import kw.manager.core.listener.MoveListener;

/**
 * @Auther jian xian si qi
 * @Date 2024/1/16 12:13
 */
public class PetPetGame extends BasePetGame {

    public PetPetGame() {
        super();
    }

    @Override
    protected void loadingView() {
        super.loadingView();
        setScreen(new PetScreen(this));
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(1,1,1,0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        super.render();
    }
}

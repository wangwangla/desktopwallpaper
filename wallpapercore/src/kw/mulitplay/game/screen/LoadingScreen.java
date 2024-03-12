package kw.mulitplay.game.screen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import kw.manager.core.base.BaseGame;
import kw.manager.core.base.BaseScreen;
import kw.mulitplay.game.UI.SnakePanel;
import kw.mulitplay.game.UI.SnakePanel1;


/**
 * all screen extends BaseScreen
 */
public class LoadingScreen extends BaseScreen {

    public LoadingScreen(BaseGame game) {
        super(game);
    }

    @Override
    public void initView() {
        SnakePanel panel = new SnakePanel();
        panel.setRunnable(new Runnable(){
            @Override
            public void run() {

                setScreen(LoadingScreen.class);
            }
        });
        stage.addActor(panel);
        stage.addListener(BackInputListener());
//        Image image = new Image(new Texture("1.png"));
//        stage.addActor(image);
//        image.addAction(Actions.moveTo(100,100,10));
    }

    private int dir;
    private InputListener BackInputListener() {
        return new InputListener(){
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if ((keycode == Input.Keys.LEFT)) {
                    dir = 4;
                }else if ((keycode == Input.Keys.RIGHT)) {
                    dir = 6;
                }else if ((keycode == Input.Keys.DOWN)) {
                    dir = 8;
                }else if ((keycode == Input.Keys.UP)) {
                    dir = 2;
                }
                return super.keyDown(event, keycode);
            }
        };
    }



    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        enterNextScreen();
    }

    public void enterNextScreen(){

    }
}

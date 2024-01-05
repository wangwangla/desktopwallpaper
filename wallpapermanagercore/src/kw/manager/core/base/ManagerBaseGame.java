package kw.manager.core.base;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import kw.manager.core.ManagerConstant;

/**
 * @Auther jian xian si qi
 * @Date 2024/1/18 21:36
 */
public class ManagerBaseGame extends Game {
     private Batch batch;
     private Viewport stageViewport;

    @Override
    public void create() {
        printInfo();
        initInstance();
        initViewport();
        initExtends();
        Gdx.app.postRunnable(()->{
            loadingView();
        });
    }

    private void printInfo() {
        String version = Gdx.gl.glGetString(GL20.GL_VERSION);
        String glslVersion = Gdx.gl.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION);
    }

    private void initExtends() {
    }

    protected void loadingView(){}

    private void initInstance(){
        Gdx.input.setCatchBackKey(true);
    }

    private void initViewport() {
        if (ManagerConstant.viewportType == ManagerConstant.EXTENDVIEWPORT) {
            stageViewport = new ExtendViewport(ManagerConstant.WIDTH, ManagerConstant.HIGHT);
        }else if (ManagerConstant.viewportType == ManagerConstant.FITVIEWPORT){
            stageViewport = new FitViewport(ManagerConstant.WIDTH, ManagerConstant.HIGHT);
        }
    }

    @Override
    public void resize(int width, int height) {
        viewPortResize(width, height);
        super.resize(width,height);
    }

    private void viewPortResize(int width, int height) {
        stageViewport.update(width,height);
        ManagerConstant.updateSize(stageViewport);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(ManagerConstant.viewColor.r, ManagerConstant.viewColor.g, ManagerConstant.viewColor.b, ManagerConstant.viewColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
//        extendInfo.setText(Gdx.app.getGraphics().getFramesPerSecond());
        super.render();
        try {
            if (ManagerConstant.DEBUG) {
                if (batch != null) {
                    batch.begin();
//                    extendInfo.draw(batch, 1);
                    batch.end();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
//
//        if (batch instanceof CpuPolygonSpriteBatch){
//            System.out.println(((CpuPolygonSpriteBatch) (batch)).renderCalls);
//        }

    }

    public Viewport getStageViewport() {
        return stageViewport;
    }

    public Batch getBatch() {
        if (batch==null) {
            batch = new SpriteBatch();
        }
        return batch;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (batch!=null) {
            batch.dispose();
            batch = null;
        }
        otherDispose();
    }

    @Override
    public void setScreen(Screen screen) {
        super.setScreen(screen);
    }

    public void otherDispose(){

    }
}

package kw.manager.core.asset;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.resolvers.LocalFileHandleResolver;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Disposable;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.loader.SkeletonDataLoader;

import java.util.ArrayList;

import kw.manager.core.ManagerConstant;

/**
 * @Auther jian xian si qi
 * @Date 2024/1/16 12:18
 */

public class ManagerAsset implements Disposable {
    private static ManagerAsset asset;
    public static AssetManager assetManager;
    public static AssetManager localAssetManager;
    private int i=0;
    private FrameBuffer frameBuffer;

    private FrameBuffer dialogBuffer = null;
    private ArrayList<FrameBuffer> mainBuffer = null;

    /**
     * 修改config文件之后会销毁游戏，重新创建， 没有正确处理资源管理器
     */
    public static void enterGameClear(){
        try {
            if (asset!=null){
                asset.setnull();
            }
            asset = null;
            assetManager = null;
            localAssetManager = null;
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("reset ------->");
        }
    }

    public void setnull(){
        if (assetManager != null) {
            assetManager.clear();
            assetManager.dispose();
        }
        assetManager = null;
        asset = null;
        frameBuffer = null;
        if (mainBuffer!=null) {
            mainBuffer.clear();
        }
        if (dialogBuffer != null){
            dialogBuffer = null;
        }
        dialogBuffer = null;
        if (localAssetManager!=null){
            localAssetManager.clear();
        }
        localAssetManager = null;
    }

    public static void resetFrameBuffer(FrameBuffer frameBuffer){
        if (frameBuffer != null) {
            frameBuffer.dispose();
            frameBuffer = null;
        }
    }

    public TextureAtlas getAtlas(String path){
        if (!assetManager.isLoaded(path)) {
            assetManager.load(path, TextureAtlas.class);
            assetManager.finishLoading();
        }
        TextureAtlas atlas = assetManager.get(path, TextureAtlas.class);
        for (Texture texture : atlas.getTextures()) {
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        return atlas;
    }
    //Gdx.files.local("levelpre/level" + levlNum+"/pre.png")
    public Texture getTexture(String path){
        if (!assetManager.isLoaded(path)) {
            TextureLoader.TextureParameter parameter = new TextureLoader.TextureParameter();
            parameter.magFilter = Texture.TextureFilter.Linear;
            parameter.minFilter = Texture.TextureFilter.Linear;
            assetManager.load(path, Texture.class,parameter);
            assetManager.finishLoading();
        }
        return assetManager.get(path,Texture.class);
    }

    public Texture getLocalTexture(String path){
//        System.out.println(Gdx.files.local(path).file().getAbsolutePath());
        if (!Gdx.files.local(path).exists()){
            return null;
        }
        if (!localAssetManager.isLoaded(path)) {
            TextureLoader.TextureParameter parameter = new TextureLoader.TextureParameter();
            parameter.magFilter = Texture.TextureFilter.Linear;
            parameter.minFilter = Texture.TextureFilter.Linear;
            localAssetManager.load(path, Texture.class,parameter);
            localAssetManager.finishLoading();
        }
        Texture texture = localAssetManager.get(path, Texture.class);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return localAssetManager.get(path,Texture.class);
    }

    public Sprite getSprite(String path){
        Texture texture = getTexture(path);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        Sprite sprite = new Sprite(texture);
        return sprite;
    }

    public void disposeTexture(String path){
        if (assetManager.isLoaded(path)) {
            assetManager.unload(path);
        }
    }

    private ManagerAsset(){
        i++;
        if (i>=2){
            throw new RuntimeException("gun");
        }
        mainBuffer = new ArrayList<>();
        assetManager = getAssetManager();
        assetManager.setLoader(SkeletonData.class,new SkeletonDataLoader(assetManager.getFileHandleResolver()));
        localAssetManager = getLocalAssetManager();
    }

    private AssetManager getAssetManager(){
        if (assetManager == null){
            assetManager = new AssetManager();
            assetManager.clear();
        }
        return assetManager;
    }

    public AssetManager getLocalAssetManager(){
        if (localAssetManager == null){
            localAssetManager = new AssetManager(new LocalFileHandleResolver());
        }
        return localAssetManager;
    }

    public static ManagerAsset getAsset() {
        if (asset==null){
            asset = new ManagerAsset();
        }
        return asset;
    }

    @Override
    public void dispose() {
        if (assetManager != null) {
            assetManager.clear();
            assetManager.dispose();
        }
        assetManager = null;
        asset = null;
        if (frameBuffer!=null) {
            frameBuffer.dispose();
        }
        frameBuffer = null;

        if (mainBuffer!=null) {

            for (FrameBuffer buffer : mainBuffer) {
                if (buffer != null) {
                    buffer.dispose();
                }
            }
            mainBuffer.clear();

        }

        if (dialogBuffer != null){
            dialogBuffer.dispose();
        }
        if (mainBuffer!=null) {
            for (FrameBuffer buffer : mainBuffer) {
                buffer.dispose();
            }
        }
        dialogBuffer = null;
        if (localAssetManager!=null){
            localAssetManager.clear();
            localAssetManager.dispose();
        }
        localAssetManager = null;
    }

    public TextureAtlas getLocalAtlas(String path) {
        if (!localAssetManager.isLoaded(path)) {
            localAssetManager.load(path, TextureAtlas.class);
            localAssetManager.finishLoading();
        }
        TextureAtlas atlas = localAssetManager.get(path, TextureAtlas.class);
        for (Texture texture : atlas.getTextures()) {
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        return atlas;
    }

    public BitmapFont loadBitFont(String path) {
        if (!assetManager.isLoaded(path)) {
            assetManager.load(path, BitmapFont.class);
            assetManager.finishLoading();
        }
        BitmapFont bitmapFont = assetManager.get(path);
        bitmapFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return assetManager.get(path);
    }

    public float getProcess(){
        return ManagerAsset.assetManager.getProgress();
    }

    public boolean update(){
        return ManagerAsset.assetManager.update();
    }

    public FrameBuffer mainBuffer(){
        FrameBuffer mainBufferTemp;
//        if (mainBuffer == null) {
//            Alpha, Intensity, LuminanceAlpha, RGB565, RGBA4444, RGB888, RGBA8888;
        Graphics.BufferFormat format = Gdx.graphics.getBufferFormat();
        if(format.r < 8){
            mainBufferTemp = new FrameBuffer(
                    Pixmap.Format.RGB565,
                    (int) ManagerConstant.GAMEWIDTH,
                    (int) ManagerConstant.GAMEHIGHT,
                    false);
        }else{
            try {
                mainBufferTemp = new FrameBuffer(
                        Pixmap.Format.RGB888,
                        (int) ManagerConstant.GAMEWIDTH,
                        (int) ManagerConstant.GAMEHIGHT,
                        false);
            }catch (Exception e){
                mainBufferTemp = new FrameBuffer(
                        Pixmap.Format.RGB565,
                        (int) ManagerConstant.GAMEWIDTH,
                        (int) ManagerConstant.GAMEHIGHT,
                        false);
            }
        }
//            buffer = new FrameBuffer();
//        }
        mainBuffer.add(mainBufferTemp);
        return mainBufferTemp;
    }


    public FrameBuffer dialogBuffer(){

        if (dialogBuffer == null) {

//            Alpha, Intensity, LuminanceAlpha, RGB565, RGBA4444, RGB888, RGBA8888;
            Graphics.BufferFormat format = Gdx.graphics.getBufferFormat();
            if(format.r < 8){
                dialogBuffer = new FrameBuffer(
                        Pixmap.Format.RGB565,
                        (int) ManagerConstant.GAMEWIDTH,
                        (int) ManagerConstant.GAMEHIGHT,
                        false);
            }else{
                try {
                    dialogBuffer = new FrameBuffer(
                            Pixmap.Format.RGB888,
                            (int) ManagerConstant.GAMEWIDTH,
                            (int) ManagerConstant.GAMEHIGHT,
                            false);
                }catch (Exception e){
                    dialogBuffer = new FrameBuffer(
                            Pixmap.Format.RGB565,
                            (int) ManagerConstant.GAMEWIDTH,
                            (int) ManagerConstant.GAMEHIGHT,
                            false);
                }
            }
//            buffer = new FrameBuffer();
        }
        return dialogBuffer;
    }

    public FrameBuffer buffer(){
        if (frameBuffer == null) {

//            Alpha, Intensity, LuminanceAlpha, RGB565, RGBA4444, RGB888, RGBA8888;
            Graphics.BufferFormat format = Gdx.graphics.getBufferFormat();
            if(format.r < 8){
                frameBuffer = new FrameBuffer(
                        Pixmap.Format.RGB565,
                        (int) ManagerConstant.GAMEWIDTH,
                        (int) ManagerConstant.GAMEHIGHT,
                        false);
            }else{
                try {
                    frameBuffer = new FrameBuffer(
                            Pixmap.Format.RGB888,
                            (int) ManagerConstant.GAMEWIDTH,
                            (int) ManagerConstant.GAMEHIGHT,
                            false);
                }catch (Exception e){
                    frameBuffer = new FrameBuffer(
                            Pixmap.Format.RGB565,
                            (int) ManagerConstant.GAMEWIDTH,
                            (int) ManagerConstant.GAMEHIGHT,
                            false);
                }
            }
//            buffer = new FrameBuffer();
        }
        return frameBuffer;
    }

    public void unloadResource(String path) {
//        if (assetManager.isLoaded())
    }

    public boolean isAssetManagerLoaded(String loadFilePath) {
        return assetManager.isLoaded(loadFilePath);
    }

    public boolean isLocalAssetManagerLoaded(String loadFilePath) {
        return localAssetManager.isLoaded(loadFilePath);
    }

    public void localAssetManagerLoad(String loadFilePath) {
        localAssetManager.load(loadFilePath,Texture.class);
    }

    public void assetManagerLoad(String loadFilePath) {
        assetManager.load(loadFilePath,Texture.class);
    }
}

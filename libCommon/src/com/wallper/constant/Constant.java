package com.wallper.constant;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.viewport.Viewport;


/**
 * @Auther jian xian si qi
 * @Date 2024/1/16 12:09
 */

public class Constant {
    public static boolean DEBUG = false;
    //    viewport
    public static final int EXTENDVIEWPORT = 0;
    public static final int FITVIEWPORT = 1;
    public static float WIDTH = 916;
    public static float HIGHT = 680;
    public static float GAMEWIDTH = 1080;
    public static float GAMEHIGHT = 1920;
    public static Color viewColor = new Color(1,1,1,0f);
    public static int viewportType = 0;

    public static void updateSize(Viewport stageViewport) {
        if (stageViewport == null)return;
        Constant.GAMEWIDTH = stageViewport.getWorldWidth();
        Constant.GAMEHIGHT = stageViewport.getWorldHeight();
    }
}


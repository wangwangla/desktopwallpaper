package kw.manager.core;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.viewport.Viewport;

import kw.manager.core.listener.ManagerListener;

/**
 * @Auther jian xian si qi
 * @Date 2024/1/16 12:09
 */

public class ManagerConstant {
    public static boolean DEBUG = false;
    //    viewport
    public static final int EXTENDVIEWPORT = 0;
    public static final int FITVIEWPORT = 1;
    public static float WIDTH = 1920/2.0f;
    public static float HIGHT = 1200/2.0f;
    public static float GAMEWIDTH = 1080;
    public static float GAMEHIGHT = 1920;
    public static final float STDWIDTH = 1920;
    public static final float STDHIGHT = 1920;
    public static ManagerListener windowListener;

    public static Color viewColor = new Color(1,1,1,0.5f);
    /**
     * 0 : cpubatch
     *
     */
    public static int batchType = 0;
    public static int viewportType = 0;
    public static boolean isSound = true;
    public static boolean isHaptic = true;
    public static float soundV = 1;
    public static boolean isMusic = true;
    public static String severLevelOrder = null;
    public static String severLevelPreOrder = null;
    public static boolean checkLevelFlag = false;

    public static void updateSize(Viewport stageViewport) {
        if (stageViewport == null)return;
        ManagerConstant.GAMEWIDTH = stageViewport.getWorldWidth();
        ManagerConstant.GAMEHIGHT = stageViewport.getWorldHeight();
    }

    public static int jishu = 0;

    public static long time = 0;
    public static long timeC = 0;

}


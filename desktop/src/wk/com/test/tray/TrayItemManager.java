package wk.com.test.tray;

import com.badlogic.gdx.utils.Array;

import java.awt.SystemTray;
import java.awt.TrayIcon;

public class TrayItemManager {
    private Array<TrayData> trayDataArray;
    private static TrayItemManager trayItemManager;
    private TrayItemManager(){
        trayDataArray = new Array<>();
    };

    public static TrayItemManager getTrayItemManager() {
        if (trayItemManager == null){
            trayItemManager = new TrayItemManager();
        }
        return trayItemManager;
    }

    public void addTraydata(TrayData trayData){
        this.trayDataArray.add(trayData);
    }

    public void closeTrayAndProcess(TrayData trayData){
        if (trayData!=null) {
            closeProcess(trayData.getProcess());
            closeTray(trayData.getItem());
        }
    }

    private void closeProcess(Process runningProcess){
        if (runningProcess!=null && runningProcess.isAlive()){
            System.out.println("old process exsit!");
            runningProcess.destroy();
            try {
                runningProcess.waitFor();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void closeTray(TrayIcon trayIcon){
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("JVM 退出，移除托盘图标");
            SystemTray tray = SystemTray.getSystemTray();
            tray.remove(trayIcon);
        }));
    }

}

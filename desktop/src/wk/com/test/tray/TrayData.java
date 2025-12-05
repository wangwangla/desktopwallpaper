package wk.com.test.tray;

import java.awt.TrayIcon;

public class TrayData {
    private TrayItem item;
    private Process process;

    public TrayItem getItem() {
        if (item!=null) {
            return item;
        }else {
            return null;
        }
    }

    public void setItem(TrayItem item) {
        this.item = item;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    @Override
    public String toString() {
        return "TrayData{" +
                "item=" + item +
                ", process=" + process +
                '}';
    }
}

package fr.vmarchaud.mineweb.common.injector;

import fr.vmarchaud.mineweb.common.ICore;
import lombok.Getter;

public class WebThread extends Thread {

    @Getter
    private NettyServer webServer;

    public WebThread (ICore api) {
        this.webServer = new NettyServer(api);
    }

    @Override
    public void run() {
        try {
            webServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopThread() {
        try {
            webServer.stop();
            interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
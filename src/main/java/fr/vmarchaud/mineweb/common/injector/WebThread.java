package fr.vmarchaud.mineweb.common.injector;

import fr.vmarchaud.mineweb.common.ICore;
import lombok.Getter;

public class WebThread extends Thread {

    private final ICore api;
    @Getter
    private NettyServer webServer;

    public WebThread (ICore api) {
        this.api = api;
        this.webServer = new NettyServer(api);
    }

    @Override
    public void run() {
        try {
            webServer.start();
        } catch (Exception e) {
            api.logger().info("HTTP server start failed! (" + e.getMessage() + ")");
            this.interrupt();
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
package fr.vmarchaud.mineweb.common.injector;

import fr.vmarchaud.mineweb.common.ICore;
import lombok.Getter;
import org.bukkit.Bukkit;

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
            Bukkit.getConsoleSender().sendMessage("HTTP server start failed! (" + e.getMessage() + ")");
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
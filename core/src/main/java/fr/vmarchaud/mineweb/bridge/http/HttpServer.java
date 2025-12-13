package fr.vmarchaud.mineweb.bridge.http;

import fr.vmarchaud.mineweb.bridge.api.ICore;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class HttpServer {

    private final ICore core;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelFuture future;

    public HttpServer(ICore core) {
        this.core = core;
    }

    public void start(int port) throws InterruptedException {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new HttpServerInitializer(core));
        future = bootstrap.bind(port).sync();
        core.logger().info("MineWeb HTTP listening on port " + port);
    }

    public void stop() {
        try {
            if (future != null) {
                future.channel().close().syncUninterruptibly();
            }
        } finally {
            if (bossGroup != null) {
                bossGroup.shutdownGracefully();
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
            }
        }
    }
}

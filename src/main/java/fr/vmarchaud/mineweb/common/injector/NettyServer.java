package fr.vmarchaud.mineweb.common.injector;

import fr.vmarchaud.mineweb.common.ICore;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyServer {

    private ICore api;
    private ChannelFuture f;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public NettyServer(ICore api) {
        this.api = api;
    }

    public void start() throws Exception {
        // Configure the server.
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             //.option(ChannelOption.SO_BACKLOG, 100)
             //.handler(new LoggingHandler(LogLevel.INFO))
             .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline p = ch.pipeline();
                    p.addFirst(new JSONAPIChannelDecoder(api));
                }
             });
            // Start the server.
            this.f = b.bind(api.config().getPort()).sync();

            // Wait until the server socket is closed.
            this.f.channel().closeFuture().sync();
        } finally {
            // Shut down all event loops to terminate all threads.
            this.bossGroup.shutdownGracefully();
            this.workerGroup.shutdownGracefully();
        }
    }

    public void stop() {
        try {
            f.channel().closeFuture().sync();
        } catch (Exception e) {
        }
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
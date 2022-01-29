package org.rivelles.adapters.server

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.HttpRequestDecoder
import io.netty.handler.codec.http.HttpResponseEncoder
import org.rivelles.adapters.server.handlers.LightweightHttpServerHandler
import org.rivelles.core.domain.context.Context


class LightweightServer(private val port: Int, private val context: Context) {

    fun run() {
        val bossGroup = NioEventLoopGroup()
        val workerGroup = NioEventLoopGroup()
        try {
            val bootstrap = ServerBootstrap()
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .childHandler(object: ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        ch.pipeline()
                            .addLast(
                                HttpRequestDecoder(),
                                HttpResponseEncoder(),
                                LightweightHttpServerHandler(context)
                            )
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)

            val channelFuture = bootstrap.bind(port).sync()
            channelFuture
                .channel()
                .closeFuture()
                .sync()
        }
        finally {
            workerGroup.shutdownGracefully()
            bossGroup.shutdownGracefully()
        }
    }
}
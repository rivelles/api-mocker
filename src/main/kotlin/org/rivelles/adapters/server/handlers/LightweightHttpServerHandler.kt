package org.rivelles.adapters.server.handlers

import io.netty.buffer.Unpooled
import io.netty.buffer.Unpooled.EMPTY_BUFFER
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.HttpHeaderNames.CONNECTION
import io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH
import io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE
import io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST
import io.netty.handler.codec.http.HttpResponseStatus.CONTINUE
import io.netty.handler.codec.http.HttpResponseStatus.OK
import io.netty.handler.codec.http.HttpUtil
import io.netty.handler.codec.http.HttpVersion.HTTP_1_1
import io.netty.handler.codec.http.LastHttpContent
import io.netty.util.CharsetUtil.UTF_8
import org.rivelles.adapters.server.utils.RequestUtils
import org.rivelles.core.domain.context.Context


class LightweightHttpServerHandler(val context: Context): SimpleChannelInboundHandler<Any>() {

    private lateinit var httpRequest: HttpRequest
    private val responseData = StringBuilder()
    private val requestUtils = RequestUtils()

    /*
        This method is called several times. The first one stores HttpRequest object in httpRequest variable, which will
        be used later in order to match with the file context.
     */
    override fun channelRead0(ctx: ChannelHandlerContext?, msg: Any?) {
        msg?.takeIf { it is HttpRequest }
            ?.let { msg ->
                println("Received $msg as HttpRequest")
                println("HttpRequest: $msg")
                httpRequest = msg as HttpRequest
                ctx.takeIf { HttpUtil.is100ContinueExpected(httpRequest) }.let { ctx ->
                    writeResponse(ctx)
                }
            }

            msg.takeIf { it is LastHttpContent }
                ?.let { msg ->
                    println("Received $msg as LastHttpContent")
                    writeResponse(ctx, msg as LastHttpContent, StringBuilder("A"))
                }
    }

    /*
        Writes final message
     */
    private fun writeResponse(ctx: ChannelHandlerContext?, trailer: LastHttpContent, responseData: StringBuilder) {
        val httpResponse = DefaultFullHttpResponse(
            HTTP_1_1,
            if (trailer.decoderResult().isSuccess) OK else BAD_REQUEST,
            Unpooled.copiedBuffer(responseData.toString(), UTF_8)
        )
        httpResponse.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8")

        httpResponse.takeIf { HttpUtil.isKeepAlive(httpRequest) }
            ?.let {
                println("http keep alive is true")
                it.headers().setInt(CONTENT_LENGTH, it.content().readableBytes())
                it.headers().set(CONNECTION, KEEP_ALIVE)
            }
        ctx?.write(httpResponse)

        ctx.takeIf { !HttpUtil.isKeepAlive(httpRequest) }
            ?.let {
                println("http keep alive is false, flushing response")
                it.writeAndFlush(EMPTY_BUFFER).addListener { ChannelFutureListener.CLOSE }
            }
    }

    /*
        Writes 100 CONTINUE response
     */
    private fun writeResponse(ctx: ChannelHandlerContext?) {
        val response = DefaultFullHttpResponse(HTTP_1_1, CONTINUE, EMPTY_BUFFER)
        ctx?.write(response)
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        ctx.flush()
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.close()
    }
}
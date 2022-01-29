package org.rivelles.adapters.server.utils

import io.netty.handler.codec.http.HttpContent
import io.netty.handler.codec.http.HttpObject
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.LastHttpContent
import io.netty.handler.codec.http.QueryStringDecoder
import io.netty.util.CharsetUtil


class RequestUtils {
    fun formatParams(request: HttpRequest): StringBuilder {
        val responseData = StringBuilder()
        val queryStringDecoder = QueryStringDecoder(request.uri())
        val params = queryStringDecoder.parameters()
        if (params.isNotEmpty()) {
            for (p in params.entries) {
                val key = p.key
                val values = p.value
                for (value in values) {
                    responseData
                        .append("Parameter: ")
                        .append(key.uppercase())
                        .append(" = ")
                        .append(value.uppercase())
                        .append("\r\n")
                }
            }
            responseData.append("\r\n")
        }
        return responseData
    }

    fun formatBody(httpContent: HttpContent): StringBuilder {
        val responseData = StringBuilder()
        val content = httpContent.content()

        if (content.isReadable) {
            responseData
                .append(content.toString(CharsetUtil.UTF_8).uppercase())
                .append("\r\n");
        }

        return responseData
    }

    fun prepareLastResponse(trailer: LastHttpContent): StringBuilder {
        val responseData = StringBuilder()
        responseData.append("Good Bye!\r\n")
        if (!trailer.trailingHeaders().isEmpty) {
            responseData.append("\r\n")
            for (name in trailer.trailingHeaders().names()) {
                for (value in trailer.trailingHeaders().getAll(name)) {
                    responseData.append("P.S. Trailing Header: ")
                    responseData.append(name).append(" = ").append(value).append("\r\n")
                }
            }
            responseData.append("\r\n")
        }
        return responseData
    }

    fun evaluateDecoderResult(o: HttpObject): StringBuilder? {
        val responseData = StringBuilder()
        val result = o.decoderResult()
        if (!result.isSuccess) {
            responseData.append("..Decoder Failure: ")
            responseData.append(result.cause())
            responseData.append("\r\n")
        }
        return responseData
    }
}
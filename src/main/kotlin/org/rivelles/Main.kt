package org.rivelles

import org.rivelles.adapters.file.FileTransformer
import org.rivelles.adapters.server.LightweightServer
import java.io.File


fun main(args : Array<String>) {
    val port = if (args.isNotEmpty()) args[0].toInt() else 8089
    val path = if (args.isNotEmpty() && args.size > 1) args[1] else "src/main/resources/spec.yaml"
    val context = FileTransformer().fromPath(path)

    val s = LightweightServer(port, context)
    s.run()
}
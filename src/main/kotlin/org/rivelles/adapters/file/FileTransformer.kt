package org.rivelles.adapters.file

import com.charleskorn.kaml.Yaml
import org.rivelles.core.domain.context.Context
import java.io.File

class FileTransformer {
    fun fromPath(filePath: String): Context =
        Yaml.default.decodeFromString(
            Context.serializer(),
            File(filePath).bufferedReader().use {
                it.readText()
            }
        )
}
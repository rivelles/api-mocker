package org.rivelles.core.domain.route

import kotlinx.serialization.Serializable
import org.rivelles.core.domain.response.Response

@Serializable
data class Route (
    val name: String,
    val path: String,
    val methods: List<String>,
    val response: Response
)
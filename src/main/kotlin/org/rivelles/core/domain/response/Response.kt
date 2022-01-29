package org.rivelles.core.domain.response

import kotlinx.serialization.Serializable

@Serializable
data class Response(
    val code: Int,
    val content: String
)
package org.rivelles.core.domain.context

import kotlinx.serialization.Serializable
import org.rivelles.core.domain.route.Route

@Serializable
data class Context(val routes: List<Route>)
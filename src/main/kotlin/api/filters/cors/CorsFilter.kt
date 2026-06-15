package api.filters.cors

import api.ProjectHTTPHeaders
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerResponseContext
import jakarta.ws.rs.container.ContainerResponseFilter
import jakarta.ws.rs.ext.Provider

@Provider
class CorsFilter : ContainerResponseFilter {
    companion object {
        private val ALLOWED_ORIGINS =
            setOf(
                "http://localhost:5173",
                "http://localhost:3000",
                "https://ssnagin.github.io",
                "https://se-itmo-labs.github.io"
            )
    }

    override fun filter(
        requestContext: ContainerRequestContext,
        responseContext: ContainerResponseContext,
    ) {
        val origin = requestContext.headers.getFirst("Origin")

        if (origin != null && origin in ALLOWED_ORIGINS) {
            responseContext.headers.add("Access-Control-Allow-Origin", origin)
            responseContext.headers.add("Access-Control-Allow-Credentials", "true")
        }

        responseContext.headers.add("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS")
        responseContext.headers.add("Access-Control-Allow-Headers", "Content-Type, ${ProjectHTTPHeaders.AUTHORIZATION}")
    }
}

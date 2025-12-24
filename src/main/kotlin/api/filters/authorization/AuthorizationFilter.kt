package api.filters.authorization

import api.ErrorResponse
import api.ProjectHTTPHeaders
import jakarta.annotation.Priority
import jakarta.ws.rs.Priorities
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.Provider

@Provider
@Priority(Priorities.AUTHORIZATION)
class AuthorizationFilter : ContainerRequestFilter {

    companion object {
        private val RESPONSE_UNAUTHORIZED = Response
            .status(Response.Status.UNAUTHORIZED)
            .entity(ErrorResponse().error("Иди регайся"))
            .build()

        private val PUBLIC_PATHS = setOf(
            "/user/auth",
            "/user/register",
        )
    }

    override fun filter(requestContext: ContainerRequestContext) {

        if (isPublicPath(requestContext.uriInfo.path)) return

        val authHeader = requestContext.getHeaderString(ProjectHTTPHeaders.AUTHORIZATION)

        if (authHeader.isNullOrEmpty()) requestContext.abortWith(RESPONSE_UNAUTHORIZED)
    }

    private fun isPublicPath(requestPath: String): Boolean {
        return PUBLIC_PATHS.contains(requestPath)
    }
}
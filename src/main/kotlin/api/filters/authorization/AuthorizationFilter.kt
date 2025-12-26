package api.filters.authorization

import JwtUtil
import api.GenericResource
import api.ProjectHTTPHeaders
import api.response.GeneralResponseBuilder
import jakarta.annotation.Priority
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.ws.rs.Priorities
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.Provider

@Provider
@Priority(Priorities.AUTHORIZATION)
@RequestScoped
open class AuthorizationFilter : ContainerRequestFilter {

    @Inject
    private lateinit var jwtUtil: JwtUtil

    companion object {
//        private val RESPONSE_UNAUTHORIZED = Response
//            .status(Response.Status.UNAUTHORIZED)
//            .entity(ErrorResponse().error("Иди регайся"))
//            .build()

        private val PUBLIC_PATHS = setOf(
            "/user/auth",
            "/user/register"
        )
    }

    override fun filter(requestContext: ContainerRequestContext) {

        if (isPublicPath(requestContext.uriInfo.path)) return

        val authHeader = requestContext.getHeaderString(ProjectHTTPHeaders.AUTHORIZATION)

        if (authHeader.isNullOrEmpty() || !jwtUtil.validateToken(authHeader)) {
            requestContext.abortWith(
                GenericResource.unauthorized("Иди регайся")
            )
        }
    }

    private fun isPublicPath(requestPath: String): Boolean {
        val normalizedPath = requestPath.removeSuffix("/")
        return PUBLIC_PATHS.any { it.removeSuffix("/") == normalizedPath }
    }
}
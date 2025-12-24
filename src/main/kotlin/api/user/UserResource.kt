package api.user

import JwtUtil
import api.ErrorResponse
import api.GenericResource
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import service.UserService

@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
open class UserResource : GenericResource() {

    @Inject
    private lateinit var jwtUtil: JwtUtil

    @Inject
    private lateinit var userService: UserService

    @POST
    @Path("/auth/")
    open fun auth(user: UserDto) : Response {

        val username = user.username ?: return badRequest("Где username")
        val password = user.password ?: return badRequest("Где password")

        val authenticatedUser = userService.authenticate(username, password)
        if (authenticatedUser == null) {
            return unauthorized("Неверный пароль ввел, или юзернейм?")
        }

        val token = jwtUtil.generateToken(username)
        return Response.ok(mapOf("token" to token)).build()
    }

    @POST
    @Path("/register/")
    @Produces(MediaType.TEXT_PLAIN)
    open fun register(user : UserDto) : Response {

        val username = user.username ?: return badRequest("Где username")
        val password = user.password ?: return badRequest("Где password")

        return try {
            userService.register(username, password)
            Response.ok().entity("User registered successfully").build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.CONFLICT)
                .entity(ErrorResponse().error(e.message ?: "Registration failed"))
                .build()
        }
    }
}
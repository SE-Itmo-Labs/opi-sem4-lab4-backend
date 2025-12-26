package api.user

import JwtUtil
import api.GenericResource
import api.response.GeneralResponseBuilder
import jakarta.inject.Inject
import jakarta.json.Json
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
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

        val response = GeneralResponseBuilder()

        val authenticatedUser = userService.authenticate(username, password)
            ?: return unauthorized("Неверный пароль ввел, или юзернейм?")

        val token = jwtUtil.generateToken(username)

        response.add("token", token)

        return Response.ok(response.ok("Authorized successfully!")).build()
    }

    @POST
    @Path("/register/")
    open fun register(user : UserDto) : Response {

        val username = user.username ?: return badRequest("Где username")
        val password = user.password ?: return badRequest("Где password")

        return try {
            userService.register(username, password)
            ok("User registered successfully!")
        } catch (e: IllegalArgumentException) {
            conflict(e.message ?: "Registration failed")
        }
    }

    @DELETE
    @Path("/delete/")
    open fun delete(user : UserDto) : Response {
        return Response.ok().build()
    }
}
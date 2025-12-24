package api

import jakarta.ws.rs.core.Response

open class GenericResource {

    open fun badRequest(message: String): Response {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(ErrorResponse().error(message))
            .build()
    }

    open fun unauthorized(message: String): Response {
        return Response.status(Response.Status.UNAUTHORIZED).entity(message).build()
    }
}
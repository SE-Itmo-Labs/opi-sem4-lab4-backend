package api

import api.response.GeneralResponseBuilder
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.Response.ResponseBuilder

open class GenericResource {

    companion object {
        open fun ok(message: String) : Response {
            return generateSampleResponse(Response.Status.OK)
                .entity(GeneralResponseBuilder().ok(message))
                .build()
        }

        open fun badRequest(message: String): Response {
            return generateSampleResponse(Response.Status.BAD_REQUEST)
                .entity(GeneralResponseBuilder().error(message))
                .build()
        }

        open fun unauthorized(message: String): Response {
            return generateSampleResponse(Response.Status.UNAUTHORIZED)
                .entity(GeneralResponseBuilder()
                    .error(message))
                .build()
        }

        open fun conflict(message: String): Response {
            return generateSampleResponse(Response.Status.CONFLICT)
                .entity(GeneralResponseBuilder().error(message))
                .build()
        }

        protected open fun generateSampleResponse(status: Response.Status): ResponseBuilder {
            return Response.status(status)
        }
    }
}
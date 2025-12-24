package api.point

import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/point")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class PointResource {
    @POST
    @Path("/throw/")
    @Produces(MediaType.TEXT_PLAIN)
    fun throwPoint(pointRequest : PointDto) : Response {
        return Response.ok().entity(pointRequest).build()
    }

    @GET
    @Path("/test/")
    fun test() : Response {
        return Response.ok().entity("{'test': 123}").build()
    }
}
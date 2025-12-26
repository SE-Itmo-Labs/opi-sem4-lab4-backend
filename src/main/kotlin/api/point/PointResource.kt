package api.point

import JwtUtil
import api.GenericResource
import api.ProjectHTTPHeaders
import api.point.PointResourceUtil.Companion.buildJsonArray
import api.response.GeneralResponseBuilder
import coordinates.builders.Point2DRBuilder
import coordinates.exceptions.PointOutOfBoundariesException
import coordinates.geometry.Point2DR
import coordinates.validator.GeometryValidator
import database.model.DotType
import database.model.Point2DRow
import database.repositories.DBPointsRepository
import database.repositories.DBUserRepository
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.lang.IllegalArgumentException
import java.time.LocalDateTime

@Path("/point")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
open class PointResource : GenericResource() {

    @Inject
    private lateinit var pointsRepository: DBPointsRepository

    @Inject
    private lateinit var userRepository: DBUserRepository

    @Inject
    private lateinit var webSocket: PointWSResource

    @Inject
    private lateinit var jwtUtil: JwtUtil

    private val geometryValidator = GeometryValidator()

    @POST
    @Path("/throw/")
    @Produces(MediaType.TEXT_PLAIN)
    open fun throwPoint(pointRequest : PointDto) : Response {
        return Response.ok().entity(pointRequest).build()
    }

    @GET
    @Path("/test/")
    open fun test() : Response {
        return Response.ok().entity("{'test': 123}").build()
    }

    @POST
    @Path("/save/")
    open fun save(
        @Context headers: HttpHeaders,
        pointDto: PointDto
    ) : Response {
        val token = headers.getHeaderString(ProjectHTTPHeaders.AUTHORIZATION)

        val username = jwtUtil.getUsernameFromToken(token)
            ?: return unauthorized("Невалидный токен")

        val user = userRepository.findByUsername(username)
            ?: return unauthorized("Пользователь не найден")

        val responseBuilder = GeneralResponseBuilder()

        val point2DR = Point2DRBuilder.build(pointDto.x, pointDto.y, pointDto.R)

        var inArea = false

        val startTime = System.nanoTime()

        try {
            geometryValidator.validate(point2DR)
            inArea = true
        } catch (e: PointOutOfBoundariesException) {

        }
        catch (e: IllegalArgumentException) {
            return badRequest(e.message!!)
        }

        val endTime = System.nanoTime()
        val executionTime = (endTime - startTime)

        val pointRow = Point2DRow(
            user = user,
            type = DotType.SIMPLE,
            point2DR = point2DR,
            timestamp = LocalDateTime.now().plusHours(3),
            executionTime = executionTime,
            inArea = inArea
        )

        pointsRepository.save(pointRow)

        val point = ArrayList<Point2DRow>()
        point.add(pointRow)

        responseBuilder.add("points", buildJsonArray(point))
        val res = responseBuilder.ok("Point has been thrown")

        // Бродкастим тут
        webSocket.broadcastToUser(user.id!!, res.toString())

        return Response.ok(res).build()
    }

    @GET
    @Path("/all")
    open fun getAllPoints(): Response {
        val allPoints = pointsRepository.getAllPoints()
        val response = GeneralResponseBuilder()
        response.add("points", allPoints.size)
        response.add("data", buildJsonArray(allPoints))
        return Response.ok(response.ok("Here I am! Rock you like a Hurricane!")).build()
    }

    @GET
    @Path("/my")
    open fun getMyPoints(@Context headers: HttpHeaders): Response {
        val token = headers.getHeaderString(ProjectHTTPHeaders.AUTHORIZATION)

        val username = jwtUtil.getUsernameFromToken(token)
            ?: return unauthorized("Невалидный токен")

        val user = userRepository.findByUsername(username)
            ?: return unauthorized("Пользователь не найден")

        val myPoints = pointsRepository.findAllByUser(user.id!!)
        val response = GeneralResponseBuilder()
        response.add("points", myPoints.size)
        response.add("data", buildJsonArray(myPoints))

        return Response.ok(response.ok("All of your points here:)))")).build()
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    open fun deletePoint(
        @Context headers: HttpHeaders,
        @PathParam("id") id: Long
    ): Response {
        val token = headers.getHeaderString(ProjectHTTPHeaders.AUTHORIZATION)

        val username = jwtUtil.getUsernameFromToken(token)
            ?: return unauthorized("Невалидный токен")

        val user = userRepository.findByUsername(username)
            ?: return unauthorized("Пользователь не найден")

        val point = pointsRepository.findById(id, Point2DRow::class.java)
            ?: return badRequest("Точка не найдена")

        if (point.user.id != user.id) {
            return badRequest("Свои точки только можно удалять, ненене)")
        }

        val deleted = pointsRepository.deletePointById(id)
        if (!deleted) {
            return badRequest("Не удалось удалить точку")
        }

        return ok("Точка удалена")
    }

    @DELETE
    @Path("/my")
    open fun deleteAllMyPoints(@Context headers: HttpHeaders): Response {
        val token = headers.getHeaderString(ProjectHTTPHeaders.AUTHORIZATION)

        val username = jwtUtil.getUsernameFromToken(token)
            ?: return unauthorized("Невалидный токен")

        val user = userRepository.findByUsername(username)
            ?: return unauthorized("Пользователь не найден")

        pointsRepository.deleteAllByUser(user.id!!)
        return ok("All of your points have been deleted!")
    }
}
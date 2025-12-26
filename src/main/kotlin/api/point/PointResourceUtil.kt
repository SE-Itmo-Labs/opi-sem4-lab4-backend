package api.point

import database.model.Point2DRow
import jakarta.json.Json

open class PointResourceUtil {
    companion object {
        open fun buildJsonArray(points: List<Point2DRow>): jakarta.json.JsonArray {
            val arrayBuilder = Json.createArrayBuilder()
            for (p in points) {
                val pointObj = Json.createObjectBuilder()
                    .add("id", p.id ?: -1)
                    .add("x", p.point2DR.x.toDouble())
                    .add("y", p.point2DR.y.toDouble())
                    .add("R", p.point2DR.R.toDouble())
                    .add("inArea", p.inArea)
                    .add("executionTime", p.executionTime)
                    .add("timestamp", p.formattedTimestamp)
                    .add("username", p.user.username)
                    .build()
                arrayBuilder.add(pointObj)
            }
            return arrayBuilder.build()
        }
    }
}
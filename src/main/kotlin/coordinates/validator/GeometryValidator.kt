package coordinates.validator

import coordinates.exceptions.PointOutOfBoundariesException
import coordinates.geometry.Point2DR
import java.lang.IllegalArgumentException
import kotlin.math.pow


class GeometryValidator {

    companion object {
        val acceptedRadius : Set<Float> = setOf(
            0.5f, 1f, 1.5f, 2f
        )
    }

    fun validate(point2DR: Point2DR) {

        if (point2DR.R !in acceptedRadius) {
            throw IllegalArgumentException("Invalid radius value. Accepted values are: ${acceptedRadius.joinToString(", ")}.")
        }

        if (point2DR.x < 0 && point2DR.y > 0) {

            if (point2DR.y > point2DR.x + point2DR.R) {
                throw PointOutOfBoundariesException("Point is outside the area in Quadrant II. Must satisfy: y <= x + R.")
            }
        }

        else if (point2DR.x > 0 && point2DR.y > 0) {

            if (point2DR.x > point2DR.R / 2 || point2DR.y > point2DR.R) {
                throw PointOutOfBoundariesException("Point is outside the area in Quadrant I. Must satisfy: x <= R/2 and y <= R.")
            }
        }

        else if (point2DR.x < 0 && point2DR.y < 0) {

            val distanceSquared = point2DR.x.toDouble().pow(2.0) + point2DR.y.toDouble().pow(2.0)
            val radiusSquared = (point2DR.R / 2).toDouble().pow(2.0)
            if (distanceSquared > radiusSquared) {
                throw PointOutOfBoundariesException("Point is outside the area in Quadrant III. Must satisfy: x^2 + y^2 <= (R/2)^2.")
            }
        }

        else if (point2DR.x == 0.0f && point2DR.y == 0.0f) {
            return
        } else {
            throw PointOutOfBoundariesException("Point is outside the defined area for Quadrant IV or on axes (except origin).")
        }
    }
}
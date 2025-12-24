package com.ssnagin.servlets.coordinates.validator

import com.ssnagin.servlets.coordinates.exceptions.PointOutOfBoundariesException
import com.ssnagin.servlets.coordinates.geometry.Point2DR
import kotlin.math.pow


class GeometryValidator {

    final fun validate(point2DR: Point2DR) {
        if (point2DR.x < 0 && point2DR.y > 0) throw PointOutOfBoundariesException()


        if (point2DR.x > 0 && point2DR.y < 0) {
            // y  = 0.5x - R/2
            if (point2DR.y < 0.5* point2DR.x - point2DR.R/2)
                throw PointOutOfBoundariesException()
        }

        // x = -R and y = R
        if (point2DR.x < -point2DR.R || point2DR.y < -point2DR.R/2)
            throw PointOutOfBoundariesException()

        // x^2 + y^2 = R^2
        if (point2DR.x >= 0 && point2DR.y >= 0) {
            if (
                point2DR.x.toDouble().pow(2.0) + point2DR.y.toDouble().pow(2.0)  > point2DR.R.toDouble().pow(2.0)
                )
                throw PointOutOfBoundariesException();
        }
    }
}
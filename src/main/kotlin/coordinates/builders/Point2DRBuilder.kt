package com.ssnagin.servlets.coordinates.builders

import com.ssnagin.servlets.coordinates.geometry.Point2DR
import java.math.BigDecimal

object Point2DRBuilder {
    @Throws(NumberFormatException::class)
    fun build(xObj: Any?, yObj: Any?, RObj: Any?): Point2DR {
        val x = BigDecimal(xObj.toString())
        val y = BigDecimal(yObj.toString())  // Fixed: was xObj.toString()
        val r = BigDecimal(RObj.toString())  // Fixed: was xObj.toString()

        if (y > BigDecimal("5")) {
            throw NumberFormatException("!!")
        }

        return Point2DR(
            x = x.toFloat(),
            y = y.toFloat(),
            R = r.toFloat()
        )
    }
}
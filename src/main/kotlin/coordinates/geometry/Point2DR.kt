package com.ssnagin.servlets.coordinates.geometry

data class Point2DR(
    val x: Float,
    val y: Float,
    val R: Float
) {
    constructor() : this(0.0f,0.0f,0.0f)
}

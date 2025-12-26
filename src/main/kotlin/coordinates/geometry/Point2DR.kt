package coordinates.geometry

import jakarta.persistence.Embeddable

@Embeddable
open class Point2DR(
    var x: Float,
    var y: Float,
    var R: Float,
) {
    constructor() : this(0.0f, 0.0f, 0.0f)
}
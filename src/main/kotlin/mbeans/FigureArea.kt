package mbeans

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import jakarta.enterprise.context.ApplicationScoped
import java.lang.management.ManagementFactory
import javax.management.ObjectName
import kotlin.math.PI
import kotlin.math.abs

@ApplicationScoped
open class FigureArea : FigureAreaMBean {

    private val points = mutableListOf<Pair<Double, Double>>()

    override val currentArea: Double
        get() = calculateConvexHullArea()

    @PostConstruct
    open fun init() {
        val mbs = ManagementFactory.getPlatformMBeanServer()
        val name = ObjectName("lab4.mbeans:type=FigureArea")
        if (!mbs.isRegistered(name)) {
            mbs.registerMBean(this, name)
        }
    }

    @PreDestroy
    open fun cleanup() {
        val mbs = ManagementFactory.getPlatformMBeanServer()
        val name = ObjectName("lab4.mbeans:type=FigureArea")
        if (mbs.isRegistered(name)) mbs.unregisterMBean(name)
    }

    open fun addPoint(x: Double, y: Double) {
        points.add(Pair(x, y))
    }

    private fun calculateConvexHullArea(): Double {
        if (points.size < 3) return 0.0

        val sorted = points.sortedWith(compareBy({ it.first }, { it.second })).distinct()
        if (sorted.size < 3) return 0.0

        val lower = mutableListOf<Pair<Double, Double>>()
        for (p in sorted) {
            while (lower.size >= 2 && crossProduct(lower[lower.size - 2], lower.last(), p) <= 0) {
                lower.removeLast()
            }
            lower.add(p)
        }

        val upper = mutableListOf<Pair<Double, Double>>()
        for (p in sorted.reversed()) {
            while (upper.size >= 2 && crossProduct(upper[upper.size - 2], upper.last(), p) <= 0) {
                upper.removeLast()
            }
            upper.add(p)
        }

        lower.removeLast()
        upper.removeLast()
        val hull = lower + upper

        var area = 0.0
        for (i in hull.indices) {
            val j = (i + 1) % hull.size
            area += hull[i].first * hull[j].second - hull[j].first * hull[i].second
        }
        return abs(area) / 2.0
    }

    private fun crossProduct(o: Pair<Double, Double>, a: Pair<Double, Double>, b: Pair<Double, Double>): Double {
        return (a.first - o.first) * (b.second - o.second) - (a.second - o.second) * (b.first - o.first)
    }
}
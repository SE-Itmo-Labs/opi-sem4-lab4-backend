package mbeans

interface FigureAreaMBean {
    val currentArea: Double

    fun reset()
}
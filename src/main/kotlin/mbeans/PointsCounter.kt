package mbeans

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import jakarta.enterprise.context.ApplicationScoped
import java.lang.management.ManagementFactory
import javax.management.Notification
import javax.management.NotificationBroadcasterSupport
import javax.management.ObjectName

@ApplicationScoped
open class PointsCounter : NotificationBroadcasterSupport(), PointsCounterMBean {

    private var _totalPoints: Int = 0
    private var _hitPoints: Int = 0
    private var sequenceNumber: Long = 0

    override val totalPoints: Int
        get() = _totalPoints

    override val hitPoints: Int
        get() = _hitPoints

    @PostConstruct
    open fun init() {
        val mbs = ManagementFactory.getPlatformMBeanServer()
        val name = ObjectName("lab4.mbeans:type=PointsCounter")
        if (!mbs.isRegistered(name)) {
            mbs.registerMBean(this, name)
        }
    }

    @PreDestroy
    open fun cleanup() {
        val mbs = ManagementFactory.getPlatformMBeanServer()
        val name = ObjectName("lab4.mbeans:type=PointsCounter")
        if (mbs.isRegistered(name)) mbs.unregisterMBean(name)
    }

    open fun addPoint(isHit: Boolean) {
        _totalPoints++
        if (isHit) _hitPoints++

        if (_totalPoints % 15 == 0) {
            val notification = Notification(
                "lab4.points.milestone",
                this.javaClass.name,
                sequenceNumber++,
                System.currentTimeMillis(),
                "Количество установленных пользователем точек стало кратно 15! Текущее количество: $_totalPoints"
            )
            sendNotification(notification)
        }
    }
}
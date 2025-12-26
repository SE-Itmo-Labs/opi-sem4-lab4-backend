package database.repositories

import database.JpaRepository
import database.model.Point2DRow
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional


@ApplicationScoped
open class DBPointsRepository : JpaRepository<Point2DRow, Long>() {

    @PersistenceContext(unitName = "default")
    override lateinit var entityManager: EntityManager

    open fun findAllByUser(userId: Long): List<Point2DRow> =
        entityManager.createQuery("SELECT p FROM Point2DRow p WHERE p.user.id = :userId", Point2DRow::class.java)
            .setParameter("userId", userId)
            .resultList

    @Transactional
    open fun getAllPoints(): List<Point2DRow> {
        return entityManager.createQuery("SELECT p FROM Point2DRow p ORDER BY p.timestamp DESC", Point2DRow::class.java)
            .resultList
    }

    @Transactional
    open fun deleteAllByUser(userId: Long) =
        entityManager.createQuery("DELETE FROM Point2DRow p WHERE p.user.id = :userId")
            .setParameter("userId", userId)
            .executeUpdate()

    @Transactional
    open fun removePoint(point: Point2DRow) {
        entityManager.remove(point)
    }

    @Transactional
    open fun deletePointById(id: Long): Boolean {
        val point = entityManager.find(Point2DRow::class.java, id)
        if (point != null) {
            entityManager.remove(point)
            return true
        }
        return false
    }
}
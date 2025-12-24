package database

import database.model.Point2DRow
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional


@ApplicationScoped
open class DBService {

    @field:PersistenceContext(unitName = "default")
    private lateinit var em: EntityManager

    @Transactional
    open fun savePoint(point: Point2DRow) {
        em.persist(point)
    }

    open fun getAllPoints(): List<Point2DRow> {
        return em.createQuery("SELECT p FROM Point2DRow p ORDER BY p.timestamp DESC", Point2DRow::class.java)
            .resultList
    }

    @Transactional
    open fun deleteAllPoints() {
        em.createQuery("DELETE FROM Point2DRow").executeUpdate()
    }
}
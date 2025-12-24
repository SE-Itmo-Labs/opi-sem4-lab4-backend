package database


import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional

abstract class JpaRepository<T : Any, ID : Any> {

    @PersistenceContext
    protected open lateinit var entityManager: EntityManager

    @Transactional
    open fun save(entity: T) {entityManager.persist(entity)}

    open fun findById(id: ID, clazz: Class<T>): T? = entityManager.find(clazz, id)

    open fun findAll(clazz: Class<T>): List<T> =
        entityManager.createQuery("SELECT e FROM ${clazz.simpleName} e", clazz).resultList
}
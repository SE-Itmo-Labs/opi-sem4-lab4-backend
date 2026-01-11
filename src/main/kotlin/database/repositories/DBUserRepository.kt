package database.repositories

import database.JpaRepository
import database.model.User
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext

@ApplicationScoped
open class DBUserRepository : JpaRepository<User, Long>() {

    @PersistenceContext(unitName = "default")
    override lateinit var entityManager: EntityManager

    open fun findByUsername(username: String): User? {
        return entityManager.createQuery(
            "SELECT u FROM User u WHERE u.username = :username", User::class.java
        )
            .setParameter("username", username)
            .resultList
            .firstOrNull()
    }

    open fun deleteByUser(user: User) {
        entityManager.remove(user)
    }
}
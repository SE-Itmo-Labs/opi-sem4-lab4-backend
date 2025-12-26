package service

import database.model.User
import database.repositories.DBPointsRepository
import database.repositories.DBUserRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import utils.crypt.PasswordEncoder

@ApplicationScoped
open class UserService {

    @Inject
    private lateinit var userRepository: DBUserRepository

    @Inject
    private lateinit var pointsRepository: DBPointsRepository

    @Inject
    private lateinit var passwordEncoder: PasswordEncoder

    @Transactional
    open fun register(username: String, rawPassword: String): User {

        if (username.isBlank() || rawPassword.isBlank())
            throw IllegalArgumentException(
                "Имя пользователя и пароль должны быть непустыми"
            )

        if (userRepository.findByUsername(username) != null)
            throw IllegalArgumentException(
                "Пользователь с таким юзернеймом уже есть, думайте"
            )

        val hashedPassword = passwordEncoder.hash(rawPassword)

        val user = User(username = username, password = hashedPassword)
        userRepository.save(user)
        return user
    }

    open fun authenticate(username: String, rawPassword: String): User? {
        val user = userRepository.findByUsername(username)
        if (
            user != null &&
            passwordEncoder.verify(rawPassword, user.password)
            ) {
            return user
        }
        return null
    }

    @Transactional
    open fun deleteUser(username: String): Boolean {
        val user = userRepository.findByUsername(username) ?: return false

        pointsRepository.deleteAllByUser(user.id!!)

        userRepository.deleteByUser(user)

        return true
    }
}
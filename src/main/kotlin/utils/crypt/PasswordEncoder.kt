package utils.crypt

import at.favre.lib.crypto.bcrypt.BCrypt
import jakarta.enterprise.context.ApplicationScoped
import java.nio.charset.StandardCharsets

@ApplicationScoped
open class PasswordEncoder {

    open fun hash(password: String): String {
        val hasher = BCrypt.withDefaults();
        val bytes = hasher.hash(12, password.toCharArray())

        return String(bytes, StandardCharsets.UTF_8)
    }

    open fun verify(password: String, hashedPassword: String): Boolean {
        return BCrypt
            .verifyer()
            .verify(password.toCharArray(), hashedPassword.toCharArray())
            .verified
    }
}
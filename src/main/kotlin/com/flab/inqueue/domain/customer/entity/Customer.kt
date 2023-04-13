package com.flab.inqueue.domain.customer.entity

import com.flab.inqueue.security.hmacsinature.utils.SecretKeyCipher
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity(name = "CUSTOMER")
class Customer(
    var name: String,
    val clientId: String,
    var clientSecret: String,
    @ElementCollection
    @CollectionTable(
        name = "CUSTOMER_ROLE",
        joinColumns = [JoinColumn(name = "customer_id")]
    )
    @Column(name = "role")
    val roles: List<Role>
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    val createdAt: LocalDateTime = LocalDateTime.now()

    companion object {
        @JvmStatic
        fun admin(name: String, clientId: String, clientSecret: String): Customer {
            return Customer(name, clientId, clientSecret, listOf(Role.USER, Role.ADMIN))
        }

        @JvmStatic
        fun user(name: String, clientId: String, clientSecret: String): Customer {
            return Customer(name, clientId, clientSecret, listOf(Role.USER))
        }
    }

    fun encryptClientSecret(secretKeyCipher: SecretKeyCipher) {
        this.clientSecret = secretKeyCipher.encrypt(clientSecret)
    }
}
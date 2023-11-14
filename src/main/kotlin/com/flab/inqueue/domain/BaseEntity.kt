package com.flab.inqueue.domain

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@EntityListeners(AuditingEntityListener::class)
@MappedSuperclass
abstract class BaseEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
) {
    @CreatedDate
    var createdDateTime: LocalDateTime? = null

    @LastModifiedDate
    var modifiedDateTime: LocalDateTime? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseEntity

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

}
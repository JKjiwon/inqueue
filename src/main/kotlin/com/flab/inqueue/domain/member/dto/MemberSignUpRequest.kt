package com.flab.inqueue.domain.member.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class MemberSignUpRequest(

    @field:NotBlank
    @field:Size(min = 1, max = 20)
    val name: String? = null,
    val phone: String? = null
)

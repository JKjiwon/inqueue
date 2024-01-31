package com.flab.inqueue.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val httpStatus: HttpStatus,
    val code: Int,
    val message: String
) {
    OK(HttpStatus.OK, 200, "OK"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, 400, "Bad Request"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, 401, "Unauthorized"),
    FORBIDDEN(HttpStatus.FORBIDDEN, 403, "Forbidden"),
    NOT_FOUND(HttpStatus.NOT_FOUND, 404, "Not Found"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 500, "Internal Server Error"),

    // validation
    REQUEST_VALIDATION_ERROR(HttpStatus.PRECONDITION_FAILED, 1000, "Request value is not valid"),

    // member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, 2000, "Member is not found"),

    // event
    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, 3000, "Event is not found"),
    EVENT_NOT_ACCESSED(HttpStatus.BAD_REQUEST, 3001, "Event is not accessed"),

    // job
    JOB_NOT_FOUND_IN_JOB_QUEUE(HttpStatus.NOT_FOUND, 4000, "Job is not found in job queue");
}
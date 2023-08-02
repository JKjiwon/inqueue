package com.flab.inqueue.domain.event.exception

import com.flab.inqueue.application.common.ApplicationException
import org.springframework.http.HttpStatus

abstract class EventException(httpStatus: HttpStatus = HttpStatus.BAD_REQUEST, message: String) :
    ApplicationException(httpStatus, message)
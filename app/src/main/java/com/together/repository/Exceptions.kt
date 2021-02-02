package com.together.repository


class NoExternalException : Throwable()

class AlreadyPlaceOrder(
    msg: String = "Already placed an order for this day.",
    externalCause: Throwable = NoExternalException()) : Throwable(msg,externalCause)
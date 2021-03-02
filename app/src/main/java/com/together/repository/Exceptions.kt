package com.together.repository


class NoExternalException : Throwable()

class NeutralException(val ignoreMe: Boolean = true) : Throwable()

class AlreadyPlaceOrder(
    msg: String = "Already placed an order for this day.",
    externalCause: Throwable = NoExternalException()) : Throwable(msg,externalCause)

class NoInternetConnection(
    msg: String = "No internet connection.",
    externalCause: Throwable = NoExternalException()) : Throwable(msg,externalCause)
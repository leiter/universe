package com.together.utils

fun String.valid() : Boolean {
    val trimmed = this.trim()
    return (trimmed.isNullOrEmpty() || this.isBlank() )
}

fun validString(input : String ) : Boolean {

    return (!input.isNullOrEmpty() || input.isNotBlank())
}
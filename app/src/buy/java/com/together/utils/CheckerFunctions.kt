package com.together.utils


fun String.doesNotFit(minLength: Int = 4) : Boolean {
    return (this.isNullOrEmpty()|| this.isBlank() || this.length < minLength )
}
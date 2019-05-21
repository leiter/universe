package com.together.app

import io.reactivex.subjects.PublishSubject

object MainMessagePipe {

    val mainThreadMessage : PublishSubject<Any> = PublishSubject.create()

}
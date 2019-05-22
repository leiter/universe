package com.together.app

import io.reactivex.subjects.PublishSubject

object MainMessagePipe {

    val mainThreadMessage : PublishSubject<in BaseModel> = PublishSubject.create()

}
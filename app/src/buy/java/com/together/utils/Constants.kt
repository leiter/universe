package com.together.utils

import android.text.method.DigitsKeyListener


val digitsWithComma = DigitsKeyListener.getInstance("0123456789,")
val digitsWithOutComma = DigitsKeyListener.getInstance("0123456789")
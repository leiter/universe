package com.together.utils

import java.io.File

object FileUtils {

    @JvmStatic
    fun deleteFile(file: File) : Boolean{
        if (file.exists()){
            return file.delete()
        }
        return false
    }


}
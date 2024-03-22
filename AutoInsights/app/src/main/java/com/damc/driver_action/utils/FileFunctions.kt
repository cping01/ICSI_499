package com.damc.driver_action.utils

import android.content.Context
import android.os.Build
import android.os.Build.VERSION
import android.os.Environment
import java.io.File
import java.io.IOException


class FileFunctions private constructor() {
    @Throws(IOException::class)
    fun makeFolder(context: Context): Boolean {
        var parFolder = false
        val parentFolder: File
        parentFolder = if (VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            File(context.externalCacheDir.toString() + "/Driver")
        } else {
            File(Environment.getExternalStorageDirectory().toString() + "/Driver")
        }
        parFolder = parentFolder.exists() || parentFolder.mkdir()
        println("$parentFolder created : $parFolder")
        return parFolder
    }

    fun makeDir(filePath: String, dir: String, context: Context): Boolean {
        var creFile = false
        val file: File
        file = if (VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            File(context.externalCacheDir.toString() + "/" + dir + "/" + filePath)
        } else {
            File(Environment.getExternalStorageDirectory().toString() + "/" + dir + "/" + filePath)
        }
        if (!file.exists()) {
            try {
                creFile = file.mkdir()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            creFile = true
        }
        return creFile
    }

    fun makeSubDir(dir1: String, dir2: String, subFile: String, context: Context): Boolean {
        var creFile = false
        val file: File
        file = if (VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            File(context.externalCacheDir.toString() + "/" + dir1 + "/" + dir2 + "/" + subFile)
        } else {
            File(
                Environment.getExternalStorageDirectory()
                    .toString() + "/" + dir1 + "/" + dir2 + "/" + subFile
            )
        }
        if (!file.exists()) {
            try {
                creFile = file.mkdirs()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            creFile = true
        }
        return creFile
    }

    @Throws(IOException::class)
    fun makeFile(filePath: String?): Boolean {
        var creFile = false
        val file = File(filePath)
        if (!file.exists()) {
            try {
                creFile = file.createNewFile()
            } catch (e: IOException) {
                //makeErrorLog(LogFileCreator.makeErrorText(e, "LOGFILE CREATOR | makeInfoLog - createNewFile"));
            }
        }
        return creFile
    }

    fun getFile(filePath: String?): File? {
        val file = File(filePath)
        return if (file.exists()) {
            file
        } else {
            null
        }
    }

    fun deleteFile(filePath: String?): Boolean {
        var delFile = false
        val file = File(filePath)
        if (file.exists()) {
            delFile = file.delete()
        }
        return delFile
    }

    fun deleteDirectoryContent(directory: String?) {
        val dir = File(directory)
        if (dir.exists()) {
            val children = dir.list()
            for (i in children.indices) {
                File(dir, children[i]).delete()
            }
            dir.delete()
        }
    }

    fun deleteDirectory(dir: File?) {
        if (dir != null) {
            if (dir.isDirectory) {
                val children = dir.list()
                for (i in children.indices) {
                    File(dir, children[i]).delete()
                }
            }
        }
    }

    @Throws(IOException::class)
    fun deleteDirectory(dirPath: String?): Boolean {
        var del = false
        val file = File(dirPath)
        if (file.isDirectory) {
            for (child in file.listFiles()) {
                deleteDirectory(child.absolutePath)
            }
        }
        del = file.delete()
        return del
    }

    @Throws(IOException::class)
    fun deleteFilesInDirectory(dirPath: String?) {
        val file = File(dirPath)
        if (file.isDirectory) {
            for (child in file.listFiles()) {
                deleteFilesInDirectory(child.absolutePath)
            }
        }
    }

    fun listFileNames(dir: String?): Array<String>? {
        return try {
            val file = File(dir)
            if (file.isDirectory) {
                file.list()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }






    companion object {
        private var theInstance: FileFunctions? = null

        @get:Synchronized
        val instance: FileFunctions?
            get() {
                if (theInstance == null) {
                    theInstance = FileFunctions()
                }
                return theInstance
            }

        @Throws(IOException::class)
        fun deleteFile(file: File): Boolean {
            var delFile = false
            delFile = file.delete()
            return delFile
        }
    }
}

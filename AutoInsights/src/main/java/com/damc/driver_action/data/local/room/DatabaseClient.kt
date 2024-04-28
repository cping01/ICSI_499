package com.damc.driver_action.data.local.room

import android.content.Context
import androidx.room.Room.databaseBuilder

class DatabaseClient(mCtx: Context?) {

    private var appDatabase: AppDataBase? = null

    init {
        appDatabase = mCtx?.let {
            databaseBuilder(it.applicationContext, AppDataBase::class.java, "zero_one_tech.db")
                .fallbackToDestructiveMigration()
                .build()
        }
    }


    @Synchronized
    fun getAppDatabase(): AppDataBase? {
        return appDatabase
    }
}
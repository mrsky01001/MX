package com.mx.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mx.app.data.model.MXLocation

@Database(
    entities = [MXLocation::class],
    version = 1,
    exportSchema = false
)
abstract class MXDatabase : RoomDatabase() {

    abstract fun locationDao(): LocationDao

    companion object {
        @Volatile
        private var INSTANCE: MXDatabase? = null

        fun getInstance(context: Context): MXDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MXDatabase::class.java,
                    "mx_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

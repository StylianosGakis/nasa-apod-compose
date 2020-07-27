package com.stylianosgakis.composenasapotd.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.stylianosgakis.composenasapotd.model.NasaPhoto

@Database(
    entities = [NasaPhoto::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun nasaPhotoDao(): NasaPhotoDao

    companion object {
        const val name = "AppDatabase"
    }
}
package com.stylianosgakis.composenasapotd.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.stylianosgakis.composenasapotd.model.NasaPhoto

@Dao
interface NasaPhotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNasaPhoto(nasaPhoto: NasaPhoto)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNasaPhotos(nasaPhotos: List<NasaPhoto>)

    @Query(
        """SELECT *
            FROM NasaPhoto 
            ORDER BY date DESC"""
    )
    suspend fun getAllPhotos(): List<NasaPhoto>


}
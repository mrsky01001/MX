package com.mx.app.data.database

import androidx.room.*
import com.mx.app.data.model.MXLocation
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {

    @Query("SELECT * FROM locations ORDER BY timestamp DESC")
    fun getAll(): Flow<List<MXLocation>>

    @Query("SELECT * FROM locations ORDER BY timestamp DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<MXLocation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: MXLocation): Long

    @Query("DELETE FROM locations")
    suspend fun deleteAll()
}

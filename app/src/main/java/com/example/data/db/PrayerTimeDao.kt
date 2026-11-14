package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.models.CachedPrayerTimesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerTimeDao {

    @Query("SELECT * FROM cached_prayer_times WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getPrayerTimesForDate(dateKey: String): CachedPrayerTimesEntity?

    @Query("SELECT * FROM cached_prayer_times WHERE dateKey = :dateKey LIMIT 1")
    fun observePrayerTimesForDate(dateKey: String): Flow<CachedPrayerTimesEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayerTimes(entity: CachedPrayerTimesEntity)
}

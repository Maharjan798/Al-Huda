package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.models.LastReadPositionEntity
import com.example.data.models.QuranBookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranBookmarkDao {

    @Query("SELECT * FROM quran_bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<QuranBookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM quran_bookmarks WHERE id = :id)")
    suspend fun isBookmarked(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBookmark(bookmark: QuranBookmarkEntity)

    @Query("DELETE FROM quran_bookmarks WHERE id = :id")
    suspend fun removeBookmark(id: String)

    @Query("SELECT * FROM last_read_position WHERE id = 1")
    fun getLastReadPosition(): Flow<LastReadPositionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLastReadPosition(position: LastReadPositionEntity)
}

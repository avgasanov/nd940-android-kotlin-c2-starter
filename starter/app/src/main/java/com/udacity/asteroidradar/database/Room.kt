package com.udacity.asteroidradar.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import com.udacity.asteroidradar.Asteroid


// Udacity dev bytes - Kotlin Android Developer Course
@Dao
interface AsteroidDao {
    @Query("select * from databaseasteroid")
    fun getSavedAsteroids(): LiveData<List<DatabaseAsteroid>>

    @Query("select * from databaseasteroid where strftime('%Y-%m-%d',closeApproachDate) >= strftime('%Y-%m-%d','now')")
    fun getOnwardsAsteroids(): LiveData<List<DatabaseAsteroid>>

    @Query("select * from databaseasteroid where strftime('%Y-%m-%d',closeApproachDate) = strftime('%Y-%m-%d','now')")
    fun getTodayAsteroids(): LiveData<List<DatabaseAsteroid>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg asteroids: DatabaseAsteroid)
}

@Database(entities = [DatabaseAsteroid::class], version = 1)
abstract class AsteroidsDatabase : RoomDatabase() {
    abstract val asteroidDao: AsteroidDao
}

private lateinit var INSTANCE: AsteroidsDatabase

fun getDatabase(context: Context): AsteroidsDatabase {
    synchronized(AsteroidsDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
                AsteroidsDatabase::class.java,
                "asteroids").build()
        }
    }
    return INSTANCE
}
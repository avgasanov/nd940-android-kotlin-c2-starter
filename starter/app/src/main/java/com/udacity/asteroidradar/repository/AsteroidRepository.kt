package com.udacity.asteroidradar.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.AsteroidsApplication
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.database.AsteroidsDatabase
import com.udacity.asteroidradar.database.asDomainModel
import com.udacity.asteroidradar.network.API_DATE_FORMAT
import com.udacity.asteroidradar.network.NasaApi
import com.udacity.asteroidradar.network.asDatabaseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.IllegalArgumentException
import java.text.SimpleDateFormat
import java.util.*

class AsteroidRepository(private val database: AsteroidsDatabase) {

    /**
     * Shared preferences keys
     * */
    val PICTURE_OF_THE_DAY = "picture_of_the_day"
    val PICTURE_OF_THE_DAY_DESC = "picture_of_the_day_desc"
    val DEFAULT_SHARED = "default_shared"


    val sharedPreferences = AsteroidsApplication.INSTANCE
        .getSharedPreferences(DEFAULT_SHARED, Context.MODE_PRIVATE)

    private val _status: MutableLiveData<Status> = MutableLiveData(Status.LOADING_COMPLETED)
    val status: LiveData<Status>
        get() = _status

    private val _pictureOfDay = MutableLiveData<String>()
    val pictureOfDay: LiveData<String>
        get() = _pictureOfDay

    private val _pictureOfDayDesc = MutableLiveData<String>(
        AsteroidsApplication.INSTANCE.getString(R.string.no_picture))
    val pictureOfDayDesc: LiveData<String>
        get() = _pictureOfDayDesc

    private val _queryType: MutableLiveData<QueryType> = MutableLiveData(QueryType.ONE_WEEK)
    val queryType: LiveData<QueryType>
        get() = _queryType

    val asteroids: LiveData<List<Asteroid>> =
        Transformations.switchMap(queryType) { type ->
            when(type) {
                QueryType.TODAY -> Transformations.map(database.asteroidDao.getTodayAsteroids()) {
                    it.asDomainModel()
                }
                QueryType.ONE_WEEK -> Transformations.map(database.asteroidDao.getOnwardsAsteroids()) {
                    it.asDomainModel()
                }
                QueryType.SAVED -> Transformations.map(database.asteroidDao.getSavedAsteroids()) {
                    it.asDomainModel()
                }
                else -> throw IllegalArgumentException("incompatible query type")
            }
        }

    fun switchQuery(type: QueryType) {
        _queryType.value = type
    }

    suspend fun refreshAsteroids() {
        _status.value = Status.LOADING
        val today = Calendar.getInstance().time
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        val sevenDaysAfter = calendar.time
        val dateFormat = SimpleDateFormat(API_DATE_FORMAT, Locale.US)
        withContext(Dispatchers.IO) {
            try {
                val asteroidsDTO = NasaApi.retrofitService
                    .getCloseAsteroidsByDatesAsync(
                        startDate = dateFormat.format(today),
                        endDate = dateFormat.format(sevenDaysAfter)
                    ).await()
                database.asteroidDao.insertAll(*asteroidsDTO.asDatabaseModel())

                val pictureDTO = NasaApi.retrofitService.getAsteroidOfTheDayAsync().await()
                if(pictureDTO.mediaType == "image") {
                    sharedPreferences.edit().putString(PICTURE_OF_THE_DAY, pictureDTO.url)
                        .putString(PICTURE_OF_THE_DAY_DESC, pictureDTO.title).commit()
                } else {
                    Log.v("AsteroidRepository", "Today media type is not image")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _status.value = Status.ERROR
                }
                e.printStackTrace()
            }
        }
        _pictureOfDay.value = sharedPreferences.getString(PICTURE_OF_THE_DAY, null)
        _pictureOfDayDesc.value = sharedPreferences.getString(PICTURE_OF_THE_DAY_DESC, null) ?:
                AsteroidsApplication.INSTANCE.getString(R.string.no_picture)
        _status.value = Status.LOADING_COMPLETED
    }

    enum class QueryType() { SAVED, ONE_WEEK, TODAY }
    enum class Status() { LOADING, LOADING_COMPLETED, ERROR }
}
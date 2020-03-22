package com.udacity.asteroidradar.main

import android.app.Application
import androidx.lifecycle.*
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.repository.AsteroidRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)


    private val database = getDatabase(application)
    private val asteroidsRepository = AsteroidRepository(database)

    init {
        refresh()
    }

    val asteroids = asteroidsRepository.asteroids
    val pictureOfDayUrl = asteroidsRepository.pictureOfDay
    val pictureOfDayDesc = asteroidsRepository.pictureOfDayDesc
    val status = asteroidsRepository.status

    private val _navigateToDetailsScreen = MutableLiveData<Asteroid>()
    val navigateToDetailsScreen: LiveData<Asteroid>
        get() = _navigateToDetailsScreen

    fun doneNavigating() {
        _navigateToDetailsScreen.value = null
    }

    fun onAsteroidClicked(asteroid: Asteroid) {
        _navigateToDetailsScreen.value = asteroid
    }

    fun onSwitchQuery(type: AsteroidRepository.QueryType) {
        asteroidsRepository.switchQuery(type)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun refresh() {
        viewModelScope.launch {
            asteroidsRepository.refreshAsteroids()
        }
    }

    // devbytes lesson (udacity kotlin android developer course)
    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}
package com.udacity.asteroidradar.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.PictureOfDay
import kotlinx.coroutines.Deferred
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Real estate - udacity Kotlin Android Developer Course
private const val BASE_URL = "https://api.nasa.gov/"

const val API_KEY = "****" // TODO place your api key here


const val API_DATE_FORMAT = "yyyy-MM-dd"

const val NEAR_EARTH_OBJECT = "near_earth_objects"
const val ID = "id"
const val NAME = "name"
const val ABSOLUTE_MAGNITUDE_H = "absolute_magnitude_h"
const val ESTIMATED_DIAMETER = "estimated_diameter"
const val KILOMETERS = "kilometers"
const val ESTIMATED_DIAMETER_MAX = "estimated_diameter_max"
const val IS_POTENTIALLY_HAZARDOUS = "is_potentially_hazardous_asteroid"
const val CLOSE_APPROACH_DATA = "close_approach_data"
const val CLOSE_APPROACH_DATE = "close_approach_date"
const val RELATIVE_VELOCITY = "relative_velocity"
const val KILOMETERS_PER_SECOND = "kilometers_per_second"
const val MISS_DISTANCE = "miss_distance"
const val ASTRONOMICAL = "astronomical"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .baseUrl(BASE_URL)
    .build()

interface NasaApiService {

    @GET("neo/rest/v1/feed")
    fun getCloseAsteroidsByDatesAsync(@Query("start_date") startDate: String,
                                      @Query("end_date") endDate: String,
                                      @Query("api_key") apiKey: String = API_KEY): Deferred<AsteroidsResponse>

    @GET("planetary/apod")
    fun getAsteroidOfTheDayAsync(@Query("api_key") apiKey: String = API_KEY): Deferred<PictureOfDay>
}

object NasaApi {
    val retrofitService: NasaApiService by lazy { retrofit.create(NasaApiService::class.java) }
}


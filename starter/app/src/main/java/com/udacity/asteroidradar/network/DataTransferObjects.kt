package com.udacity.asteroidradar.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.database.DatabaseAsteroid

@JsonClass(generateAdapter = true)
data class AsteroidsResponse(@Json(name = "near_earth_objects") val nearEarthObjects: Map<String,List<NearEarthObject>>)

/**
 * Videos represent a devbyte that can be played.
 */
@JsonClass(generateAdapter = true)
data class NearEarthObject(
    val id: Long,
    val name: String,
    @Json(name = "absolute_magnitude_h") val absoluteMagnitude: Double,
    @Json(name = "close_approach_data") val closeApproachData: List<CloseApproachData>,
    @Json(name = "estimated_diameter") val estimatedDiameter: EstimatedDiameter,
    @Json(name = "is_potentially_hazardous_asteroid") val isHazardous: Boolean)


@JsonClass(generateAdapter = true)
data class CloseApproachData(
    @Json(name = "close_approach_date") val closeApproachDate: String,
    @Json(name = "relative_velocity") val relativeVelocity: RelativeVelocity,
    @Json(name = "miss_distance") val missDistance: MissDistance
)

@JsonClass(generateAdapter = true)
data class RelativeVelocity(
    @Json(name = "kilometers_per_second") val kilometersPerSecond: Double
)

@JsonClass(generateAdapter = true)
data class MissDistance(
    val astronomical: Double
)

@JsonClass(generateAdapter = true)
data class EstimatedDiameter(
    val kilometers: Kilometers
)

@JsonClass(generateAdapter = true)
data class Kilometers(
    @Json(name = "estimated_diameter_max") val estimatedDiameterMax: Double
)

/**
 * Convert Network results to database objects
 */
fun AsteroidsResponse.asDomainModel(): List<Asteroid> {
    val asteroids = ArrayList<Asteroid>()
    nearEarthObjects.values.forEach() { nearEarthObjects ->
        nearEarthObjects.forEach() { asteroid ->
            with(asteroid) {
                val lastCloseApproach = closeApproachData.last()
                val domainAsteroid = Asteroid(
                    id = id,
                    codename = name,
                    closeApproachDate = (lastCloseApproach.closeApproachDate),
                    absoluteMagnitude = absoluteMagnitude,
                    estimatedDiameter = estimatedDiameter.kilometers.estimatedDiameterMax,
                    relativeVelocity = lastCloseApproach.relativeVelocity.kilometersPerSecond,
                    distanceFromEarth = lastCloseApproach.missDistance.astronomical,
                    isPotentiallyHazardous = isHazardous
                )
                asteroids.add(domainAsteroid)
            }
        }
    }
    return asteroids
}


fun AsteroidsResponse.asDatabaseModel(): Array<DatabaseAsteroid> {
    val asteroids = ArrayList<DatabaseAsteroid>()
    nearEarthObjects.values.forEach() { nearEarthObjects ->
        nearEarthObjects.forEach() { asteroid ->
            with(asteroid) {
                val lastCloseApproach = closeApproachData.last()
                val databaseAsteroid = DatabaseAsteroid(
                    id = id,
                    codename = name,
                    closeApproachDate = (lastCloseApproach.closeApproachDate),
                    absoluteMagnitude = absoluteMagnitude,
                    estimatedDiameter = estimatedDiameter.kilometers.estimatedDiameterMax,
                    relativeVelocity = lastCloseApproach.relativeVelocity.kilometersPerSecond,
                    distanceFromEarth = lastCloseApproach.missDistance.astronomical,
                    isPotentiallyHazardous = isHazardous
                )
                asteroids.add(databaseAsteroid)
            }
        }
    }
    return asteroids.toTypedArray()
}
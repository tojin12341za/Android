/*
 * Copyright (c) 2020 DuckDuckGo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duckduckgo.app.quickactions.api

import android.content.Intent
import android.location.Location
import android.net.Uri
import com.duckduckgo.app.autocomplete.api.AutoComplete.AutoCompleteSuggestion.QuickAnswerSuggestion
import com.duckduckgo.app.autocomplete.api.AutoComplete.AutoCompleteSuggestion.QuickAnswerSuggestion.IntentSuggestion
import com.duckduckgo.app.global.AppUrl
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query
import timber.log.Timber
import javax.inject.Inject

// https://duckduckgo.com/local.js?q=pizza%20hut%20nearby&tg=maps_places&rt=D&mkexp=b&strict_bbox=0
interface PlacesQuickActionService {

    @GET("${AppUrl.Url.API}/local.js")
    fun places(
        @Query("q") query: String,
        @Query("tg") tg: String = "maps_places",
        @Query("rt") rt: String = "D",
        @Query("mkexp") mkexp: String = "b",
        @Query("strict_bbox") strictBbox: String = "0"
    ): Observable<PlacesServiceRawResult>
}

data class PlacesServiceRawResult(val results: List<Place>, val geoip_lat: Double, val geoip_lon: Double)

data class Place(
    val name: String,
    val website: String,
    val phone: String,
    val address: String,
    val closed: Boolean,
    val returned_categories: List<List<String>>,
    val hours: Hours,
    val neighborhood: List<String>,
    val coordinates: Coordinates
)

data class Hours(val state_switch_time: String?)

data class Coordinates(val latitude: Double, val longitude: Double)

class PlacesQuickAction @Inject constructor(private val placesQuickActionService: PlacesQuickActionService) : QuickAction {

    override fun getQuickActions(query: String): Observable<List<QuickAnswerSuggestion>> {
        return placesQuickActionService.places(query).map { Pair(it, it.results) }
            .flatMap { pair ->
                val values = pair.second.filter { !it.closed }.take(3)
                val list = mutableListOf<QuickAnswerSuggestion>()
                values.map {
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse("tel:${it.phone}")
                    val address = it.address.split(" ")
                    val postCode = "${address[address.lastIndex - 1]} ${address.last()}"
                    val closesAt = if (it.hours.state_switch_time.isNullOrEmpty()) {
                        ""
                    } else {
                        ", Closes at ${it.hours.state_switch_time}"
                    }
                    val neighborhood = it.neighborhood.firstOrNull() ?: ""
                    val distance = calculateDistance(pair.first.geoip_lat, it.coordinates.latitude, pair.first.geoip_lon, it.coordinates.longitude)
                    list.add(IntentSuggestion("$postCode, $distance miles away$closesAt", "Call ${it.name} $neighborhood", intent))
                }
                Observable.just(list.toList().distinct())
            }
            .onErrorReturn { e ->
                Timber.e(e.localizedMessage)
                emptyList()
            }

    }

    private fun calculateDistance(latitudeA: Double, latitudeB: Double, longitudeA: Double, longitudeB: Double): String {
        val startPoint = Location("locationA")
        startPoint.latitude = latitudeA
        startPoint.longitude = longitudeA

        val endPoint = Location("locationB")
        endPoint.latitude = latitudeB
        endPoint.longitude = longitudeB
        val miles = 0.0006213712
        val distance = startPoint.distanceTo(endPoint) * miles
        return String.format("%.1f", distance)
    }
}
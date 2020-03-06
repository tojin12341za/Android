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
import com.duckduckgo.app.autocomplete.api.AutoComplete.AutoCompleteSuggestion.QuickAnswerSuggestion.MultipleIntentSuggestion
import com.duckduckgo.app.browser.R
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject

class ExtendedPlacesQuickAction @Inject constructor(private val placesQuickActionService: PlacesQuickActionService) : QuickAction {

    override fun getQuickActions(query: String): Observable<List<QuickAnswerSuggestion>> {
        return placesQuickActionService.places(query).map { Pair(it, it.results) }
            .flatMap { pair ->
                val values = pair.second.filter { !it.closed }.take(3)
                val list = mutableListOf<QuickAnswerSuggestion>()
                values.map {
                    val firstIntent = Intent(Intent.ACTION_DIAL)
                    firstIntent.data = Uri.parse("tel:${it.phone}")
                    val secondIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:${it.coordinates.latitude},${it.coordinates.longitude}"))
                    val address = it.address.split(" ")
                    val postCode = "${address[address.lastIndex - 1]} ${address.last()}"
                    val closesAt = if (it.hours.state_switch_time.isNullOrEmpty()) {
                        ""
                    } else {
                        ", Closes at ${it.hours.state_switch_time}"
                    }
                    val neighborhood = it.neighborhood.firstOrNull() ?: ""
                    val distance = calculateDistance(pair.first.geoip_lat, it.coordinates.latitude, pair.first.geoip_lon, it.coordinates.longitude)

                    val mapOfIntents = mapOf(
                        "Call Now" to firstIntent,
                        "Get Directions" to secondIntent
                    )
                    list.add(
                        MultipleIntentSuggestion(
                            it.website,
                            "${it.name} $neighborhood",
                            mapOfIntents,
                            R.drawable.ic_pin,
                            "$postCode, $distance miles away$closesAt"
                        )
                    )
                }
                Observable.just(list.toList().distinct())
            }
            .onErrorReturn { e ->
                Timber.e(e.localizedMessage)
                emptyList()
            }

    }

    private fun calculateDistance(latitudeA: Double, latitudeB: Double, longitudeA: Double, longitudeB: Double): String {
        val startPoint = Location("locationA").apply {
            latitude = latitudeA
            longitude = longitudeA
        }

        val endPoint = Location("locationB").apply {
            latitude = latitudeB
            longitude = longitudeB
        }
        val miles = 0.0006213712
        val distance = startPoint.distanceTo(endPoint) * miles
        return String.format("%.1f", distance)
    }
}
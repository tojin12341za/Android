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

import com.duckduckgo.app.autocomplete.api.AutoComplete.AutoCompleteSuggestion.QuickAnswerSuggestion
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query
import timber.log.Timber
import javax.inject.Inject

// https://api.duckduckgo.com/?q=sunset&format=json&pretty=1&atb=v171-1&ia=answer
interface InstantAnswersQuickActionService {

    @GET("https://api.duckduckgo.com")
    fun instantAswers(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("ia") ia: String = "answer",
        @Query("pretty") pretty: String = "1"
    ): Single<InstantAnswer>
}

data class InstantAnswer(val Answer: String)


class InstantAnswersQuickAction @Inject constructor(private val instantAnswersQuickActionService: InstantAnswersQuickActionService) : QuickAction {

    override fun getQuickActions(query: String): Observable<List<QuickAnswerSuggestion>> {
        return instantAnswersQuickActionService.instantAswers(query)
            .map {
                val list = mutableListOf<QuickAnswerSuggestion>()
                list.add(QuickAnswerSuggestion.InstantAnswerSuggestion(it.Answer))
                list.toList().distinct()
            }
            .onErrorReturn { e ->
                Timber.e(e.localizedMessage)
                emptyList()
            }
            .toObservable()
    }
}
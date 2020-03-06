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
import android.net.Uri
import com.duckduckgo.app.autocomplete.api.AutoComplete.AutoCompleteSuggestion.QuickAnswerSuggestion
import com.duckduckgo.app.autocomplete.api.AutoComplete.AutoCompleteSuggestion.QuickAnswerSuggestion.IntentSuggestion
import com.duckduckgo.app.browser.R
import io.reactivex.Observable
import java.util.Locale
import javax.inject.Inject

class MapsQuickAction @Inject constructor() : QuickAction {

    override fun getQuickActions(query: String): Observable<List<QuickAnswerSuggestion>> {
        val directions = query.replace(" ", "+").toLowerCase(Locale.ROOT).removePrefix("directions+to+")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$directions"))
        return Observable.just(
            listOf(
                IntentSuggestion(
                    "We will open your maps app",
                    "Get me to ${directions.replace("+", " ")}",
                    intent,
                    R.drawable.ic_pin
                )
            )
        )
    }
}
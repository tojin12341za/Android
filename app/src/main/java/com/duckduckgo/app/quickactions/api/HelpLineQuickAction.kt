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

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.duckduckgo.app.autocomplete.api.AutoComplete
import com.duckduckgo.app.browser.R
import com.duckduckgo.app.quickactions.models.HelpLineJson
import com.squareup.moshi.Moshi
import java.util.Locale

class HelpLineQuickAction(private val context: Context, private val moshi: Moshi) : QuickAction {

    override fun getQuickActions(): List<AutoComplete.AutoCompleteSuggestion.QuickAnswerSuggestion> {
        val quickAnswersJson = context.resources.openRawResource(R.raw.helplines).bufferedReader().use { it.readText() }
        val adapter = moshi.adapter(HelpLineJson::class.java)
        val answers = adapter.fromJson(quickAnswersJson)
        val country = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales.get(0).country.toUpperCase(Locale.getDefault())
        } else {
            context.resources.configuration.locale.country.toUpperCase(Locale.getDefault())
        }
        val answer = answers.countries[country] ?: return emptyList()

        return answer.contacts.map {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:${it.phone}")
            AutoComplete.AutoCompleteSuggestion.QuickAnswerSuggestion(it.phone, "Call ${it.name} now to get help", intent)
        }.toList()
    }
}
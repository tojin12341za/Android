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

package com.duckduckgo.app.quickactions

import android.content.Context
import com.duckduckgo.app.browser.R
import com.duckduckgo.app.quickactions.api.HelpLineQuickAction
import com.duckduckgo.app.quickactions.api.QuickAction
import com.duckduckgo.app.quickactions.models.TriggersJson
import com.squareup.moshi.Moshi

class QuickActionProvider(private val context: Context, private val moshi: Moshi) {

    fun getAction(query: String): QuickAction? {
        val triggersJson = context.resources.openRawResource(R.raw.triggers).bufferedReader().use { it.readText() }
        val adapter2 = moshi.adapter(TriggersJson::class.java)
        val triggers = adapter2.fromJson(triggersJson)
        val quickAnswer: String? = triggers.triggers[query]

        if (quickAnswer.isNullOrEmpty()) {
            return null
        }

        return when (quickAnswer) {
            HELPLINES -> HelpLineQuickAction(context, moshi)
            else -> null
        }
    }

    companion object {
        const val HELPLINES = "helplines"
    }
}
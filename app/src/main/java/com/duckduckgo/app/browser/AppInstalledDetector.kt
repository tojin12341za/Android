/*
 * Copyright (c) 2019 DuckDuckGo
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

package com.duckduckgo.app.browser

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import com.duckduckgo.app.browser.knownbrowsers.KnownBrowser
import timber.log.Timber


interface AppInstalledDetector {

    fun getAppDataForLink(url: String?): ActivityInfo?
}

class NativeAppInstalledDetector(val context: Context) : AppInstalledDetector {

    override fun getAppDataForLink(url: String?) :ActivityInfo? {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)

        val resolveInfos: List<ResolveInfo> = context.packageManager
            .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            .filterNot { it.activityInfo.packageName == context.packageName }
            .filterNot { isKnownBrowser(it.activityInfo.packageName) }

        Timber.i("Found ${resolveInfos.size} matching apps for $url")

        for (i in resolveInfos) {
            Timber.i("Linked package name is ${i.activityInfo.packageName}")
        }

        if (resolveInfos.isEmpty() || resolveInfos.size > 1) {
            return null
        }
        val activity = resolveInfos.first().activityInfo
        Timber.i("Found installed activity ${activity.name}")

        if (!activity.exported) {
            Timber.i("$activity.name is not exported")
            return null
        }

        return activity
    }

    private fun isKnownBrowser(packageName: String): Boolean {
        var knownBrowser = false
        KnownBrowser.values().forEach { browser ->
            Timber.v("Comparing ${browser.packageName} to $packageName")
            if (browser.packageName == packageName) {
                Timber.d("$packageName is a known browser")
                knownBrowser = true
            }
        }

        return knownBrowser
    }
}
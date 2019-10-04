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

package com.duckduckgo.app.referral

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.duckduckgo.app.statistics.pixels.Pixel
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine

class AppInstallationReferrerStateListener @Inject constructor(
    context: Context,
    private val packageManager: PackageManager,
    private val pixel: Pixel
) : InstallReferrerStateListener {

    private val referralClient = InstallReferrerClient.newBuilder(context).build()

    private var referralCodeContinuation: Continuation<String>? = null

    suspend fun retrieveReferralCode(): String {
        Timber.i("Referrer: Retrieving referral code from Play Store")

        return suspendCoroutine { continuation ->
            referralCodeContinuation = continuation

            if (playStoreReferralServiceInstalled()) {
                Timber.i("Play Store Services installed")
                referralClient.startConnection(this)
            } else {
                Timber.w("Play Store Services not installed")
                continuation.resumeWith(Result.failure(IllegalStateException("Play Store Services not installed")))
            }
        }
    }

    private fun playStoreReferralServiceInstalled(): Boolean {
        val playStoreConnectionServiceIntent = Intent()
        playStoreConnectionServiceIntent.component =
            ComponentName("com.android.vending", "com.google.android.finsky.externalreferrer.GetInstallReferrerService")
        val matchingServices = packageManager.queryIntentServices(playStoreConnectionServiceIntent, 0);

        Timber.i("Found ${matchingServices.size} services")
        return matchingServices.size > 0
    }

    override fun onInstallReferrerSetupFinished(responseCode: Int) {
        when (responseCode) {
            InstallReferrerClient.InstallReferrerResponse.OK -> {
                Timber.i("Referrer: OK!")

                //pixel.fire(Pixel.PixelName.APP_INSTALLATION_REFERRAL_METHOD_PLAY_STORE_SUCCESS)

                val response = referralClient.installReferrer
                val referrer = response.installReferrer
                Timber.i("Referrer: Referral code $referrer - split into parts: \n\t${referrer?.split("&")?.joinToString(separator = "\n\t")}")

                referralCodeContinuation?.resumeWith(Result.success(referrer))

                //val referrerClickTimestamp = sdf.format(Date(response.referrerClickTimestampSeconds))
                //val referrerInstallBeginTimestamp = sdf.format(Date(response.installBeginTimestampSeconds))
                //Timber.i("Referrer: \n$referrer\nClicked at $referrerClickTimestamp\nInstall began $referrerInstallBeginTimestamp")
            }

            InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                Timber.i("Referrer: Feature not supported")

                referralCodeContinuation?.resumeWith(Result.failure(java.lang.IllegalStateException("$responseCode - Feature not supported")))
            }

            InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                Timber.i("Referrer: Connection could not be established")
                referralCodeContinuation?.resumeWith(Result.failure(java.lang.IllegalStateException("$responseCode - Service unavailable")))
            }
            InstallReferrerClient.InstallReferrerResponse.DEVELOPER_ERROR -> {
                Timber.w("Referrer: Developer error")
                referralCodeContinuation?.resumeWith(Result.failure(java.lang.IllegalStateException("$responseCode - Developer error")))
            }
            InstallReferrerClient.InstallReferrerResponse.SERVICE_DISCONNECTED -> {
                Timber.w("Referrer: Service disconnected")
                referralCodeContinuation?.resumeWith(Result.failure(java.lang.IllegalStateException("$responseCode - Service disconnected")))
            }
        }
        referralClient.endConnection()
    }

    override fun onInstallReferrerServiceDisconnected() {
        Timber.i("Referrer: ServiceDisconnected")
    }
}
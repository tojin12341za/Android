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

package com.duckduckgo.app.browser.knownbrowsers

// list originally sourced from https://github.com/mozilla-mobile/focus-android/blob/a84a97494230d82d37b922e7632495ba22932fea/app/src/main/java/org/mozilla/focus/utils/Browsers.java
enum class KnownBrowser(val packageName: String) {
    DUCKDUCKGO("com.duckduckgo.mobile.android"),
    FIREFOX("org.mozilla.firefox"),
    FIREFOX_FOCUS("org.mozilla.focus"),
    FIREFOX_PREVIEW("org.mozilla.fenix"),
    FIREFOX_PREVIEW_BETA("org.mozilla.fenix.beta"),
    FIREFOX_PREVIEW_NIGHTLY("org.mozilla.fenix.nightly"),
    FIREFOX_BETA("org.mozilla.firefox_beta"),
    FIREFOX_AURORA("org.mozilla.fennec_aurora"),
    FIREFOX_NIGHTLY("org.mozilla.fennec"),
    FIREFOX_ROCKET("org.mozilla.rocket"),
    FIREFOX_FDROID("org.mozilla.fennec_fdroid"),
    CHROME("com.android.chrome"),
    CHROME_BETA("com.chrome.beta"),
    CHROME_DEV("com.chrome.dev"),
    CHROME_CANARY("com.chrome.canary"),
    OPERA("com.opera.browser"),
    OPERA_BETA("com.opera.browser.beta"),
    OPERA_MINI("com.opera.mini.native"),
    OPERA_MINI_BETA("com.opera.mini.native.beta"),
    UC_BROWSER("com.UCMobile.intl"),
    UC_BROWSER_MINI("com.uc.browser.en"),
    ANDROID_STOCK_BROWSER("com.android.browser"),
    VIVALDI("com.vivaldi.browser"),
    SAMSUNG_INTERNET("com.sec.android.app.sbrowser"),
    DOLPHIN_BROWSER("mobi.mgeek.TunnyBrowser"),
    BRAVE_BROWSER("com.brave.browser"),
    LINK_BUBBLE("com.linkbubble.playstore"),
    ADBLOCK_BROWSER("org.adblockplus.browser"),
    CHROMER("arun.com.chromer"),
    FLYNX("com.flynx"),
    GHOSTERY_BROWSER("com.ghostery.android.ghostery"),
    SEZNAM("cz.seznam.sbrowser"),
    CM_BROWSER("com.ksmobile.cb");
}
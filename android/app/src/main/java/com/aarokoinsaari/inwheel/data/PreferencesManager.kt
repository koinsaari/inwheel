/*
 * Copyright (c) 2025 Aaro Koinsaari
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

package com.aarokoinsaari.inwheel.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("inwheel_preferences", Context.MODE_PRIVATE)

    fun hasAcceptedTerms(): Boolean =
        sharedPreferences.getBoolean(KEY_TERMS_ACCEPTED, false)

    fun setTermsAccepted(accepted: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_TERMS_ACCEPTED, accepted)
        }
    }

    companion object {
        private const val KEY_TERMS_ACCEPTED = "terms_accepted"
    }
}

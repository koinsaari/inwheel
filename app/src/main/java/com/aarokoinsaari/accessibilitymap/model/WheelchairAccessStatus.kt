/*
 * Copyright (c) 2024 Aaro Koinsaari
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

package com.aarokoinsaari.accessibilitymap.model

import androidx.compose.ui.graphics.Color

enum class WheelchairAccessStatus {
    FULLY_ACCESSIBLE,
    PARTIALLY_ACCESSIBLE,
    LIMITED_ACCESSIBILITY,
    NOT_ACCESSIBLE,
    UNKNOWN;

    fun getAccessibilityColor(): Color =
        when (this) { // TODO: Change to MaterialTheme
            FULLY_ACCESSIBLE -> Color.Green
            PARTIALLY_ACCESSIBLE -> Color.Yellow
            LIMITED_ACCESSIBILITY -> Color.Yellow // TODO: Change to orange
            NOT_ACCESSIBLE -> Color.Red
            UNKNOWN -> Color.Gray
        }
}

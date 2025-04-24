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

package com.aarokoinsaari.inwheel.domain.intent

import com.aarokoinsaari.inwheel.domain.model.AccessibilityStatus
import com.aarokoinsaari.inwheel.domain.model.Place
import com.aarokoinsaari.inwheel.domain.model.PlaceDetailProperty

sealed class PlaceDetailIntent {
    data class UpdateGeneralAccessibility(
        val place: Place,
        val status: AccessibilityStatus
    ) : PlaceDetailIntent()
    data class UpdateAccessibilityDetail(
        val place: Place,
        val detailProperty: PlaceDetailProperty,
        val status: AccessibilityStatus
    ) : PlaceDetailIntent()
    data class UpdateAccessibilityDetailString(
        val place: Place,
        val detailProperty: PlaceDetailProperty,
        val value: String
    ) : PlaceDetailIntent()
    data class UpdateAccessibilityDetailBoolean(
        val place: Place,
        val detailProperty: PlaceDetailProperty,
        val value: Boolean
    ) : PlaceDetailIntent()
    data class OpenDialog(
        val place: Place,
        val property: PlaceDetailProperty
    ) : PlaceDetailIntent()
    data class CloseDialog(
        val place: Place,
        val property: PlaceDetailProperty
    ) : PlaceDetailIntent()
}

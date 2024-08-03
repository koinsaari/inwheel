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
package com.aarokoinsaari.accessibilitymap.network

import com.aarokoinsaari.accessibilitymap.R

object CategoryConfig {
    val allCategories: Map<String, Int> = mapOf(
        "cafe" to R.drawable.ic_cafe,
        "restaurant" to R.drawable.ic_restaurant,
        "toilets" to R.drawable.ic_wc,
        "bus_station" to R.drawable.ic_bus_station,
        "train_station" to R.drawable.ic_train_station,
        "subway_station" to R.drawable.ic_subway_station,
        "parking" to R.drawable.ic_parking_area,
        "supermarket" to R.drawable.ic_grocery_store,
        "shop" to R.drawable.ic_shop,
        "pharmacy" to R.drawable.ic_pharmacy,
        "hospital" to R.drawable.ic_hospital,
        "beach" to R.drawable.ic_beach,
        "default" to R.drawable.ic_default_marker
    )
}

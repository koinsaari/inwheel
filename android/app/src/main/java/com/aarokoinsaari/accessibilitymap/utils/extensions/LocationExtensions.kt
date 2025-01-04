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

package com.aarokoinsaari.accessibilitymap.utils.extensions

import android.location.Location
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.Tasks

@Suppress("TooGenericExceptionCaught") // TODO
fun FusedLocationProviderClient.getLastLocationSuspended(): Location? =
    try {
        Tasks.await(lastLocation)
    } catch (e: SecurityException) {
        Log.e("getLastLocationSuspended", "Location permission denied", e)
        null
    } catch (e: Exception) {
        Log.e("getLastLocationSuspended", e.message, e)
        null
    }

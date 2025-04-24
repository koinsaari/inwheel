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

package com.aarokoinsaari.inwheel.view.placedetails

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.aarokoinsaari.inwheel.R
import com.aarokoinsaari.inwheel.domain.intent.PlaceDetailIntent
import com.aarokoinsaari.inwheel.domain.model.AccessibilityStatus
import com.aarokoinsaari.inwheel.domain.model.AccessibilityStatus.FULLY_ACCESSIBLE
import com.aarokoinsaari.inwheel.domain.model.AccessibilityStatus.NOT_ACCESSIBLE
import com.aarokoinsaari.inwheel.domain.model.AccessibilityStatus.PARTIALLY_ACCESSIBLE
import com.aarokoinsaari.inwheel.domain.model.AccessibilityStatus.UNKNOWN
import com.aarokoinsaari.inwheel.domain.model.Place
import com.aarokoinsaari.inwheel.domain.model.PlaceDetailProperty

@Composable
fun PlaceDetailsContactInfoSection(
    place: Place,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val contactItems = listOfNotNull(
        place.phone?.let { phone ->
            Triple(
                phone,
                Icons.Outlined.Phone,
                stringResource(id = R.string.content_desc_phone)
            ) to {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = "tel:$phone".toUri()
                }
                context.startActivity(intent)
            }
        },
        place.email?.let { email ->
            Triple(
                email,
                Icons.Outlined.Email,
                stringResource(id = R.string.content_desc_email)
            ) to {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = "mailto:$email".toUri()
                }
                context.startActivity(intent)
            }
        },
        place.website?.let { website ->
            Triple(
                website,
                Icons.Outlined.Public,
                stringResource(id = R.string.content_desc_website)
            ) to {
                val normalized = if (website.startsWith("http")) website else "https://$website"
                val intent = Intent(Intent.ACTION_VIEW, normalized.toUri())
                context.startActivity(intent)
            }
        }
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        if (contactItems.isNotEmpty()) {
            contactItems.forEach { (info, action) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = action)
                ) {
                    Icon(
                        imageVector = info.second,
                        contentDescription = info.third,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = info.first,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            Text(
                text = stringResource(id = R.string.no_contact_info_available),
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
fun PlaceDetailsEntranceDetailsSection(
    place: Place,
    modifier: Modifier = Modifier,
    onIntent: (PlaceDetailIntent) -> Unit = {},
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        val stepCount = place.stepCount
        PlaceDetailItem(
            detailProperty = PlaceDetailProperty.STEP_COUNT,
            value = stringResource(id = stepCount.getStepCountValueStringRes()),
            place = place,
            onIntent = onIntent
        )

        if (stepCount != FULLY_ACCESSIBLE && stepCount != UNKNOWN && stepCount != null) {
            PlaceDetailItem(
                detailProperty = PlaceDetailProperty.STEP_HEIGHT,
                value = stringResource(id = place.stepHeight.getStepHeightValueStringRes()),
                place = place,
                onIntent = onIntent
            )
        }
        PlaceDetailItem(
            detailProperty = PlaceDetailProperty.RAMP,
            value = stringResource(id = place.ramp.getRampValueStringRes()),
            place = place,
            onIntent = onIntent
        )
        PlaceDetailItem(
            detailProperty = PlaceDetailProperty.LIFT,
            value = stringResource(id = place.lift.getLiftValueStringRes()),
            place = place,
            onIntent = onIntent
        )
        PlaceDetailItem(
            detailProperty = PlaceDetailProperty.ENTRANCE_WIDTH,
            value = stringResource(id = place.entranceWidth.getDoorWidthValueStringRes()),
            place = place,
            onIntent = onIntent
        )
        PlaceDetailItem(
            detailProperty = PlaceDetailProperty.DOOR_TYPE,
            value = place.doorType ?: stringResource(id = R.string.emoji_question),
            place = place,
            onIntent = onIntent
        )
    }
}

@Composable
fun PlaceDetailsRestroomDetailsSection(
    place: Place,
    modifier: Modifier = Modifier,
    onIntent: (PlaceDetailIntent) -> Unit = {},
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        PlaceDetailItem(
            detailProperty = PlaceDetailProperty.DOOR_WIDTH,
            value = stringResource(id = place.doorWidth.getDoorWidthValueStringRes()),
            place = place,
            onIntent = onIntent
        )
        PlaceDetailItem(
            detailProperty = PlaceDetailProperty.ROOM_MANEUVER,
            value = stringResource(id = place.roomManeuver.getRoomManeuverValueStringRes()),
            place = place,
            onIntent = onIntent
        )
        PlaceDetailItem(
            detailProperty = PlaceDetailProperty.GRAB_RAILS,
            value = stringResource(id = place.grabRails.getGrabRailsValueStringRes()),
            place = place,
            onIntent = onIntent
        )
        PlaceDetailItem(
            detailProperty = PlaceDetailProperty.SINK,
            value = stringResource(id = place.sink.getAccessibilityStringRes()),
            place = place,
            onIntent = onIntent
        )
        PlaceDetailItem(
            detailProperty = PlaceDetailProperty.TOILET_SEAT,
            value = stringResource(id = place.toiletSeat.getAccessibilityStringRes()),
            place = place,
            onIntent = onIntent
        )
        PlaceDetailItem(
            detailProperty = PlaceDetailProperty.EMERGENCY_ALARM,
            value = stringResource(id = place.emergencyAlarm.getEmergencyAlarmValueStringRes()),
            place = place,
            onIntent = onIntent
        )
        PlaceDetailItem(
            detailProperty = PlaceDetailProperty.EURO_KEY,
            value = stringResource(id = place.euroKey.getBooleanEmojiStringRes()),
            place = place,
            onIntent = onIntent
        )
    }
}

private fun Boolean?.getBooleanEmojiStringRes(): Int =
    when (this) {
        true -> R.string.checkmark_yes
        false -> R.string.cross_no
        null -> R.string.emoji_question
    }

private fun AccessibilityStatus?.getStepCountValueStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.step_count_option_none
        PARTIALLY_ACCESSIBLE -> R.string.step_count_option_one
        NOT_ACCESSIBLE -> R.string.step_count_option_multiple
        else -> R.string.emoji_question
    }

private fun AccessibilityStatus?.getStepHeightValueStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.step_height_option_low
        PARTIALLY_ACCESSIBLE -> R.string.step_height_option_medium
        NOT_ACCESSIBLE -> R.string.step_height_option_high
        else -> R.string.emoji_question
    }

private fun AccessibilityStatus?.getDoorWidthValueStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.door_width_option_wide
        PARTIALLY_ACCESSIBLE -> R.string.door_width_option_standard
        NOT_ACCESSIBLE -> R.string.door_width_option_narrow
        else -> R.string.emoji_question
    }

private fun AccessibilityStatus?.getLiftValueStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.lift_option_accessible
        PARTIALLY_ACCESSIBLE -> R.string.lift_option_partially_accessible
        NOT_ACCESSIBLE -> R.string.lift_option_none
        else -> R.string.emoji_question
    }

private fun AccessibilityStatus?.getRampValueStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.ramp_option_accessible
        PARTIALLY_ACCESSIBLE -> R.string.ramp_option_partial
        NOT_ACCESSIBLE -> R.string.ramp_option_none
        else -> R.string.emoji_question
    }

private fun AccessibilityStatus?.getRoomManeuverValueStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.room_maneuver_option_spacious
        PARTIALLY_ACCESSIBLE -> R.string.room_maneuver_option_limited
        NOT_ACCESSIBLE -> R.string.room_maneuver_option_tight
        else -> R.string.emoji_question
    }

private fun AccessibilityStatus?.getGrabRailsValueStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.grab_rails_option_both_sides
        PARTIALLY_ACCESSIBLE -> R.string.grab_rails_option_one_side
        NOT_ACCESSIBLE -> R.string.grab_rails_option_none
        else -> R.string.emoji_question
    }

private fun AccessibilityStatus?.getEmergencyAlarmValueStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.emergency_alarm_option_available
        PARTIALLY_ACCESSIBLE -> R.string.emergency_alarm_option_limited
        NOT_ACCESSIBLE -> R.string.emergency_alarm_option_none
        else -> R.string.emoji_question
    }

private fun AccessibilityStatus?.getAccessibilityStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.accessibility_option_fully_accessible
        PARTIALLY_ACCESSIBLE -> R.string.accessibility_option_partially_accessible
        NOT_ACCESSIBLE -> R.string.accessibility_option_not_accessible
        else -> R.string.emoji_question
    }

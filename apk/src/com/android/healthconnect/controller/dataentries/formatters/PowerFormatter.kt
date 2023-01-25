/**
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * ```
 *      http://www.apache.org/licenses/LICENSE-2.0
 * ```
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.android.healthconnect.controller.dataentries.formatters

import android.content.Context
import android.healthconnect.datatypes.PowerRecord
import android.healthconnect.datatypes.PowerRecord.PowerRecordSample
import android.icu.text.MessageFormat
import androidx.annotation.StringRes
import com.android.healthconnect.controller.R
import com.android.healthconnect.controller.dataentries.formatters.shared.EntryFormatter
import com.android.healthconnect.controller.dataentries.units.UnitPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Formatter for printing Power series data. */
@Singleton
class PowerFormatter @Inject constructor(@ApplicationContext private val context: Context) :
    EntryFormatter<PowerRecord>(context) {

    override suspend fun formatValue(
        record: PowerRecord,
        unitPreferences: UnitPreferences
    ): String {
        return format(R.string.watt_format, record.samples)
    }

    override suspend fun formatA11yValue(
        record: PowerRecord,
        unitPreferences: UnitPreferences
    ): String {
        return format(R.string.watt_format_long, record.samples)
    }

    private fun format(@StringRes res: Int, samples: List<PowerRecordSample>): String {
        val avrPower = samples.sumOf { it.power.inWatts } / samples.size
        return MessageFormat.format(context.getString(res), mapOf("value" to avrPower))
    }
}

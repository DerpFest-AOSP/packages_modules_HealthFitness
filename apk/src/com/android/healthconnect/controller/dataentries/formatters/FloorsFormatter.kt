/**
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.android.healthconnect.controller.dataentries.formatters

import android.content.Context
import android.healthconnect.datatypes.FloorsClimbedRecord
import android.icu.text.MessageFormat.format
import com.android.healthconnect.controller.R
import com.android.healthconnect.controller.dataentries.formatters.shared.EntryFormatter
import com.android.healthconnect.controller.dataentries.units.UnitPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** Formatter for printing FloorsRecord data. */
class FloorsFormatter @Inject constructor(@ApplicationContext private val context: Context) :
    EntryFormatter<FloorsClimbedRecord>(context) {

    override suspend fun formatValue(
        record: FloorsClimbedRecord,
        unitPreferences: UnitPreferences
    ): String {
        return format(context.getString(R.string.floors_climbed), mapOf("count" to record.floors))
    }

    override suspend fun formatA11yValue(
        record: FloorsClimbedRecord,
        unitPreferences: UnitPreferences
    ): String {
        return formatValue(record, unitPreferences)
    }
}

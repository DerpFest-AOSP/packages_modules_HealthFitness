/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.health.connect.internal.datatypes;

import android.annotation.NonNull;
import android.health.connect.datatypes.Identifier;
import android.health.connect.datatypes.LeanBodyMassRecord;
import android.health.connect.datatypes.RecordTypeIdentifier;
import android.health.connect.datatypes.units.Mass;
import android.os.Parcel;

/**
 * @see LeanBodyMassRecord
 * @hide
 */
@Identifier(recordIdentifier = RecordTypeIdentifier.RECORD_TYPE_LEAN_BODY_MASS)
public final class LeanBodyMassRecordInternal extends InstantRecordInternal<LeanBodyMassRecord> {
    private double mMass;

    public double getMass() {
        return mMass;
    }

    /** returns this object with the specified mass */
    @NonNull
    public LeanBodyMassRecordInternal setMass(double mass) {
        this.mMass = mass;
        return this;
    }

    @NonNull
    @Override
    public LeanBodyMassRecord toExternalRecord() {
        return new LeanBodyMassRecord.Builder(
                        buildMetaData(), getTime(), Mass.fromKilograms(getMass()))
                .setZoneOffset(getZoneOffset())
                .build();
    }

    @Override
    void populateInstantRecordFrom(@NonNull Parcel parcel) {
        mMass = parcel.readDouble();
    }

    @Override
    void populateInstantRecordFrom(@NonNull LeanBodyMassRecord leanBodyMassRecord) {
        mMass = leanBodyMassRecord.getMass().getInKilograms();
    }

    @Override
    void populateInstantRecordTo(@NonNull Parcel parcel) {
        parcel.writeDouble(mMass);
    }
}
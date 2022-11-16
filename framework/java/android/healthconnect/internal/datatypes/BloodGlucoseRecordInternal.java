/*
 * Copyright (C) 2022 The Android Open Source Project
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
package android.healthconnect.internal.datatypes;

import android.healthconnect.datatypes.BloodGlucoseRecord;
import android.healthconnect.datatypes.BloodGlucoseRecord.RelationToMealType.RelationToMealTypes;
import android.healthconnect.datatypes.BloodGlucoseRecord.SpecimenSource.SpecimenSourceType;
import android.healthconnect.datatypes.Identifier;
import android.healthconnect.datatypes.MealType;
import android.healthconnect.datatypes.RecordTypeIdentifier;
import android.healthconnect.datatypes.units.BloodGlucose;
import android.os.Parcel;

import androidx.annotation.NonNull;

/**
 * @see BloodGlucoseRecord
 * @hide
 */
@Identifier(recordIdentifier = RecordTypeIdentifier.RECORD_TYPE_BLOOD_GLUCOSE)
public final class BloodGlucoseRecordInternal extends InstantRecordInternal<BloodGlucoseRecord> {
    private int mSpecimenSource;
    private double mLevel;
    private int mRelationToMeal;
    private int mMealType;

    @SpecimenSourceType
    public int getSpecimenSource() {
        return mSpecimenSource;
    }

    /** returns this object with the specified specimenSource */
    @NonNull
    public BloodGlucoseRecordInternal setSpecimenSource(int specimenSource) {
        this.mSpecimenSource = specimenSource;
        return this;
    }

    public double getLevel() {
        return mLevel;
    }

    /** returns this object with the specified level */
    @NonNull
    public BloodGlucoseRecordInternal setLevel(double level) {
        this.mLevel = level;
        return this;
    }

    @RelationToMealTypes
    public int getRelationToMeal() {
        return mRelationToMeal;
    }

    /** returns this object with the specified relationToMeal */
    @NonNull
    public BloodGlucoseRecordInternal setRelationToMeal(int relationToMeal) {
        this.mRelationToMeal = relationToMeal;
        return this;
    }

    @MealType.MealTypes
    public int getMealType() {
        return mMealType;
    }

    /** returns this object with the specified mealType */
    @NonNull
    public BloodGlucoseRecordInternal setMealType(int mealType) {
        this.mMealType = mealType;
        return this;
    }

    @NonNull
    @Override
    public BloodGlucoseRecord toExternalRecord() {
        return new BloodGlucoseRecord.Builder(
                        buildMetaData(),
                        getTime(),
                        getSpecimenSource(),
                        BloodGlucose.fromMillimolesPerLiter(getLevel()),
                        getRelationToMeal(),
                        getMealType())
                .setZoneOffset(getZoneOffset())
                .build();
    }

    @Override
    void populateInstantRecordFrom(@NonNull Parcel parcel) {
        mSpecimenSource = parcel.readInt();
        mLevel = parcel.readDouble();
        mRelationToMeal = parcel.readInt();
        mMealType = parcel.readInt();
    }

    @Override
    void populateInstantRecordFrom(@NonNull BloodGlucoseRecord bloodGlucoseRecord) {
        mSpecimenSource = bloodGlucoseRecord.getSpecimenSource();
        mLevel = bloodGlucoseRecord.getLevel().getInMillimolesPerLiter();
        mRelationToMeal = bloodGlucoseRecord.getRelationToMeal();
        mMealType = bloodGlucoseRecord.getMealType();
    }

    @Override
    void populateInstantRecordTo(@NonNull Parcel parcel) {
        parcel.writeInt(mSpecimenSource);
        parcel.writeDouble(mLevel);
        parcel.writeInt(mRelationToMeal);
        parcel.writeInt(mMealType);
    }
}
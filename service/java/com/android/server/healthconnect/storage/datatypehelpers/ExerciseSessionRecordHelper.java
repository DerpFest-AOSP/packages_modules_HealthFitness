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

package com.android.server.healthconnect.storage.datatypehelpers;

import static android.health.connect.HealthPermissions.READ_EXERCISE_ROUTE;
import static android.health.connect.HealthPermissions.WRITE_EXERCISE_ROUTE;

import static com.android.server.healthconnect.storage.datatypehelpers.ExerciseRouteRecordHelper.EXERCISE_ROUTE_RECORD_TABLE_NAME;
import static com.android.server.healthconnect.storage.datatypehelpers.SeriesRecordHelper.PARENT_KEY_COLUMN_NAME;
import static com.android.server.healthconnect.storage.utils.StorageUtils.BOOLEAN_FALSE_VALUE;
import static com.android.server.healthconnect.storage.utils.StorageUtils.BOOLEAN_TRUE_VALUE;
import static com.android.server.healthconnect.storage.utils.StorageUtils.INTEGER;
import static com.android.server.healthconnect.storage.utils.StorageUtils.TEXT_NULL;
import static com.android.server.healthconnect.storage.utils.StorageUtils.getCursorInt;
import static com.android.server.healthconnect.storage.utils.StorageUtils.getCursorString;
import static com.android.server.healthconnect.storage.utils.StorageUtils.getIntegerAndConvertToBoolean;

import android.annotation.NonNull;
import android.content.ContentValues;
import android.database.Cursor;
import android.health.connect.aidl.ReadRecordsRequestParcel;
import android.health.connect.datatypes.RecordTypeIdentifier;
import android.health.connect.internal.datatypes.ExerciseLapInternal;
import android.health.connect.internal.datatypes.ExerciseSegmentInternal;
import android.health.connect.internal.datatypes.ExerciseSessionRecordInternal;
import android.health.connect.internal.datatypes.RecordInternal;
import android.util.ArraySet;
import android.util.Pair;

import com.android.server.healthconnect.storage.request.CreateTableRequest;
import com.android.server.healthconnect.storage.request.ReadTableRequest;
import com.android.server.healthconnect.storage.request.UpsertTableRequest;
import com.android.server.healthconnect.storage.utils.SqlJoin;
import com.android.server.healthconnect.storage.utils.WhereClauses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for ExerciseSessionRecord.
 *
 * @hide
 */
@HelperFor(recordIdentifier = RecordTypeIdentifier.RECORD_TYPE_EXERCISE_SESSION)
public final class ExerciseSessionRecordHelper
        extends IntervalRecordHelper<ExerciseSessionRecordInternal> {
    private static final String TAG = "ExerciseSessionRecordHelper";

    private static final String EXERCISE_SESSION_RECORD_TABLE_NAME =
            "exercise_session_record_table";

    // Exercise Session columns names
    private static final String NOTES_COLUMN_NAME = "notes";
    private static final String EXERCISE_TYPE_COLUMN_NAME = "exercise_type";
    private static final String TITLE_COLUMN_NAME = "title";
    private static final String HAS_ROUTE_COLUMN_NAME = "has_route";

    /** Returns the table name to be created corresponding to this helper */
    @Override
    String getMainTableName() {
        return EXERCISE_SESSION_RECORD_TABLE_NAME;
    }

    @Override
    void populateSpecificRecordValue(
            @NonNull Cursor cursor, @NonNull ExerciseSessionRecordInternal exerciseSessionRecord) {
        String uuid = getCursorString(cursor, UUID_COLUMN_NAME);
        exerciseSessionRecord.setNotes(getCursorString(cursor, NOTES_COLUMN_NAME));
        exerciseSessionRecord.setExerciseType(getCursorInt(cursor, EXERCISE_TYPE_COLUMN_NAME));
        exerciseSessionRecord.setTitle(getCursorString(cursor, TITLE_COLUMN_NAME));
        exerciseSessionRecord.setHasRoute(
                getIntegerAndConvertToBoolean(cursor, HAS_ROUTE_COLUMN_NAME));

        // The table might contain duplicates because of 2 left joins, use sets to remove them.
        ArraySet<ExerciseLapInternal> lapsSet = new ArraySet<>();
        ArraySet<ExerciseSegmentInternal> segmentsSet = new ArraySet<>();
        do {
            // Populate lap and segments from each row.
            ExerciseLapRecordHelper.populateLapIfRecorded(cursor, lapsSet);
            ExerciseSegmentRecordHelper.populateSegmentIfRecorded(cursor, segmentsSet);
        } while (cursor.moveToNext() && uuid.equals(getCursorString(cursor, UUID_COLUMN_NAME)));
        // In case we hit another record, move the cursor back to read next record in outer
        // RecordHelper#getInternalRecords loop.
        cursor.moveToPrevious();

        if (!lapsSet.isEmpty()) {
            exerciseSessionRecord.setExerciseLaps(lapsSet.stream().toList());
        }

        if (!segmentsSet.isEmpty()) {
            exerciseSessionRecord.setExerciseSegments(segmentsSet.stream().toList());
        }
    }

    @Override
    void populateSpecificContentValues(
            @NonNull ContentValues contentValues,
            @NonNull ExerciseSessionRecordInternal exerciseSessionRecord) {
        contentValues.put(NOTES_COLUMN_NAME, exerciseSessionRecord.getNotes());
        contentValues.put(EXERCISE_TYPE_COLUMN_NAME, exerciseSessionRecord.getExerciseType());
        contentValues.put(TITLE_COLUMN_NAME, exerciseSessionRecord.getTitle());
        contentValues.put(
                HAS_ROUTE_COLUMN_NAME,
                exerciseSessionRecord.hasRoute() ? BOOLEAN_TRUE_VALUE : BOOLEAN_FALSE_VALUE);
    }

    @Override
    List<CreateTableRequest> getChildTableCreateRequests() {
        return List.of(
                ExerciseRouteRecordHelper.getCreateRouteTableRequest(getMainTableName()),
                ExerciseLapRecordHelper.getCreateLapsTableRequest(getMainTableName()),
                ExerciseSegmentRecordHelper.getCreateSegmentsTableRequest(getMainTableName()));
    }

    @Override
    List<UpsertTableRequest> getChildTableUpsertRequests(
            @NonNull ExerciseSessionRecordInternal record) {
        List<UpsertTableRequest> childUpsertRequests = new ArrayList<>();

        if (record.getRoute() != null) {
            childUpsertRequests.addAll(
                    ExerciseRouteRecordHelper.getRouteUpsertRequests(record.getRoute()));
        }

        if (record.getLaps() != null) {
            childUpsertRequests.addAll(
                    ExerciseLapRecordHelper.getLapsUpsertRequests(record.getLaps()));
        }

        if (record.getSegments() != null) {
            childUpsertRequests.addAll(
                    ExerciseSegmentRecordHelper.getSegmentsUpsertRequests(record.getSegments()));
        }

        return childUpsertRequests;
    }

    @Override
    @NonNull
    protected List<Pair<String, String>> getIntervalRecordColumnInfo() {
        return Arrays.asList(
                new Pair<>(NOTES_COLUMN_NAME, TEXT_NULL),
                new Pair<>(EXERCISE_TYPE_COLUMN_NAME, INTEGER),
                new Pair<>(TITLE_COLUMN_NAME, TEXT_NULL),
                new Pair<>(HAS_ROUTE_COLUMN_NAME, INTEGER));
    }

    @Override
    SqlJoin getJoinForReadRequest() {
        return ExerciseLapRecordHelper.getJoinReadRequest(getMainTableName())
                .attachJoin(ExerciseSegmentRecordHelper.getJoinReadRequest(getMainTableName()));
    }

    @Override
    List<ReadTableRequest> getExtraDataReadRequests(
            ReadRecordsRequestParcel request,
            String packageName,
            long startDateAccess,
            Map<String, Boolean> extraPermsState) {
        boolean canReadAnyRoute = extraPermsState.get(READ_EXERCISE_ROUTE);
        WhereClauses whereClause =
                getReadTableWhereClause(
                        request,
                        packageName,
                        /* enforceSelfRead= */ !canReadAnyRoute,
                        startDateAccess);
        return List.of(getRouteReadRequest(whereClause));
    }

    /** Returns extra permissions required to write given record. */
    @Override
    public List<String> getExtraWritePermissionsToCheck(RecordInternal<?> recordInternal) {
        ExerciseSessionRecordInternal session = (ExerciseSessionRecordInternal) recordInternal;
        if (session.getRoute() != null) {
            return Collections.singletonList(WRITE_EXERCISE_ROUTE);
        }
        return Collections.emptyList();
    }

    /** Returns permissions required to read extra record data. */
    @Override
    public List<String> getExtraReadPermissions() {
        return Collections.singletonList(READ_EXERCISE_ROUTE);
    }

    @Override
    List<ReadTableRequest> getExtraDataReadRequests(List<String> uuids, long startDateAccess) {
        WhereClauses whereClause = new WhereClauses().addWhereInClause(UUID_COLUMN_NAME, uuids);
        whereClause.addWhereLaterThanTimeClause(getStartTimeColumnName(), startDateAccess);
        return List.of(getRouteReadRequest(whereClause));
    }

    @Override
    public void readExtraData(
            List<ExerciseSessionRecordInternal> internalRecords,
            Cursor cursorExtraData,
            String tableName) {
        // Collect rowId to Record mapping to understand which record update with route location.
        Map<Integer, Integer> mapping = new HashMap<>(internalRecords.size());
        for (int i = 0; i < internalRecords.size(); i++) {
            mapping.put(internalRecords.get(i).getRowId(), i);
        }

        while (cursorExtraData.moveToNext()) {
            ExerciseSessionRecordInternal record =
                    internalRecords.get(
                            mapping.get(getCursorInt(cursorExtraData, PARENT_KEY_COLUMN_NAME)));
            record.addRouteLocation(ExerciseRouteRecordHelper.populateLocation(cursorExtraData));
        }
    }

    private ReadTableRequest getRouteReadRequest(WhereClauses clauseToFilterSessionIds) {
        ReadTableRequest routeReadRequest = new ReadTableRequest(EXERCISE_ROUTE_RECORD_TABLE_NAME);

        ReadTableRequest sessionsIdsRequest = new ReadTableRequest(getMainTableName());
        sessionsIdsRequest.setColumnNames(List.of(PRIMARY_COLUMN_NAME));
        sessionsIdsRequest.setWhereClause(clauseToFilterSessionIds);

        WhereClauses inClause = new WhereClauses();
        inClause.addWhereInSQLRequestClause(PARENT_KEY_COLUMN_NAME, sessionsIdsRequest);
        routeReadRequest.setWhereClause(inClause);
        return routeReadRequest;
    }
}

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

package android.healthconnect.datatypes;

import android.annotation.FloatRange;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.healthconnect.datatypes.units.Length;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/** Route of the exercise session. Contains sequence of location points with timestamps. */
public final class ExerciseRoute {
    private final List<Location> mRouteLocations;

    /**
     * Creates {@link ExerciseRoute} instance
     *
     * @param routeLocations list of locations with timestamps that make up the route
     */
    public ExerciseRoute(@NonNull List<Location> routeLocations) {
        Objects.requireNonNull(routeLocations);
        mRouteLocations = routeLocations;
    }

    @NonNull
    public List<Location> getRouteLocations() {
        return mRouteLocations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExerciseRoute)) return false;
        ExerciseRoute that = (ExerciseRoute) o;
        return getRouteLocations().equals(that.getRouteLocations());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRouteLocations());
    }

    /** Point in the time and space. Used in {@link ExerciseRoute}. */
    public static final class Location {
        // Values are used for FloatRange annotation in latitude/longitude getters and constructor.
        private static final double MIN_LONGITUDE = -180;
        private static final double MAX_LONGITUDE = 180;
        private static final double MIN_LATITUDE = -90;
        private static final double MAX_LATITUDE = 90;

        private final Instant mTime;
        private final double mLatitude;
        private final double mLongitude;
        private final Length mHorizontalAccuracy;
        private final Length mVerticalAccuracy;
        private final Length mAltitude;

        /**
         * Represents a single location in an exercise route.
         *
         * @param time The point in time when the measurement was taken.
         * @param latitude Latitude of a location represented as a double, in degrees. Valid range:
         *     from -90 to 90 degrees.
         * @param longitude Longitude of a location represented as a double, in degrees. Valid
         *     range: from -180 to 180 degrees.
         * @param horizontalAccuracy The radius of uncertainty for the location, in [Length] unit.
         *     Must be non-negative value.
         * @param verticalAccuracy The validity of the altitude values, and their estimated
         *     uncertainty, in [Length] unit. Must be non-negative value.
         * @param altitude An altitude of a location represented as a float, in [Length] unit above
         *     sea level.
         * @see ExerciseRoute
         */
        private Location(
                @NonNull Instant time,
                @FloatRange(from = MIN_LATITUDE, to = MAX_LATITUDE) double latitude,
                @FloatRange(from = MIN_LONGITUDE, to = MAX_LONGITUDE) double longitude,
                @Nullable Length horizontalAccuracy,
                @Nullable Length verticalAccuracy,
                @Nullable Length altitude) {
            Objects.requireNonNull(time);

            if (latitude < MIN_LATITUDE || latitude > MAX_LATITUDE) {
                throw new IllegalArgumentException(
                        "Latitude must be in range from "
                                + MIN_LATITUDE
                                + " to "
                                + MAX_LATITUDE
                                + ".");
            }

            if (longitude < MIN_LONGITUDE || longitude > MAX_LONGITUDE) {
                throw new IllegalArgumentException(
                        "Longitude must be in range from "
                                + MIN_LONGITUDE
                                + " to "
                                + MAX_LONGITUDE
                                + ".");
            }

            if (horizontalAccuracy != null && horizontalAccuracy.getInMeters() < 0) {
                throw new IllegalArgumentException("Horizontal accuracy must be non-negative");
            }

            if (verticalAccuracy != null && verticalAccuracy.getInMeters() < 0) {
                throw new IllegalArgumentException("Vertical accuracy must be non-negative");
            }

            mTime = time;
            mLatitude = latitude;
            mLongitude = longitude;
            mHorizontalAccuracy = horizontalAccuracy;
            mVerticalAccuracy = verticalAccuracy;
            mAltitude = altitude;
        }

        /** Returns time when this location has been recorded */
        @NonNull
        public Instant getTime() {
            return mTime;
        }

        /** Returns longitude of this location */
        @FloatRange(from = -180.0, to = 180.0)
        public double getLongitude() {
            return mLongitude;
        }

        /** Returns latitude of this location */
        @FloatRange(from = -90.0, to = 90.0)
        public double getLatitude() {
            return mLatitude;
        }

        /**
         * Returns horizontal accuracy of this location time point. Returns null if no horizontal
         * accuracy was specified.
         */
        @Nullable
        public Length getHorizontalAccuracy() {
            return mHorizontalAccuracy;
        }

        /**
         * Returns vertical accuracy of this location time point. Returns null if no vertical
         * accuracy was specified.
         */
        @Nullable
        public Length getVerticalAccuracy() {
            return mVerticalAccuracy;
        }

        /**
         * Returns altitude of this location time point. Returns null if no altitude was specified.
         */
        @Nullable
        public Length getAltitude() {
            return mAltitude;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Location)) return false;
            Location that = (Location) o;
            return Objects.equals(getAltitude(), that.getAltitude())
                    && getTime().equals(that.getTime())
                    && (getLatitude() == that.getLatitude())
                    && (getLongitude() == that.getLongitude())
                    && Objects.equals(getHorizontalAccuracy(), that.getHorizontalAccuracy())
                    && Objects.equals(getVerticalAccuracy(), that.getVerticalAccuracy());
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    getTime(),
                    getLatitude(),
                    getLongitude(),
                    getHorizontalAccuracy(),
                    getVerticalAccuracy(),
                    getAltitude());
        }

        /** Builder class for {@link Location} */
        public static final class Builder {
            @NonNull private final Instant mTime;

            @FloatRange(from = MIN_LATITUDE, to = MAX_LATITUDE)
            private final double mLatitude;

            @FloatRange(from = MIN_LONGITUDE, to = MAX_LONGITUDE)
            private final double mLongitude;

            @Nullable private Length mHorizontalAccuracy;
            @Nullable private Length mVerticalAccuracy;
            @Nullable private Length mAltitude;

            /** Sets time, longitude and latitude to the point. */
            public Builder(
                    @NonNull Instant time,
                    @FloatRange(from = -90.0, to = 90.0) double latitude,
                    @FloatRange(from = -180.0, to = 180.0) double longitude) {
                Objects.requireNonNull(time);
                mTime = time;
                mLatitude = latitude;
                mLongitude = longitude;
            }

            /** Sets horizontal accuracy to the point. */
            @NonNull
            public Builder setHorizontalAccuracy(@NonNull Length horizontalAccuracy) {
                Objects.requireNonNull(horizontalAccuracy);
                mHorizontalAccuracy = horizontalAccuracy;
                return this;
            }

            /** Sets vertical accuracy to the point. */
            @NonNull
            public Builder setVerticalAccuracy(@NonNull Length verticalAccuracy) {
                Objects.requireNonNull(verticalAccuracy);
                mVerticalAccuracy = verticalAccuracy;
                return this;
            }

            /** Sets altitude to the point. */
            @NonNull
            public Builder setAltitude(@NonNull Length altitude) {
                Objects.requireNonNull(altitude);
                mAltitude = altitude;
                return this;
            }

            /** Builds {@link Location} */
            @NonNull
            public Location build() {
                return new Location(
                        mTime,
                        mLatitude,
                        mLongitude,
                        mHorizontalAccuracy,
                        mVerticalAccuracy,
                        mAltitude);
            }
        }
    }
}
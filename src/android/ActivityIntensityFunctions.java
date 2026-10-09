package org.apache.cordova.health;

import android.os.Build;

import androidx.health.connect.client.aggregate.AggregateMetric;
import androidx.health.connect.client.aggregate.AggregationResult;
import androidx.health.connect.client.records.ActivityIntensityRecord;
import androidx.health.connect.client.records.metadata.DataOrigin;
import androidx.health.connect.client.records.metadata.Metadata;
import androidx.health.connect.client.records.Record;
import androidx.health.connect.client.request.AggregateGroupByDurationRequest;
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest;
import androidx.health.connect.client.request.AggregateRequest;
import androidx.health.connect.client.time.TimeRangeFilter;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kotlin.reflect.KClass;

public class ActivityIntensityFunctions {
    public static KClass<? extends Record> dataTypeToClass() {
        return kotlin.jvm.JvmClassMappingKt.getKotlinClass(ActivityIntensityRecord.class);
    }


    public static void populateFromQuery(Record datapoint, JSONObject aiObj) throws JSONException {
        ActivityIntensityRecord aiDP = (ActivityIntensityRecord) datapoint;
        aiObj.put("startDate", aiDP.getStartTime().toEpochMilli());
        aiObj.put("endDate", aiDP.getEndTime().toEpochMilli());

        int activityTypeInt = aiDP.getActivityIntensityType();
        String activityTypeS = "unknown";
        if (activityTypeInt == ActivityIntensityRecord.ACTIVITY_INTENSITY_TYPE_MODERATE) {
            activityTypeS = "moderate";
        } else if (activityTypeInt == ActivityIntensityRecord.ACTIVITY_INTENSITY_TYPE_VIGOROUS) {
            activityTypeS = "vigorous";
        }
        aiObj.put("value", activityTypeS);
        aiObj.put("unit", "activityIntensity");
    }

    public static void populateFromAggregatedQuery(AggregationResult response, JSONObject retObj) throws JSONException {
        if (
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) && (
                        (response.get(ActivityIntensityRecord.DURATION_TOTAL) != null) ||
                        (response.get(ActivityIntensityRecord.INTENSITY_MINUTES_TOTAL) != null) ||
                        (response.get(ActivityIntensityRecord.MODERATE_DURATION_TOTAL) != null) ||
                        (response.get(ActivityIntensityRecord.VIGOROUS_DURATION_TOTAL) != null)
                )
        ) {
            JSONObject aiStats = new JSONObject();
            if (response.get(ActivityIntensityRecord.DURATION_TOTAL) != null) {
                long total = response.get(ActivityIntensityRecord.DURATION_TOTAL).getSeconds();
                aiStats.put("duration", total);
            }

            if (response.get(ActivityIntensityRecord.INTENSITY_MINUTES_TOTAL) != null) {
                long intensityMinutes = response.get(ActivityIntensityRecord.INTENSITY_MINUTES_TOTAL);
                aiStats.put("intensity", intensityMinutes * 60);
            }

            if (response.get(ActivityIntensityRecord.MODERATE_DURATION_TOTAL) != null) {
                long moderateSecs = response.get(ActivityIntensityRecord.MODERATE_DURATION_TOTAL).getSeconds();
                aiStats.put("moderateDuration", moderateSecs);
            }

            if (response.get(ActivityIntensityRecord.VIGOROUS_DURATION_TOTAL) != null) {
                long moderateSecs = response.get(ActivityIntensityRecord.VIGOROUS_DURATION_TOTAL).getSeconds();
                aiStats.put("vigorousDuration", moderateSecs);
            }

            retObj.put("value", aiStats);
            retObj.put("unit", "s");
        } else {
            retObj.put("value", null);
            retObj.put("unit", "'s'");
        }
    }

    public static AggregateGroupByPeriodRequest prepareAggregateGroupByPeriodRequest(TimeRangeFilter timeRange, Period period, HashSet<DataOrigin> dor) {
        Set<AggregateMetric<Duration>> metrics = new HashSet<>();
        metrics.add(ActivityIntensityRecord.DURATION_TOTAL);
        metrics.add(ActivityIntensityRecord.MODERATE_DURATION_TOTAL);
        metrics.add(ActivityIntensityRecord.VIGOROUS_DURATION_TOTAL);
        return new AggregateGroupByPeriodRequest(metrics, timeRange, period, dor);
    }

    public static AggregateGroupByDurationRequest prepareAggregateGroupByDurationRequest(TimeRangeFilter timeRange, Duration duration, HashSet<DataOrigin> dor) {
        Set<AggregateMetric<Duration>> metrics = new HashSet<>();
        metrics.add(ActivityIntensityRecord.DURATION_TOTAL);
        metrics.add(ActivityIntensityRecord.MODERATE_DURATION_TOTAL);
        metrics.add(ActivityIntensityRecord.VIGOROUS_DURATION_TOTAL);
        return new AggregateGroupByDurationRequest(metrics, timeRange, duration, dor);
    }

    public static AggregateRequest prepareAggregateRequest(TimeRangeFilter timeRange, HashSet<DataOrigin> dor) {
        Set<AggregateMetric<Duration>> metrics = new HashSet<>();
        metrics.add(ActivityIntensityRecord.DURATION_TOTAL);
        metrics.add(ActivityIntensityRecord.MODERATE_DURATION_TOTAL);
        metrics.add(ActivityIntensityRecord.VIGOROUS_DURATION_TOTAL);
        return new AggregateRequest(metrics, timeRange, dor);
    }

    public static void prepareStoreRecords(JSONObject storeObj, long st, long et, List<Record> data) throws JSONException {
        String intensityS = storeObj.getString("value");
        int intensityInt = 0;
        if (intensityS.equalsIgnoreCase("moderate")) {
            intensityInt = ActivityIntensityRecord.ACTIVITY_INTENSITY_TYPE_MODERATE;
        }
        if (intensityS.equalsIgnoreCase("vigorous")) {
            intensityInt = ActivityIntensityRecord.ACTIVITY_INTENSITY_TYPE_VIGOROUS;
        }

        ActivityIntensityRecord record = new ActivityIntensityRecord(
                Instant.ofEpochMilli(st), null,
                Instant.ofEpochMilli(et), null,
                Metadata.unknownRecordingMethod(),
                intensityInt
        );
        data.add(record);
    }

}

package org.apache.cordova.health;

import androidx.health.connect.client.aggregate.AggregateMetric;
import androidx.health.connect.client.aggregate.AggregationResult;
import androidx.health.connect.client.records.HydrationRecord;
import androidx.health.connect.client.records.Record;
import androidx.health.connect.client.records.metadata.DataOrigin;
import androidx.health.connect.client.records.metadata.Metadata;
import androidx.health.connect.client.request.AggregateGroupByDurationRequest;
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest;
import androidx.health.connect.client.request.AggregateRequest;
import androidx.health.connect.client.time.TimeRangeFilter;
import androidx.health.connect.client.units.Volume;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import kotlin.reflect.KClass;

public class HydrationFunctions {

    public static KClass<? extends Record> dataTypeToClass() {
        return kotlin.jvm.JvmClassMappingKt.getKotlinClass(HydrationRecord.class);
    }

    public static void populateFromQuery(androidx.health.connect.client.records.Record datapoint, JSONObject obj) throws JSONException {
        HydrationRecord hydrationDP = (HydrationRecord) datapoint;
        obj.put("startDate", hydrationDP.getStartTime().toEpochMilli());
        obj.put("endDate", hydrationDP.getEndTime().toEpochMilli());

        double volume = hydrationDP.getVolume().getMilliliters();
        obj.put("value", volume);
        obj.put("unit", "ml");
    }

    public static void populateFromAggregatedQuery(AggregationResult response, JSONObject retObj)
            throws JSONException {
        if (response.get(HydrationRecord.VOLUME_TOTAL) != null) {
            double liters = Objects.requireNonNull(response.get(HydrationRecord.VOLUME_TOTAL)).getMilliliters();
            retObj.put("value", liters);
            retObj.put("unit", "ml");
        } else {
            retObj.put("value", 0);
            retObj.put("unit", "ml");
        }
    }

    public static AggregateGroupByPeriodRequest prepareAggregateGroupByPeriodRequest(TimeRangeFilter timeRange,
                                                                                              Period period, HashSet<DataOrigin> dor) {
        Set<AggregateMetric<Volume>> metrics = new HashSet<>();
        metrics.add(HydrationRecord.VOLUME_TOTAL);

        return new AggregateGroupByPeriodRequest(metrics, timeRange, period, dor);
    }

    public static AggregateGroupByDurationRequest prepareAggregateGroupByDurationRequest(
            TimeRangeFilter timeRange, Duration duration, HashSet<DataOrigin> dor) {
        Set<AggregateMetric<Volume>> metrics = new HashSet<>();
        metrics.add(HydrationRecord.VOLUME_TOTAL);

        return new AggregateGroupByDurationRequest(metrics, timeRange, duration, dor);
    }

    public static AggregateRequest prepareAggregateRequest(TimeRangeFilter timeRange,
                                                                    HashSet<DataOrigin> dor) {
        Set<AggregateMetric<Volume>> metrics = new HashSet<>();
        metrics.add(HydrationRecord.VOLUME_TOTAL);

        return new AggregateRequest(metrics, timeRange, dor);
    }

    public static void prepareStoreRecords(JSONObject storeObj, List<Record> data) throws JSONException {
        long st = storeObj.getLong("startDate");
        long et = storeObj.getLong("endDate");

        double milliliters = storeObj.getDouble("value");
        Volume vol = Volume.milliliters(milliliters);

        HydrationRecord record = new HydrationRecord(
                Instant.ofEpochMilli(st), null,
                Instant.ofEpochMilli(et), null,
                vol,
                Metadata.unknownRecordingMethod());
        data.add(record);
    }
}

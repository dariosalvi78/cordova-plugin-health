package org.apache.cordova.health;

import androidx.health.connect.client.aggregate.AggregateMetric;
import androidx.health.connect.client.aggregate.AggregationResult;
import androidx.health.connect.client.records.HeartRateRecord;
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord;
import androidx.health.connect.client.records.metadata.DataOrigin;
import androidx.health.connect.client.records.metadata.Metadata;
import androidx.health.connect.client.records.Record;
import androidx.health.connect.client.records.RestingHeartRateRecord;
import androidx.health.connect.client.request.AggregateGroupByDurationRequest;
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest;
import androidx.health.connect.client.request.AggregateRequest;
import androidx.health.connect.client.time.TimeRangeFilter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import kotlin.reflect.KClass;

public class HeartRateFunctions {
    public static KClass<? extends Record> dataTypeToClass() {
        return kotlin.jvm.JvmClassMappingKt.getKotlinClass(HeartRateRecord.class);
    }

    public static KClass<? extends Record> restingDataTypeToClass() {
        return kotlin.jvm.JvmClassMappingKt.getKotlinClass(RestingHeartRateRecord.class);
    }

    public static KClass<? extends Record> variabilityDataTypeToClass() {
        return kotlin.jvm.JvmClassMappingKt.getKotlinClass(HeartRateVariabilityRmssdRecord.class);
    }

    public static void populateFromQuery(Record datapoint, JSONArray resultset) throws JSONException {
        HeartRateRecord hrDP = (HeartRateRecord) datapoint;
        JSONObject hrObj = new JSONObject();

        List<HeartRateRecord.Sample> hrSamples = hrDP.getSamples();
        for (HeartRateRecord.Sample sample : hrSamples) {
            long bpm = sample.getBeatsPerMinute();
            hrObj.put("startDate", sample.getTime().toEpochMilli());
            hrObj.put("endDate",  sample.getTime().toEpochMilli());

            hrObj.put("value", bpm);
            hrObj.put("unit", "bpm");
            resultset.put(hrObj);
        }
    }

    public static void populateFromAggregatedQuery(AggregationResult response, JSONObject retObj) throws JSONException {
        if (response.get(HeartRateRecord.BPM_AVG) != null) {
            JSONObject hrStats = new JSONObject();
            long avg = response.get(HeartRateRecord.BPM_AVG);
            hrStats.put("average", avg);
            long min = response.get(HeartRateRecord.BPM_MIN);
            hrStats.put("min", min);
            long max = response.get(HeartRateRecord.BPM_MAX);
            hrStats.put("max", max);
            retObj.put("value", hrStats);
            retObj.put("unit", "bpm");
        } else {
            retObj.put("value", null);
            retObj.put("unit", "'bpm'");
        }
    }

    public static AggregateGroupByPeriodRequest prepareAggregateGroupByPeriodRequest(TimeRangeFilter timeRange, Period period, HashSet<DataOrigin> dor) {
        Set<AggregateMetric<Long>> metrics = new HashSet<>();
        metrics.add(HeartRateRecord.BPM_AVG);
        metrics.add(HeartRateRecord.BPM_MAX);
        metrics.add(HeartRateRecord.BPM_MIN);
        return new AggregateGroupByPeriodRequest(metrics, timeRange, period, dor);
    }

    public static AggregateGroupByDurationRequest prepareAggregateGroupByDurationRequest(TimeRangeFilter timeRange, Duration duration, HashSet<DataOrigin> dor) {
        Set<AggregateMetric<Long>> metrics = new HashSet<>();
        metrics.add(HeartRateRecord.BPM_AVG);
        metrics.add(HeartRateRecord.BPM_MAX);
        metrics.add(HeartRateRecord.BPM_MIN);
        return new AggregateGroupByDurationRequest(metrics, timeRange, duration, dor);
    }

    public static AggregateRequest prepareAggregateRequest(TimeRangeFilter timeRange, HashSet<DataOrigin> dor) {
        Set<AggregateMetric<Long>> metrics = new HashSet<>();
        metrics.add(HeartRateRecord.BPM_AVG);
        metrics.add(HeartRateRecord.BPM_MAX);
        metrics.add(HeartRateRecord.BPM_MIN);
        return new AggregateRequest(metrics, timeRange, dor);
    }

    public static void prepareStoreRecords(JSONObject storeObj, long st, long et, List<Record> data) throws JSONException {
        List<HeartRateRecord.Sample> samples = new LinkedList<>();

        try {
            long bpm = storeObj.getLong("value");
            HeartRateRecord.Sample sample = new HeartRateRecord.Sample(
                    Instant.ofEpochMilli(st),
                    bpm
            );
            samples.add(sample);
        } catch (JSONException ex) {
            // not a long, so maybe an array
            JSONArray bpmObjs = storeObj.getJSONArray("value");
            for (int i=0; i<bpmObjs.length(); i++) {
                JSONObject bpmObj = bpmObjs.getJSONObject(i);
                long bpm = bpmObj.getLong("bpm");
                long ts = bpmObj.getLong("timestamp");
                HeartRateRecord.Sample sample = new HeartRateRecord.Sample(
                        Instant.ofEpochMilli(ts),
                        bpm
                );
                samples.add(sample);
            }
        }

        HeartRateRecord hrRecord = new HeartRateRecord(
                Instant.ofEpochMilli(st), null,
                Instant.ofEpochMilli(et), null,
                samples,
                Metadata.unknownRecordingMethod()
        );
        data.add(hrRecord);
    }

    /* Resting Heart Rate */

    public static void populateRestingFromQuery(Record datapoint, JSONObject obj) throws JSONException {
        RestingHeartRateRecord restingHR = (RestingHeartRateRecord) datapoint;
        obj.put("startDate", restingHR.getTime().toEpochMilli());
        obj.put("endDate", restingHR.getTime().toEpochMilli());

        long bpm = (long) restingHR.getBeatsPerMinute();
        obj.put("value", bpm);
        obj.put("unit", "bpm");
    }

    public static void populateRestingFromAggregatedQuery(AggregationResult response, JSONObject retObj) throws JSONException {
        JSONObject hrStats = new JSONObject();

        if (response.get(RestingHeartRateRecord.BPM_AVG) != null) {
            long avg = response.get(RestingHeartRateRecord.BPM_AVG);
            hrStats.put("average", avg);
        }
        if (response.get(RestingHeartRateRecord.BPM_MIN) !=null) {
            long min = response.get(RestingHeartRateRecord.BPM_MIN);
            hrStats.put("min", min);
        }
        if (response.get(RestingHeartRateRecord.BPM_MAX) !=null) {
            long max = response.get(RestingHeartRateRecord.BPM_MAX);
            hrStats.put("max", max);
        }
            
        retObj.put("value", hrStats);
        retObj.put("unit", "bpm");
    }

    public static AggregateGroupByPeriodRequest prepareRestingAggregateGroupByPeriodRequest(TimeRangeFilter timeRange, Period period, HashSet<DataOrigin> dor) {
        Set<AggregateMetric<Long>> metrics = new HashSet<>();
        metrics.add(RestingHeartRateRecord.BPM_AVG);
        metrics.add(RestingHeartRateRecord.BPM_MIN);
        metrics.add(RestingHeartRateRecord.BPM_MAX);
        return new AggregateGroupByPeriodRequest(metrics, timeRange, period, dor);
    }

    public static AggregateGroupByDurationRequest prepareRestingAggregateGroupByDurationRequest(TimeRangeFilter timeRange, Duration duration, HashSet<DataOrigin> dor) {
        Set<AggregateMetric<Long>> metrics = new HashSet<>();
        metrics.add(RestingHeartRateRecord.BPM_AVG);
        metrics.add(RestingHeartRateRecord.BPM_MIN);
        metrics.add(RestingHeartRateRecord.BPM_MAX);
        return new AggregateGroupByDurationRequest(metrics, timeRange, duration, dor);
    }

    public static AggregateRequest prepareRestingAggregateRequest(TimeRangeFilter timeRange, HashSet<DataOrigin> dor) {
        Set<AggregateMetric<Long>> metrics = new HashSet<>();
        metrics.add(RestingHeartRateRecord.BPM_AVG);
        metrics.add(RestingHeartRateRecord.BPM_MIN);
        metrics.add(RestingHeartRateRecord.BPM_MAX);
        return new AggregateRequest(metrics, timeRange, dor);
    }

    public static void prepareRestingStoreRecords(JSONObject storeObj, long st, List<Record> data) throws JSONException {
        long bpm = storeObj.getLong("value");
        RestingHeartRateRecord record = new RestingHeartRateRecord(
                Instant.ofEpochMilli(st),
                ZoneOffset.from(ZonedDateTime.now()),
                bpm,
                Metadata.unknownRecordingMethod()
        );
        data.add(record);
    }

    /* Heart Rate Variability */

    public static void populateVariabilityFromQuery(Record datapoint, JSONObject obj) throws JSONException {
        HeartRateVariabilityRmssdRecord hrVariability = (HeartRateVariabilityRmssdRecord) datapoint;
        obj.put("startDate", hrVariability.getTime().toEpochMilli());
        obj.put("endDate", hrVariability.getTime().toEpochMilli());

        double rmssd = hrVariability.getHeartRateVariabilityMillis();
        obj.put("value", rmssd);
        obj.put("unit", "ms");
    }

    public static void prepareVariabilityStoreRecords(JSONObject storeObj, long st, List<Record> data) throws JSONException {
        double ms = storeObj.getDouble("value");
        HeartRateVariabilityRmssdRecord record = new HeartRateVariabilityRmssdRecord(
                Instant.ofEpochMilli(st),
                ZoneOffset.from(ZonedDateTime.now()),
                ms,
                Metadata.unknownRecordingMethod()
        );
        data.add(record);
    }

}

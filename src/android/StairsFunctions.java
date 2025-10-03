package org.apache.cordova.health;

import androidx.health.connect.client.aggregate.AggregateMetric;
import androidx.health.connect.client.aggregate.AggregationResult;
import androidx.health.connect.client.records.FloorsClimbedRecord;
import androidx.health.connect.client.records.Record;
import androidx.health.connect.client.records.metadata.DataOrigin;
import androidx.health.connect.client.records.metadata.Metadata;
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

public class StairsFunctions {
    public static KClass<? extends Record> dataTypeToClass() {
        return kotlin.jvm.JvmClassMappingKt.getKotlinClass(FloorsClimbedRecord.class);
    }

    public static void populateFromQuery(Record datapoint, JSONObject obj) throws JSONException {
        FloorsClimbedRecord floorsDP = (FloorsClimbedRecord) datapoint;
        obj.put("startDate", floorsDP.getStartTime().toEpochMilli());
        obj.put("endDate", floorsDP.getEndTime().toEpochMilli());

        Double floors = floorsDP.getFloors();
        obj.put("value", floors);
        obj.put("unit", "count");
    }

    public static void populateFromAggregatedQuery(AggregationResult response, JSONObject retObj) throws JSONException {
        if (response.get(FloorsClimbedRecord.FLOORS_CLIMBED_TOTAL) != null) {
            Double val = response.get(FloorsClimbedRecord.FLOORS_CLIMBED_TOTAL);
            retObj.put("value", val);
            retObj.put("unit", "count");
        } else {
            retObj.put("value", 0);
            retObj.put("unit", "count");
        }
    }

    public static AggregateGroupByPeriodRequest prepareAggregateGroupByPeriodRequest (TimeRangeFilter timeRange, Period period, HashSet<DataOrigin> dor) {
        Set<AggregateMetric<Double>> metrics = new HashSet<>();
        metrics.add(FloorsClimbedRecord.FLOORS_CLIMBED_TOTAL);
        return new AggregateGroupByPeriodRequest(metrics, timeRange, period, dor);
    }

    public static AggregateGroupByDurationRequest prepareAggregateGroupByDurationRequest (TimeRangeFilter timeRange, Duration duration, HashSet<DataOrigin> dor) {
        Set<AggregateMetric<Double>> metrics = new HashSet<>();
        metrics.add(FloorsClimbedRecord.FLOORS_CLIMBED_TOTAL);
        return new AggregateGroupByDurationRequest(metrics, timeRange, duration, dor);
    }

    public static AggregateRequest prepareAggregateRequest(TimeRangeFilter timeRange, HashSet<DataOrigin> dor) {
        Set<AggregateMetric<Double>> metrics = new HashSet<>();
        metrics.add(FloorsClimbedRecord.FLOORS_CLIMBED_TOTAL);
        return new AggregateRequest(metrics, timeRange, dor);
    }


    public static void prepareStoreRecords(JSONObject storeObj, long st, long et, List<Record> data) throws JSONException {
        Double floors = storeObj.getDouble("value");
        // TODO: we could add meta data when storing, including entry method, client ID and device
        FloorsClimbedRecord record = new FloorsClimbedRecord(
                Instant.ofEpochMilli(st), null,
                Instant.ofEpochMilli(et), null,
                floors,
                Metadata.EMPTY
        );
        data.add(record);
    }
}
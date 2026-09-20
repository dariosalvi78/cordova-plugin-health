package org.apache.cordova.health;

import androidx.health.connect.client.aggregate.AggregateMetric;
import androidx.health.connect.client.aggregate.AggregationResult;
import androidx.health.connect.client.records.Record;
import androidx.health.connect.client.records.WeightRecord;
import androidx.health.connect.client.records.metadata.DataOrigin;
import androidx.health.connect.client.records.metadata.Metadata;
import androidx.health.connect.client.request.AggregateGroupByDurationRequest;
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest;
import androidx.health.connect.client.request.AggregateRequest;
import androidx.health.connect.client.time.TimeRangeFilter;
import androidx.health.connect.client.units.Mass;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kotlin.reflect.KClass;

public class WeightFunctions {

    public static KClass<? extends Record> dataTypeToClass() {
        return kotlin.jvm.JvmClassMappingKt.getKotlinClass(WeightRecord.class);
    }

    public static void populateFromQuery(Record datapoint, JSONObject obj) throws JSONException {
        WeightRecord weightDP = (WeightRecord) datapoint;
        obj.put("startDate", weightDP.getTime().toEpochMilli());
        obj.put("endDate", weightDP.getTime().toEpochMilli());

        double kgs = weightDP.getWeight().getKilograms();
        obj.put("value", kgs);
        obj.put("unit", "kg");
    }

    public static void populateFromAggregatedQuery(AggregationResult response, JSONObject retObj) throws JSONException {
        JSONObject weightStats = new JSONObject();

        if (response.get(WeightRecord.WEIGHT_AVG) != null) {
            double metersAvg = response.get(WeightRecord.WEIGHT_AVG).getKilograms();
            weightStats.put("average", metersAvg);
        }
        if (response.get(WeightRecord.WEIGHT_MIN) !=null) {
            double metersMin = response.get(WeightRecord.WEIGHT_MIN).getKilograms();
            weightStats.put("min", metersMin);
        }
        if (response.get(WeightRecord.WEIGHT_MAX) !=null) {
            double metersMax = response.get(WeightRecord.WEIGHT_MAX).getKilograms();
            weightStats.put("max", metersMax);
        }
            retObj.put("value", weightStats);
            retObj.put("unit", "kg");
    }

    public static AggregateGroupByPeriodRequest prepareAggregateGroupByPeriodRequest (TimeRangeFilter timeRange, Period period, HashSet<DataOrigin> dor) {
        Set<AggregateMetric<Mass>> metrics = new HashSet<>();
        metrics.add(WeightRecord.WEIGHT_AVG);
        metrics.add(WeightRecord.WEIGHT_MAX);
        metrics.add(WeightRecord.WEIGHT_MIN);
        return new AggregateGroupByPeriodRequest(metrics, timeRange, period, dor);
    }

    public static AggregateGroupByDurationRequest prepareAggregateGroupByDurationRequest (TimeRangeFilter timeRange, Duration duration, HashSet<DataOrigin> dor) {
        Set<AggregateMetric<Mass>> metrics = new HashSet<>();
        metrics.add(WeightRecord.WEIGHT_AVG);
        metrics.add(WeightRecord.WEIGHT_MAX);
        metrics.add(WeightRecord.WEIGHT_MIN);
        return new AggregateGroupByDurationRequest(metrics, timeRange, duration, dor);
    }

    public static AggregateRequest prepareAggregateRequest(TimeRangeFilter timeRange, HashSet<DataOrigin> dor) {
        Set<AggregateMetric<Mass>> metrics = new HashSet<>();
        metrics.add(WeightRecord.WEIGHT_AVG);
        metrics.add(WeightRecord.WEIGHT_MAX);
        metrics.add(WeightRecord.WEIGHT_MIN);
        return new AggregateRequest(metrics, timeRange, dor);
    }


    public static void prepareStoreRecords(JSONObject storeObj, long st, List<Record> data) throws JSONException {
        double kgs = storeObj.getDouble("value");

        WeightRecord record = new WeightRecord(
                Instant.ofEpochMilli(st), null,
                Mass.kilograms(kgs),
                Metadata.unknownRecordingMethod()
        );
        data.add(record);
    }
}

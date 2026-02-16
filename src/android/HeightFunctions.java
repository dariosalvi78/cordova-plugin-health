package org.apache.cordova.health;

import androidx.health.connect.client.aggregate.AggregateMetric;
import androidx.health.connect.client.aggregate.AggregationResult;
import androidx.health.connect.client.records.HeightRecord;
import androidx.health.connect.client.records.Record;
import androidx.health.connect.client.records.metadata.DataOrigin;
import androidx.health.connect.client.records.metadata.Metadata;
import androidx.health.connect.client.request.AggregateGroupByDurationRequest;
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest;
import androidx.health.connect.client.request.AggregateRequest;
import androidx.health.connect.client.time.TimeRangeFilter;
import androidx.health.connect.client.units.Length;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kotlin.reflect.KClass;

public class HeightFunctions {

        public static KClass<? extends Record> dataTypeToClass() {
            return kotlin.jvm.JvmClassMappingKt.getKotlinClass(HeightRecord.class);
        }

        public static void populateFromQuery(Record datapoint, JSONObject obj) throws JSONException {
            HeightRecord heightDP = (HeightRecord) datapoint;
            obj.put("startDate", heightDP.getTime().toEpochMilli());
            obj.put("endDate", heightDP.getTime().toEpochMilli());

            double kgs = heightDP.getHeight().getMeters();
            obj.put("value", kgs);
            obj.put("unit", "m");
        }

        public static void populateFromAggregatedQuery(AggregationResult response, JSONObject retObj) throws JSONException {
            if (response.get(HeightRecord.HEIGHT_AVG) != null) {
                JSONObject weightStats = new JSONObject();
                double metersAvg = response.get(HeightRecord.HEIGHT_AVG).getMeters();
                weightStats.put("average", metersAvg);
                double metersMin = response.get(HeightRecord.HEIGHT_MIN).getMeters();
                weightStats.put("min", metersMin);
                double metersMax = response.get(HeightRecord.HEIGHT_MAX).getMeters();
                weightStats.put("max", metersMax);
                retObj.put("value", weightStats);
                retObj.put("unit", "m");
            } else {
                retObj.put("value", null);
                retObj.put("unit", "m");
            }
        }

        public static AggregateGroupByPeriodRequest prepareAggregateGroupByPeriodRequest (TimeRangeFilter timeRange, Period period, HashSet<DataOrigin> dor) {
            Set<AggregateMetric<Length>> metrics = new HashSet<>();
            metrics.add(HeightRecord.HEIGHT_AVG);
            metrics.add(HeightRecord.HEIGHT_MAX);
            metrics.add(HeightRecord.HEIGHT_MIN);
            return new AggregateGroupByPeriodRequest(metrics, timeRange, period, dor);
        }

        public static AggregateGroupByDurationRequest prepareAggregateGroupByDurationRequest (TimeRangeFilter timeRange, Duration duration, HashSet<DataOrigin> dor) {
            Set<AggregateMetric<Length>> metrics = new HashSet<>();
            metrics.add(HeightRecord.HEIGHT_AVG);
            metrics.add(HeightRecord.HEIGHT_MAX);
            metrics.add(HeightRecord.HEIGHT_MIN);
            return new AggregateGroupByDurationRequest(metrics, timeRange, duration, dor);
        }

        public static AggregateRequest prepareAggregateRequest(TimeRangeFilter timeRange, HashSet<DataOrigin> dor) {
            Set<AggregateMetric<Length>> metrics = new HashSet<>();
            metrics.add(HeightRecord.HEIGHT_AVG);
            metrics.add(HeightRecord.HEIGHT_MAX);
            metrics.add(HeightRecord.HEIGHT_MIN);
            return new AggregateRequest(metrics, timeRange, dor);
        }


        public static void prepareStoreRecords(JSONObject storeObj, long st, List<Record> data) throws JSONException {
            double meters = storeObj.getDouble("value");

            HeightRecord record = new HeightRecord(
                    Instant.ofEpochMilli(st), null,
                    Length.meters(meters),
                    Metadata.unknownRecordingMethod()
            );
            data.add(record);
        }
}

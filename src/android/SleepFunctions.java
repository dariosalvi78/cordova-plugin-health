package org.apache.cordova.health;

import androidx.health.connect.client.aggregate.AggregateMetric;
import androidx.health.connect.client.aggregate.AggregationResult;
import androidx.health.connect.client.records.Record;
import androidx.health.connect.client.records.SleepSessionRecord;
import androidx.health.connect.client.records.metadata.DataOrigin;
import androidx.health.connect.client.records.metadata.Metadata;
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import kotlin.reflect.KClass;

public class SleepFunctions {
    public static KClass<? extends Record> dataTypeToClass() {
        return kotlin.jvm.JvmClassMappingKt.getKotlinClass(SleepSessionRecord.class);
    }

    public static void populateFromQuery(Record datapoint, JSONObject obj, JSONArray resultset, boolean keepSession) throws JSONException {
        // holder of stages, only used if returning sessions
        JSONArray sleepStages = new JSONArray();

        SleepSessionRecord sleepSessR = (SleepSessionRecord) datapoint;
        obj.put("startDate", sleepSessR.getStartTime().toEpochMilli());
        obj.put("endDate", sleepSessR.getEndTime().toEpochMilli());

        if (! sleepSessR.getStages().isEmpty()) {
            for (SleepSessionRecord.Stage stage : sleepSessR.getStages()) {
                JSONObject sleepObj = new JSONObject();
                sleepObj.put("startDate", stage.getStartTime().toEpochMilli());
                sleepObj.put("endDate",  stage.getEndTime().toEpochMilli());
                int stageType = stage.getStage();
                String sleepSegmentType = "sleep";
                switch (stageType) {
                    case SleepSessionRecord.STAGE_TYPE_AWAKE:
                        sleepSegmentType = "sleep.awake";
                        break;
                    case SleepSessionRecord.STAGE_TYPE_AWAKE_IN_BED:
                        sleepSegmentType = "sleep.inBed";
                        break;
                    case SleepSessionRecord.STAGE_TYPE_DEEP:
                        sleepSegmentType = "sleep.deep";
                        break;
                    case SleepSessionRecord.STAGE_TYPE_SLEEPING:
                        sleepSegmentType = "sleep";
                        break;
                    case SleepSessionRecord.STAGE_TYPE_LIGHT:
                        sleepSegmentType = "sleep.light";
                        break;
                    case SleepSessionRecord.STAGE_TYPE_OUT_OF_BED:
                        sleepSegmentType = "sleep.outOfBed";
                        break;
                    case SleepSessionRecord.STAGE_TYPE_REM:
                        sleepSegmentType = "sleep.rem";
                        break;
                    case SleepSessionRecord.STAGE_TYPE_UNKNOWN:
                        sleepSegmentType = "sleep";
                        break;
                }

                if (keepSession) {
                    sleepObj.put("stage", sleepSegmentType);
                    sleepStages.put(sleepObj);
                } else {
                    // this is a bit of a special case where each stage becomes
                    // a separate returned value, to be compatible with HealthKit
                    // the 1 record - 1 element does not hold true here
                    HealthPlugin.populateFromMeta(sleepObj, datapoint.getMetadata());
                    sleepObj.put("value", sleepSegmentType);
                    sleepObj.put("unit", "sleep");
                    resultset.put(sleepObj);
                }
            } // end of for loop

            if (keepSession) {
                // we have the sleep stages populated in the array, return that
                obj.put("value", sleepStages);
                obj.put("unit", "sleepSession");
            }
        } else {
            // no stages! we can only assume that it's generic sleep
            if (keepSession) {
                // create a single stage object
                JSONObject sleepObj = new JSONObject();
                sleepObj.put("startDate", sleepSessR.getStartTime().toEpochMilli());
                sleepObj.put("endDate",  sleepSessR.getEndTime().toEpochMilli());
                sleepObj.put("stage", "sleep");
                sleepStages.put(sleepObj);
                obj.put("value", sleepStages);
                obj.put("unit", "sleepSession");
            } else {
                obj.put("value", "sleep");
                obj.put("unit", "sleep");
                // need to add manually to the resultset in this case
                // because it's skipped later
                resultset.put(obj);
            }
        }
    }

    public static void populateFromAggregatedQuery(AggregationResult response, JSONObject retObj) throws JSONException {
        if (response.get(SleepSessionRecord.SLEEP_DURATION_TOTAL) != null) {
            double sleepSecs = response.get(SleepSessionRecord.SLEEP_DURATION_TOTAL).getSeconds();
            retObj.put("value", sleepSecs);
            retObj.put("unit", "s");
        } else {
            retObj.put("value", 0);
            retObj.put("unit", "s");
        }
    }

    public static AggregateGroupByPeriodRequest prepareAggregateGroupByPeriodRequest (TimeRangeFilter timeRange, Period period, HashSet<DataOrigin> dor) {
        Set<AggregateMetric<Duration>> metrics = new HashSet<>();
        metrics.add(SleepSessionRecord.SLEEP_DURATION_TOTAL);
        return new AggregateGroupByPeriodRequest(metrics, timeRange, period, dor);
    }

    public static AggregateGroupByDurationRequest prepareAggregateGroupByDurationRequest (TimeRangeFilter timeRange, Duration duration, HashSet<DataOrigin> dor) {
        Set<AggregateMetric<Duration>> metrics = new HashSet<>();
        metrics.add(SleepSessionRecord.SLEEP_DURATION_TOTAL);
        return new AggregateGroupByDurationRequest(metrics, timeRange, duration, dor);
    }

    public static AggregateRequest prepareAggregateRequest(TimeRangeFilter timeRange, HashSet<DataOrigin> dor) {
        Set<AggregateMetric<Duration>> metrics = new HashSet<>();
        metrics.add(SleepSessionRecord.SLEEP_DURATION_TOTAL);
        return new AggregateRequest(metrics, timeRange, dor);
    }

    private static int sleepTypeToInt(String sleepSegmentType) {
        switch (sleepSegmentType) {
            case "sleep.awake":
                return SleepSessionRecord.STAGE_TYPE_AWAKE;
            case "sleep.inBed":
                return SleepSessionRecord.STAGE_TYPE_AWAKE_IN_BED;
            case "sleep.deep":
                return SleepSessionRecord.STAGE_TYPE_DEEP;
            case "sleep":
                return SleepSessionRecord.STAGE_TYPE_SLEEPING;
            case "sleep.light":
                return SleepSessionRecord.STAGE_TYPE_LIGHT;
            case "sleep.outOfBed":
                return SleepSessionRecord.STAGE_TYPE_OUT_OF_BED;
            case "sleep.rem":
                return SleepSessionRecord.STAGE_TYPE_REM;
        }
        return SleepSessionRecord.STAGE_TYPE_UNKNOWN;
    }

    public static void prepareStoreRecords(JSONObject storeObj, List<Record> data) throws JSONException {
        // special flag to indicate that one wants to submit an entire session
        boolean keepSession = false;
        if (storeObj.has("sleepSession")) {
            keepSession = storeObj.getBoolean("sleepSession");
        }

        if (keepSession) {
            // stages are in an array
            long sessionStart = Long.MAX_VALUE;
            long sessionEnd = Long.MIN_VALUE;
            JSONArray stagesArr = storeObj.getJSONArray("value");
            LinkedList<SleepSessionRecord.Stage> stages = new LinkedList<>();
            for (int i=0; i<stagesArr.length(); i++) {
                JSONObject stageObj = stagesArr.getJSONObject(i);
                if (!stageObj.has("startDate")) {
                    throw new JSONException("Missing startDate in stage");
                }
                long stageST = stageObj.getLong("startDate");
                if (stageST < sessionStart) sessionStart = stageST;

                if (!stageObj.has("endDate")) {
                    throw new JSONException("Missing endDate in stage");
                }
                long stageET = storeObj.getLong("endDate");
                if (stageET > sessionEnd) sessionEnd = stageET;

                String sleepType = stageObj.getString("stage");
                SleepSessionRecord.Stage stage = new SleepSessionRecord.Stage(
                        Instant.ofEpochMilli(stageST),
                        Instant.ofEpochMilli(stageET),
                        sleepTypeToInt(sleepType)
                        );
                stages.add(stage);
            }
            SleepSessionRecord sleepSession = new SleepSessionRecord(
                    Instant.ofEpochMilli(sessionStart), null,
                    Instant.ofEpochMilli(sessionEnd), null,
                    Metadata.unknownRecordingMethod(),
                    null,
                    null,
                    stages
            );
            data.add(sleepSession);

        } else {
            // one stage per session
            if (!storeObj.has("startDate")) {
                throw new JSONException("Missing startDate in stage");
            }
            long stageST = storeObj.getLong("startDate");

            if (!storeObj.has("endDate")) {
                throw new JSONException("Missing endDate in stage");
            }
            long stageET = storeObj.getLong("endDate");

            String sleepType = storeObj.getString("value");
            SleepSessionRecord.Stage stage = new SleepSessionRecord.Stage(
                    Instant.ofEpochMilli(stageST),
                    Instant.ofEpochMilli(stageET),
                    sleepTypeToInt(sleepType)
            );
            LinkedList<SleepSessionRecord.Stage> stages = new LinkedList<>();
            stages.add(stage);

            SleepSessionRecord sleepSession = new SleepSessionRecord(
                    Instant.ofEpochMilli(stageST), null,
                    Instant.ofEpochMilli(stageET), null,
                    Metadata.unknownRecordingMethod(),
                    null,
                    null,
                    stages
            );
            data.add(sleepSession);
        }
    }

}

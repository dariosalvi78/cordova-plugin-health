package org.apache.cordova.health;

import androidx.health.connect.client.aggregate.AggregateMetric;
import androidx.health.connect.client.aggregate.AggregationResult;
import androidx.health.connect.client.records.BodyTemperatureMeasurementLocation;
import androidx.health.connect.client.records.Record;
import androidx.health.connect.client.records.SkinTemperatureRecord;
import androidx.health.connect.client.records.metadata.DataOrigin;
import androidx.health.connect.client.records.metadata.Metadata;
import androidx.health.connect.client.request.AggregateGroupByDurationRequest;
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest;
import androidx.health.connect.client.request.AggregateRequest;
import androidx.health.connect.client.time.TimeRangeFilter;
import androidx.health.connect.client.units.Length;
import androidx.health.connect.client.units.Temperature;
import androidx.health.connect.client.units.TemperatureDelta;

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

public class SkinTemperatureFunctions {

    public static KClass<? extends Record> dataTypeToClass() {
        return kotlin.jvm.JvmClassMappingKt.getKotlinClass(SkinTemperatureRecord.class);
    }

    public static void populateFromQuery(Record datapoint, JSONObject obj) throws JSONException {
        SkinTemperatureRecord tempDP = (SkinTemperatureRecord) datapoint;
        obj.put("startDate", tempDP.getStartTime().toEpochMilli());
        obj.put("endDate", tempDP.getEndTime().toEpochMilli());

        JSONObject retValue = new JSONObject();

        if (tempDP.getBaseline() != null) {
            double baseline = tempDP.getBaseline().getCelsius();
            retValue.put("baseline", baseline);
        }
        JSONArray retDeltas = new JSONArray();
        retValue.put("deltas", retDeltas);
        for (SkinTemperatureRecord.Delta delta: tempDP.getDeltas()) {
            JSONObject deltaObj = new JSONObject();
            deltaObj.put("time", delta.getTime().toEpochMilli());
            deltaObj.put("temperatureDelta", delta.getDelta().getCelsius());
            retDeltas.put(deltaObj);
        }

        int location = tempDP.getMeasurementLocation();
        obj.put("value", retValue);
        obj.put("location", location);
        obj.put("unit", "C");
    }

    public static void prepareStoreRecords(JSONObject storeObj, long st, long et, List<Record> data) throws JSONException {
        JSONObject tempObj = storeObj.getJSONObject("value");
        Temperature baselineTemp = null;
        if (tempObj.has("baseline")) {
            baselineTemp = Temperature.celsius(tempObj.getDouble("baseline"));
        }

        LinkedList<SkinTemperatureRecord.Delta> deltas = new LinkedList<>();
        JSONArray deltaJsonArr = tempObj.getJSONArray("deltas");
        for(int i=0; i < deltaJsonArr.length(); i++) {
            JSONObject deltaObj = deltaJsonArr.getJSONObject(i);
            Instant timestamp = Instant.ofEpochMilli(deltaObj.getLong("time"));
            TemperatureDelta tdelta = TemperatureDelta.celsius(deltaObj.getDouble("temperatureDelta"));
            SkinTemperatureRecord.Delta delta = new SkinTemperatureRecord.Delta(timestamp, tdelta);
            deltas.add(delta);
        }


        SkinTemperatureRecord record = new SkinTemperatureRecord(
                Instant.ofEpochMilli(st), null,
                Instant.ofEpochMilli(et), null,
                Metadata.unknownRecordingMethod(),
                deltas,
                baselineTemp,
                SkinTemperatureRecord.MEASUREMENT_LOCATION_UNKNOWN
        );
        data.add(record);
    }
}

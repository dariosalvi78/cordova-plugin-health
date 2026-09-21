package org.apache.cordova.health;

import androidx.health.connect.client.aggregate.AggregateMetric;
import androidx.health.connect.client.aggregate.AggregationResult;
import androidx.health.connect.client.records.BodyTemperatureMeasurementLocation;
import androidx.health.connect.client.records.BodyTemperatureRecord;
import androidx.health.connect.client.records.Record;
import androidx.health.connect.client.records.metadata.DataOrigin;
import androidx.health.connect.client.records.metadata.Metadata;
import androidx.health.connect.client.request.AggregateGroupByDurationRequest;
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest;
import androidx.health.connect.client.request.AggregateRequest;
import androidx.health.connect.client.time.TimeRangeFilter;
import androidx.health.connect.client.units.Length;
import androidx.health.connect.client.units.Temperature;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kotlin.reflect.KClass;

public class BodyTemperatureFunctions {

    public static KClass<? extends Record> dataTypeToClass() {
        return kotlin.jvm.JvmClassMappingKt.getKotlinClass(BodyTemperatureRecord.class);
    }

    public static void populateFromQuery(Record datapoint, JSONObject obj) throws JSONException {
        BodyTemperatureRecord tempDP = (BodyTemperatureRecord) datapoint;
        obj.put("startDate", tempDP.getTime().toEpochMilli());
        obj.put("endDate", tempDP.getTime().toEpochMilli());

        double celsius = tempDP.getTemperature().getCelsius();
        int location = tempDP.getMeasurementLocation();
        obj.put("value", celsius);
        obj.put("location", location);
        obj.put("unit", "C");
    }

    public static void prepareStoreRecords(JSONObject storeObj, long st, List<Record> data) throws JSONException {
        double celsius = storeObj.getDouble("value");
        int location = BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_UNKNOWN;
        if (storeObj.has("location")) {
            location = storeObj.getInt("location");
        }

        BodyTemperatureRecord record = new BodyTemperatureRecord(
                Instant.ofEpochMilli(st), null,
                Metadata.unknownRecordingMethod(),
                Temperature.celsius(celsius),
                location
        );
        data.add(record);
    }
}

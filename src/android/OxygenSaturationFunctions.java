package org.apache.cordova.health;

import androidx.health.connect.client.records.OxygenSaturationRecord;
import androidx.health.connect.client.records.metadata.Metadata;
import androidx.health.connect.client.records.Record;
import androidx.health.connect.client.units.Percentage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.util.List;

import kotlin.reflect.KClass;

public class OxygenSaturationFunctions {
    public static KClass<? extends Record> dataTypeToClass() {
        return kotlin.jvm.JvmClassMappingKt.getKotlinClass(OxygenSaturationRecord.class);
    }

    public static void populateFromQuery(Record datapoint, JSONObject osObj) throws JSONException {
        OxygenSaturationRecord osDP = (OxygenSaturationRecord) datapoint;

       Percentage satPerc = osDP.getPercentage();

       osObj.put("startDate", osDP.getTime());
       osObj.put("endDate", osDP.getTime());
       osObj.put("saturation", satPerc.getValue());
       osObj.put("unit", "%");
    }

    public static void prepareStoreRecords(JSONObject storeObj, long st, List<Record> data) throws JSONException {
        double perc = storeObj.getDouble("value");

        OxygenSaturationRecord record = new OxygenSaturationRecord(
                Instant.ofEpochMilli(st), null,
                new Percentage(perc),
                Metadata.EMPTY
        );
        data.add(record);
    }
}

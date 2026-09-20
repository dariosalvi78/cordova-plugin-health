package org.apache.cordova.health;

import androidx.health.connect.client.records.MenstruationPeriodRecord;
import androidx.health.connect.client.records.Record;
import androidx.health.connect.client.records.metadata.Metadata;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.util.List;

import kotlin.reflect.KClass;

public class MenstruationPeriodFunctions {

    public static KClass<? extends Record> dataTypeToClass() {
        return kotlin.jvm.JvmClassMappingKt.getKotlinClass(MenstruationPeriodRecord.class);
    }

    public static void populateFromQuery(Record datapoint, JSONObject obj) throws JSONException {
        MenstruationPeriodRecord mpDP = (MenstruationPeriodRecord) datapoint;
        obj.put("startDate", mpDP.getStartTime().toEpochMilli());
        obj.put("endDate", mpDP.getEndTime().toEpochMilli());
    }


    public static void prepareStoreRecords(long st,  long et, List<Record> data) throws JSONException {
        MenstruationPeriodRecord record = new MenstruationPeriodRecord(
                Instant.ofEpochMilli(st), null,
                Instant.ofEpochMilli(et), null,
                Metadata.EMPTY
        );
        data.add(record);
    }
}

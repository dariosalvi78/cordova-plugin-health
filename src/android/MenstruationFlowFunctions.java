package org.apache.cordova.health;

import androidx.health.connect.client.records.MenstruationFlowRecord;
import androidx.health.connect.client.records.Record;
import androidx.health.connect.client.records.metadata.Metadata;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.util.List;

import kotlin.reflect.KClass;

public class MenstruationFlowFunctions {

    static public String flowIntToString(int flow) {
        switch (flow) {
            case MenstruationFlowRecord.FLOW_UNKNOWN:
                return "unknown";
            case MenstruationFlowRecord.FLOW_LIGHT:
                return "light";
            case MenstruationFlowRecord.FLOW_MEDIUM:
                return "medium";
            case MenstruationFlowRecord.FLOW_HEAVY:
                return "heavy";
            default:
                return "unknown";
        }
    }

    static public int flowStringToInt(String flowEnum) {
        switch(flowEnum) {
            case "unknown":
                return MenstruationFlowRecord.FLOW_UNKNOWN;
            case "light":
                return MenstruationFlowRecord.FLOW_LIGHT;
            case "medium":
                return MenstruationFlowRecord.FLOW_MEDIUM;
            case "heavy":
                return MenstruationFlowRecord.FLOW_HEAVY;
            default:
                return MenstruationFlowRecord.FLOW_UNKNOWN;
        }
    }

    public static KClass<? extends Record> dataTypeToClass() {
        return kotlin.jvm.JvmClassMappingKt.getKotlinClass(MenstruationFlowRecord.class);
    }

    public static void populateFromQuery(Record datapoint, JSONObject obj) throws JSONException {
        MenstruationFlowRecord mfDP = (MenstruationFlowRecord) datapoint;
        obj.put("startDate", mfDP.getTime().toEpochMilli());
        obj.put("endDate", mfDP.getTime().toEpochMilli());

        int flowEnum = mfDP.getFlow();

        obj.put("value", flowIntToString(flowEnum));
        obj.put("unit", "flow");
    }


    public static void prepareStoreRecords(JSONObject storeObj, long st, List<Record> data) throws JSONException {
        String flowEnum = storeObj.getString("value");
        int flowInt = flowStringToInt(flowEnum);
        MenstruationFlowRecord record = new MenstruationFlowRecord(
                Instant.ofEpochMilli(st), null,
                Metadata.unknownRecordingMethod(),
                flowInt
        );
        data.add(record);
    }
}

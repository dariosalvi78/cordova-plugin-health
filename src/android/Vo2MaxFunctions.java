package org.apache.cordova.health;

import androidx.health.connect.client.records.Record;
import androidx.health.connect.client.records.Vo2MaxRecord;
import androidx.health.connect.client.records.metadata.Metadata;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.util.List;

import kotlin.reflect.KClass;

public class Vo2MaxFunctions {

    static public String flowIntToString(int flow) {
        switch (flow) {
            case Vo2MaxRecord.MEASUREMENT_METHOD_OTHER:
                return "other";
            case Vo2MaxRecord.MEASUREMENT_METHOD_METABOLIC_CART:
                return "metabolic_chart";
            case Vo2MaxRecord.MEASUREMENT_METHOD_HEART_RATE_RATIO:
                return "heart_rate_ratio";
            case Vo2MaxRecord.MEASUREMENT_METHOD_COOPER_TEST:
                return "cooper_test";
            case Vo2MaxRecord.MEASUREMENT_METHOD_MULTISTAGE_FITNESS_TEST:
                return "multistage_fintess_test";
            case Vo2MaxRecord.MEASUREMENT_METHOD_ROCKPORT_FITNESS_TEST:
                return "rockport_fintess_test";
            default:
                return "other";
        }
    }

    static public int flowStringToInt(String flowEnum) {
        switch(flowEnum.toLowerCase()) {
            case "other":
                return Vo2MaxRecord.MEASUREMENT_METHOD_OTHER;
            case "metabolic_chart":
                return  Vo2MaxRecord.MEASUREMENT_METHOD_METABOLIC_CART;
            case "heart_rate_ratio" :
                return Vo2MaxRecord.MEASUREMENT_METHOD_HEART_RATE_RATIO;
            case "cooper_test":
                return Vo2MaxRecord.MEASUREMENT_METHOD_COOPER_TEST;
            case "multistage_fintess_test":
                return Vo2MaxRecord.MEASUREMENT_METHOD_MULTISTAGE_FITNESS_TEST;
            case "rockport_fintess_test":
                return Vo2MaxRecord.MEASUREMENT_METHOD_ROCKPORT_FITNESS_TEST;
            default:
                return Vo2MaxRecord.MEASUREMENT_METHOD_OTHER;
        }
    }

    public static KClass<? extends Record> dataTypeToClass() {
        return kotlin.jvm.JvmClassMappingKt.getKotlinClass(Vo2MaxRecord.class);
    }

    public static void populateFromQuery(Record datapoint, JSONObject obj) throws JSONException {
        Vo2MaxRecord vo2DP = (Vo2MaxRecord) datapoint;
        obj.put("startDate", vo2DP.getTime().toEpochMilli());
        obj.put("endDate", vo2DP.getTime().toEpochMilli());

        double vo2Flow = vo2DP.getVo2MillilitersPerMinuteKilogram();
        String method = flowIntToString(vo2DP.getMeasurementMethod());

        obj.put("value", vo2Flow);
        obj.put("method", method);
        obj.put("unit", "mL/kg/min");
    }


    public static void prepareStoreRecords(JSONObject storeObj, long st, List<Record> data) throws JSONException {
        double vo2Flow = storeObj.getDouble("value");
        int method = Vo2MaxRecord.MEASUREMENT_METHOD_OTHER;
        if (storeObj.has("method")) {
            method = flowStringToInt(storeObj.getString("method"));
        }
        Vo2MaxRecord record = new Vo2MaxRecord(
                Instant.ofEpochMilli(st), null,
                Metadata.unknownRecordingMethod(),
                vo2Flow,
                method
        );
        data.add(record);
    }
}

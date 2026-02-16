package org.apache.cordova.health;

import androidx.health.connect.client.records.BloodGlucoseRecord;
import androidx.health.connect.client.records.MealType;
import androidx.health.connect.client.records.Record;
import androidx.health.connect.client.records.metadata.Metadata;
import androidx.health.connect.client.units.BloodGlucose;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.util.List;

import kotlin.reflect.KClass;

public class BloodGlucoseFunctions {

    public static KClass<? extends Record> dataTypeToClass() {
        return kotlin.jvm.JvmClassMappingKt.getKotlinClass(BloodGlucoseRecord.class);
    }

    public static void populateFromQuery(Record datapoint, JSONObject obj) throws JSONException {
        BloodGlucoseRecord bloodGlucose = (BloodGlucoseRecord) datapoint;
        obj.put("startDate", bloodGlucose.getTime().toEpochMilli());
        obj.put("endDate", bloodGlucose.getTime().toEpochMilli());

        BloodGlucose gluco = bloodGlucose.getLevel();
        double val = gluco.getMillimolesPerLiter();

        //  { glucose: 5.5,
        //  meal: 'breakfast',
        // `meal` can be: 'before_' / 'after_' + 'meal' (iOS only), 'fasting', 'breakfast', 'dinner', 'lunch', 'snack', 'unknown'
        //  sleep: 'fully_awake',
        // `sleep` can be (iOS only): 'fully_awake', 'before_sleep', 'on_waking', 'during_sleep'
        //  source: 'capillary_blood' }
        // `source` can be: 'capillary_blood' ,'interstitial_fluid', 'plasma', 'serum', 'tears', whole_blood', 'unknown'
        JSONObject glucob = new JSONObject();
        glucob.put("glucose", val);

        int temp_to_meal = bloodGlucose.getRelationToMeal();
        String meal = "";
        if (temp_to_meal == BloodGlucoseRecord.RELATION_TO_MEAL_AFTER_MEAL) {
            meal = "after_";
        } else if (temp_to_meal == BloodGlucoseRecord.RELATION_TO_MEAL_BEFORE_MEAL) {
            meal = "before_";
        } else if (temp_to_meal == BloodGlucoseRecord.RELATION_TO_MEAL_FASTING) {
            meal = "fasting_";
        } else {
            meal = "";
        }

        temp_to_meal = bloodGlucose.getMealType();
        if (temp_to_meal == MealType.MEAL_TYPE_BREAKFAST) {
            meal += "breakfast";
        } else if (temp_to_meal == MealType.MEAL_TYPE_DINNER) {
            meal += "dinner";
        } else if (temp_to_meal == MealType.MEAL_TYPE_LUNCH) {
            meal += "lunch";
        } else if (temp_to_meal == MealType.MEAL_TYPE_SNACK) {
            meal += "snack";
        } else {
            meal += "unknown";
        }

        glucob.put("meal", meal);

        String source = "";
        int sourceInt = bloodGlucose.getSpecimenSource();
        if (sourceInt == BloodGlucoseRecord.SPECIMEN_SOURCE_INTERSTITIAL_FLUID) {
            source = "interstitial_fluid";
        } else if (sourceInt == BloodGlucoseRecord.SPECIMEN_SOURCE_CAPILLARY_BLOOD) {
            source = "capillary_blood";
        } else if (sourceInt == BloodGlucoseRecord.SPECIMEN_SOURCE_PLASMA) {
            source = "plasma";
        } else if (sourceInt == BloodGlucoseRecord.SPECIMEN_SOURCE_SERUM) {
            source = "serum";
        } else if (sourceInt == BloodGlucoseRecord.SPECIMEN_SOURCE_TEARS) {
            source = "tears";
        } else if (sourceInt == BloodGlucoseRecord.SPECIMEN_SOURCE_WHOLE_BLOOD) {
            source = "whole_blood";
        } else  {
            source = "unknown";
        }

        glucob.put("source", source);


        obj.put("value", glucob);
        obj.put("unit", "mmol/L");
    }


    public static void prepareStoreRecords(JSONObject glucoseobj, long st, List<Record> data) throws JSONException {
        double glucose = glucoseobj.getDouble("glucose");
        BloodGlucose level = BloodGlucose.millimolesPerLiter(glucose);

        int mealType = MealType.MEAL_TYPE_UNKNOWN;
        int relationToMeal = BloodGlucoseRecord.RELATION_TO_MEAL_UNKNOWN;

        if (glucoseobj.has("meal")) {
            String meal = glucoseobj.getString("meal");
            if (meal.startsWith("before_")) {
                relationToMeal = BloodGlucoseRecord.RELATION_TO_MEAL_BEFORE_MEAL;
                meal = meal.substring("before_".length());
            } else if (meal.startsWith("after_")) {
                relationToMeal = BloodGlucoseRecord.RELATION_TO_MEAL_AFTER_MEAL;
                meal = meal.substring("after_".length());
            } else if (meal.startsWith("fasting_")) {
                relationToMeal = BloodGlucoseRecord.RELATION_TO_MEAL_FASTING;
                meal = meal.substring("fasting_".length());
            }
            if (meal.equalsIgnoreCase("dinner")) {
                mealType = MealType.MEAL_TYPE_DINNER;
            } else if (meal.equalsIgnoreCase("lunch")) {
                mealType = MealType.MEAL_TYPE_LUNCH;
            } else if (meal.equalsIgnoreCase("snack")) {
                mealType = MealType.MEAL_TYPE_SNACK;
            } else if (meal.equalsIgnoreCase("breakfast")) {
                mealType = MealType.MEAL_TYPE_BREAKFAST;
            }
        }

        int specimenSource = BloodGlucoseRecord.SPECIMEN_SOURCE_UNKNOWN;
        if (glucoseobj.has("source")) {
            String source = glucoseobj.getString("source");
            if (source.equalsIgnoreCase("interstitial_fluid")) {
                specimenSource = BloodGlucoseRecord.SPECIMEN_SOURCE_INTERSTITIAL_FLUID;
            } else if (source.equalsIgnoreCase("plasma")) {
                specimenSource = BloodGlucoseRecord.SPECIMEN_SOURCE_PLASMA;
            } else if (source.equalsIgnoreCase("serum")) {
                specimenSource = BloodGlucoseRecord.SPECIMEN_SOURCE_SERUM;
            } else if (source.equalsIgnoreCase("tears")) {
                specimenSource = BloodGlucoseRecord.SPECIMEN_SOURCE_TEARS;
            } else if (source.equalsIgnoreCase("whole_blood")) {
                specimenSource = BloodGlucoseRecord.SPECIMEN_SOURCE_WHOLE_BLOOD;
            } else if (source.equalsIgnoreCase("capillary_blood")) {
                specimenSource = BloodGlucoseRecord.SPECIMEN_SOURCE_CAPILLARY_BLOOD;
            }
        }

        BloodGlucoseRecord record = new BloodGlucoseRecord(
                Instant.ofEpochMilli(st),null,
				Metadata.unknownRecordingMethod(),
                level,
                specimenSource,
                mealType,
                relationToMeal);

        data.add(record);
    }
}

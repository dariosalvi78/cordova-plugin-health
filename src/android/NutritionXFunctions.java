package org.apache.cordova.health;

import androidx.health.connect.client.aggregate.AggregateMetric;
import androidx.health.connect.client.aggregate.AggregationResult;
import androidx.health.connect.client.records.MealType;
import androidx.health.connect.client.records.NutritionRecord;
import androidx.health.connect.client.records.Record;
import androidx.health.connect.client.records.metadata.DataOrigin;
import androidx.health.connect.client.records.metadata.Metadata;
import androidx.health.connect.client.request.AggregateGroupByDurationRequest;
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest;
import androidx.health.connect.client.request.AggregateRequest;
import androidx.health.connect.client.time.TimeRangeFilter;
import androidx.health.connect.client.units.Energy;
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

public class NutritionXFunctions {

    public static KClass<? extends androidx.health.connect.client.records.Record> dataTypeToClass() {
        return kotlin.jvm.JvmClassMappingKt.getKotlinClass(NutritionRecord.class);
    }

    public static void populateFromQuery(String datatype, androidx.health.connect.client.records.Record datapoint,
            JSONObject obj) throws JSONException {
        // | nutrition.X | 234.9 <br/>**Notes**: for the unit, see the corresponding
        // type in the table above |

        NutritionRecord nutritionR = (NutritionRecord) datapoint;

        double value = 0;

        if (datatype.equalsIgnoreCase("carbs.total") && nutritionR.getTotalCarbohydrate() != null) {
            value = nutritionR.getTotalCarbohydrate().getGrams();
            obj.put("unit", "g");
        }
        if (datatype.equalsIgnoreCase("nutrition.fat.total") && nutritionR.getTotalFat() != null) {
            value = nutritionR.getTotalFat().getGrams();
            obj.put("unit", "g");
        }

        if (datatype.equalsIgnoreCase("nutrition.protein") && nutritionR.getProtein() != null) {
            value = nutritionR.getProtein().getGrams();
            obj.put("unit", "g");
        }

        if (datatype.equalsIgnoreCase("nutrition.calories") && nutritionR.getEnergy() != null) {
            value = nutritionR.getEnergy().getKilocalories();
            obj.put("unit", "kcal");
        }

        if (datatype.equalsIgnoreCase("nutrition.sugar") && nutritionR.getSugar() != null) {
            value = nutritionR.getSugar().getGrams();
            obj.put("unit", "g");
        }

        obj.put("startDate", nutritionR.getStartTime().toEpochMilli());
        obj.put("endDate", nutritionR.getEndTime().toEpochMilli());
        obj.put("value", value);
    }

    public static void populateFromAggregatedQuery(AggregationResult response, JSONObject retObj) throws JSONException {

        Double value = 0.0;

        if (response.get(NutritionRecord.ENERGY_TOTAL) != null) {
            value = response.get(NutritionRecord.ENERGY_TOTAL).getKilocalories();
            retObj.put("unit", "kcal");

        }
        if (response.get(NutritionRecord.PROTEIN_TOTAL) != null) {
            value = response.get(NutritionRecord.PROTEIN_TOTAL).getGrams();
            retObj.put("unit", "g");
        }
        if (response.get(NutritionRecord.TOTAL_FAT_TOTAL) != null) {
            value = response.get(NutritionRecord.TOTAL_FAT_TOTAL).getGrams();
            retObj.put("unit", "g");
        }
        if (response.get(NutritionRecord.TOTAL_CARBOHYDRATE_TOTAL) != null) {
            value = response.get(NutritionRecord.TOTAL_CARBOHYDRATE_TOTAL).getGrams();
            retObj.put("unit", "g");
        }
        if (response.get(NutritionRecord.SUGAR_TOTAL) != null) {
            value = response.get(NutritionRecord.SUGAR_TOTAL).getGrams();
            retObj.put("unit", "g");
        }
        retObj.put("value", value);
    }

    public static AggregateGroupByPeriodRequest prepareAggregateGroupByPeriodRequest(String datatype,
            TimeRangeFilter timeRange,
            Period period, HashSet<DataOrigin> dor) {
        Set<AggregateMetric<?>> metrics = new HashSet<>();
        if (datatype.equalsIgnoreCase("carbs.total")) {
            metrics.add(NutritionRecord.TOTAL_CARBOHYDRATE_TOTAL);
        }
        if (datatype.equalsIgnoreCase("nutrition.fat.total")) {
            metrics.add(NutritionRecord.TOTAL_FAT_TOTAL);
        }
        if (datatype.equalsIgnoreCase("nutrition.protein")) {
            metrics.add(NutritionRecord.PROTEIN_TOTAL);
        }
        if (datatype.equalsIgnoreCase("nutrition.calories")) {
            metrics.add(NutritionRecord.ENERGY_TOTAL);
        }
        if (datatype.equalsIgnoreCase("nutrition.sugar")) {
            metrics.add(NutritionRecord.SUGAR_TOTAL);
        }

        return new AggregateGroupByPeriodRequest(metrics, timeRange, period, dor);
    }

    public static AggregateGroupByDurationRequest prepareAggregateGroupByDurationRequest(String datatype,
            TimeRangeFilter timeRange,
            Duration duration, HashSet<DataOrigin> dor) {
        Set<AggregateMetric<?>> metrics = new HashSet<>();
        if (datatype.equalsIgnoreCase("carbs.total")) {
            metrics.add(NutritionRecord.TOTAL_CARBOHYDRATE_TOTAL);
        }
        if (datatype.equalsIgnoreCase("nutrition.fat.total")) {
            metrics.add(NutritionRecord.TOTAL_FAT_TOTAL);
        }
        if (datatype.equalsIgnoreCase("nutrition.protein")) {
            metrics.add(NutritionRecord.PROTEIN_TOTAL);
        }
        if (datatype.equalsIgnoreCase("nutrition.calories")) {
            metrics.add(NutritionRecord.ENERGY_TOTAL);
        }
        if (datatype.equalsIgnoreCase("nutrition.sugar")) {
            metrics.add(NutritionRecord.SUGAR_TOTAL);
        }
        return new AggregateGroupByDurationRequest(metrics, timeRange, duration, dor);
    }

    public static AggregateRequest prepareAggregateRequest(String datatype, TimeRangeFilter timeRange,
            HashSet<DataOrigin> dor) {
        Set<AggregateMetric<?>> metrics = new HashSet<>();
        if (datatype.equalsIgnoreCase("carbs.total")) {
            metrics.add(NutritionRecord.TOTAL_CARBOHYDRATE_TOTAL);
        }
        if (datatype.equalsIgnoreCase("nutrition.fat.total")) {
            metrics.add(NutritionRecord.TOTAL_FAT_TOTAL);
        }
        if (datatype.equalsIgnoreCase("nutrition.protein")) {
            metrics.add(NutritionRecord.PROTEIN_TOTAL);
        }
        if (datatype.equalsIgnoreCase("nutrition.calories")) {
            metrics.add(NutritionRecord.ENERGY_TOTAL);
        }
        if (datatype.equalsIgnoreCase("nutrition.sugar")) {
            metrics.add(NutritionRecord.SUGAR_TOTAL);
        }
        return new AggregateRequest(metrics, timeRange, dor);
    }

    public static void prepareStoreRecords(String datatype, JSONObject storeObj, long st, long et, List<Record> data)
            throws JSONException {

        int mealType = MealType.MEAL_TYPE_UNKNOWN;

        Double sugar = null;
        if (datatype.equalsIgnoreCase("nutrition.sugar")) {
            sugar = storeObj.getDouble("value");
        }
        Double fat = null;
        if (datatype.equalsIgnoreCase("nutrition.fat.total")) {
            fat = storeObj.getDouble("value");
        }
        Double protein = null;
        if (datatype.equalsIgnoreCase("nutrition.protein")) {
            protein = storeObj.getDouble("value");
        }
        Double kcal = null;
        if (datatype.equalsIgnoreCase("nutrition.calories")) {
            kcal = storeObj.getDouble("value");
        }
        Double carbs = null;
        if (datatype.equalsIgnoreCase("nutrition.carbs.total")) {
            carbs = storeObj.getDouble("value");
        }

        NutritionRecord record = new NutritionRecord(
                Instant.ofEpochMilli(st),
                null,
                Instant.ofEpochMilli(et),
                null,
                Metadata.unknownRecordingMethod(),
                null,
                null,
                null,
                kcal == null ? null : Energy.kilocalories(kcal),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                protein == null ? null : Mass.grams(protein),
                null,
                null,
                null,
                null,
                sugar == null ? null : Mass.grams(sugar),
                null,
                carbs == null ? null : Mass.grams(carbs),
                fat == null ? null : Mass.grams(fat),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                mealType);
        data.add(record);
    }
}

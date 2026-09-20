package org.apache.cordova.health;

import androidx.health.connect.client.aggregate.AggregateMetric;
import androidx.health.connect.client.aggregate.AggregationResult;
import androidx.health.connect.client.records.NutritionRecord;
import androidx.health.connect.client.records.metadata.DataOrigin;
import androidx.health.connect.client.records.metadata.Metadata;
import androidx.health.connect.client.records.Record;
import androidx.health.connect.client.request.AggregateGroupByDurationRequest;
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest;
import androidx.health.connect.client.request.AggregateRequest;
import androidx.health.connect.client.time.TimeRangeFilter;
import androidx.health.connect.client.records.MealType;
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

public class NutritionFunctions {

    public static KClass<? extends Record> dataTypeToClass() {
        return kotlin.jvm.JvmClassMappingKt.getKotlinClass(NutritionRecord.class);
    }

    public static void populateFromQuery(Record datapoint, JSONObject obj) throws JSONException {

        JSONObject nutritionObj = new JSONObject();
        // { item: "cheese", meal_type: "lunch", brand_name: "Cheddar", nutrients: {
        // 'fat.saturated': 11.5, 'calories': 233.1 } }

        NutritionRecord nutritionR = (NutritionRecord) datapoint;

        String name = nutritionR.getName();
        nutritionObj.put("item", name);

        int mealType = nutritionR.getMealType();
        if (mealType == MealType.MEAL_TYPE_BREAKFAST) {
            nutritionObj.put("meal_type", "breakfast");
        } else if (mealType == MealType.MEAL_TYPE_LUNCH) {
            nutritionObj.put("meal_type", "lunch");
        } else if (mealType == MealType.MEAL_TYPE_DINNER) {
            nutritionObj.put("meal_type", "dinner");
        } else if (mealType == MealType.MEAL_TYPE_SNACK) {
            nutritionObj.put("meal_type", "snack");
        } else {
            nutritionObj.put("meal_type", "unknown");
        }

        JSONObject nutrientsObj = new JSONObject();
        nutritionObj.put("nutrients", nutrientsObj);

        if (nutritionR.getEnergy() != null) {
            double kcal = nutritionR.getEnergy().getKilocalories();
            nutrientsObj.put("calories", kcal);
        }

        if (nutritionR.getProtein() != null) {
            double protein = nutritionR.getProtein().getGrams();
            nutrientsObj.put("protein", protein);
        }

        if (nutritionR.getTotalFat() != null) {
            double fat = nutritionR.getTotalFat().getGrams();
            nutrientsObj.put("fat.total", fat);
        }

        if (nutritionR.getTotalCarbohydrate() != null) {
            double carbs = nutritionR.getTotalCarbohydrate().getGrams();
            nutrientsObj.put("carbs.total", carbs);
        }

        if (nutritionR.getSugar() != null) {
            double sugar = nutritionR.getSugar().getGrams();
            nutrientsObj.put("sugar", sugar);
        }

        obj.put("startDate", nutritionR.getStartTime().toEpochMilli());
        obj.put("endDate", nutritionR.getEndTime().toEpochMilli());
        obj.put("value", nutritionObj);
        obj.put("unit", "nutrition");
    }

    public static void populateFromAggregatedQuery(AggregationResult response, JSONObject retObj) throws JSONException {

        JSONObject nutritionStats = new JSONObject();

        if (response.get(NutritionRecord.ENERGY_TOTAL) != null) {
            double totalEnergy = response.get(NutritionRecord.ENERGY_TOTAL).getKilocalories();
            nutritionStats.put("calories", totalEnergy);
        }

        if (response.get(NutritionRecord.PROTEIN_TOTAL) != null) {
            double totalProtein = response.get(NutritionRecord.PROTEIN_TOTAL).getGrams();
            nutritionStats.put("protein", totalProtein);
        }

        if (response.get(NutritionRecord.TOTAL_FAT_TOTAL) != null) {
            double totalFat = response.get(NutritionRecord.TOTAL_FAT_TOTAL).getGrams();
            nutritionStats.put("fat.total", totalFat);
        }

        if (response.get(NutritionRecord.TOTAL_CARBOHYDRATE_TOTAL) != null) {
            double totalCarbs = response.get(NutritionRecord.TOTAL_CARBOHYDRATE_TOTAL).getGrams();
            nutritionStats.put("carbs.total", totalCarbs);
        }

        if (response.get(NutritionRecord.SUGAR_TOTAL) != null) {
            double totalSugar = response.get(NutritionRecord.SUGAR_TOTAL).getGrams();
            nutritionStats.put("sugar", totalSugar);
        }

        retObj.put("value", nutritionStats);
        retObj.put("unit", "nutrition");
    }

    public static AggregateGroupByPeriodRequest prepareAggregateGroupByPeriodRequest(TimeRangeFilter timeRange,
            Period period, HashSet<DataOrigin> dor) {
        Set<AggregateMetric<?>> metrics = new HashSet<>();
        metrics.add(NutritionRecord.ENERGY_TOTAL);
        metrics.add(NutritionRecord.PROTEIN_TOTAL);
        metrics.add(NutritionRecord.TOTAL_FAT_TOTAL);
        metrics.add(NutritionRecord.TOTAL_CARBOHYDRATE_TOTAL);
        metrics.add(NutritionRecord.SUGAR_TOTAL);

        return new AggregateGroupByPeriodRequest(metrics, timeRange, period, dor);
    }

    public static AggregateGroupByDurationRequest prepareAggregateGroupByDurationRequest(TimeRangeFilter timeRange,
            Duration duration, HashSet<DataOrigin> dor) {
        Set<AggregateMetric<?>> metrics = new HashSet<>();
        metrics.add(NutritionRecord.ENERGY_TOTAL);
        metrics.add(NutritionRecord.PROTEIN_TOTAL);
        metrics.add(NutritionRecord.TOTAL_FAT_TOTAL);
        metrics.add(NutritionRecord.TOTAL_CARBOHYDRATE_TOTAL);
        metrics.add(NutritionRecord.SUGAR_TOTAL);
        return new AggregateGroupByDurationRequest(metrics, timeRange, duration, dor);
    }

    public static AggregateRequest prepareAggregateRequest(TimeRangeFilter timeRange, HashSet<DataOrigin> dor) {
        Set<AggregateMetric<?>> metrics = new HashSet<>();
        metrics.add(NutritionRecord.ENERGY_TOTAL);
        metrics.add(NutritionRecord.PROTEIN_TOTAL);
        metrics.add(NutritionRecord.TOTAL_FAT_TOTAL);
        metrics.add(NutritionRecord.TOTAL_CARBOHYDRATE_TOTAL);
        metrics.add(NutritionRecord.SUGAR_TOTAL);
        return new AggregateRequest(metrics, timeRange, dor);
    }

    public static void prepareStoreRecords(JSONObject storeObj, long st, long et, List<Record> data)
            throws JSONException {
        JSONObject nutritionObj = storeObj.getJSONObject("value");

        int mealType = MealType.MEAL_TYPE_UNKNOWN;

        if (nutritionObj.has("meal_type")) {
            String meal = nutritionObj.getString("meal_type");

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

        String name = nutritionObj.getString("item");

        JSONObject nutrientsObj = nutritionObj.getJSONObject("nutrients");

        Double sugar = null;
        if (nutrientsObj.has("sugar")) {
            sugar = nutrientsObj.getDouble("sugar");
        }
        Double kcal = null;
        if (nutrientsObj.has("calories")) {
            kcal = nutrientsObj.getDouble("calories");
        }
        Double protein = null;
        if (nutrientsObj.has("protein")) {
            protein = nutrientsObj.getDouble("protein");
        }
        Double fat = null;
        if (nutrientsObj.has("fat.total")) {
            fat = nutrientsObj.getDouble("fat.total");
        }
        Double carbs = null;
        if (nutrientsObj.has("carbs.total")) {
            carbs = nutrientsObj.getDouble("carbs.total");
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
                name,
                mealType);
        data.add(record);
    }

}

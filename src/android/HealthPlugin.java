/**
 * Cordova plugin that accesses the Health Connect API
 */
package org.apache.cordova.health;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.health.connect.client.HealthConnectClient;

import androidx.health.connect.client.HealthConnectFeatures;

import androidx.health.connect.client.PermissionController;
import androidx.health.connect.client.aggregate.AggregateMetric;
import androidx.health.connect.client.aggregate.AggregationResult;
import androidx.health.connect.client.aggregate.AggregationResultGroupedByDuration;
import androidx.health.connect.client.aggregate.AggregationResultGroupedByPeriod;
import androidx.health.connect.client.permission.HealthPermission;
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord;
import androidx.health.connect.client.records.BasalMetabolicRateRecord;
import androidx.health.connect.client.records.BloodGlucoseRecord;
import androidx.health.connect.client.records.BloodPressureRecord;
import androidx.health.connect.client.records.NutritionRecord;
import androidx.health.connect.client.records.BodyFatRecord;
import androidx.health.connect.client.records.DistanceRecord;
import androidx.health.connect.client.records.ExerciseLap;
import androidx.health.connect.client.records.ExerciseSegment;
import androidx.health.connect.client.records.ExerciseSessionRecord;
import androidx.health.connect.client.records.FloorsClimbedRecord;
import androidx.health.connect.client.records.HeartRateRecord;
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord;
import androidx.health.connect.client.records.HeightRecord;
import androidx.health.connect.client.records.HydrationRecord;
import androidx.health.connect.client.records.Record;
import androidx.health.connect.client.records.RestingHeartRateRecord;
import androidx.health.connect.client.records.SleepSessionRecord;
import androidx.health.connect.client.records.StepsRecord;
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord;
import androidx.health.connect.client.records.WeightRecord;
import androidx.health.connect.client.records.metadata.DataOrigin;
import androidx.health.connect.client.records.metadata.Device;
import androidx.health.connect.client.records.metadata.Metadata;
import androidx.health.connect.client.request.AggregateGroupByDurationRequest;
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest;
import androidx.health.connect.client.request.AggregateRequest;
import androidx.health.connect.client.request.ReadRecordsRequest;
import androidx.health.connect.client.response.InsertRecordsResponse;
import androidx.health.connect.client.response.ReadRecordsResponse;
import androidx.health.connect.client.time.TimeRangeFilter;
import androidx.health.connect.client.units.Energy;
import androidx.health.connect.client.units.Length;
import androidx.health.connect.client.units.Percentage;
import androidx.health.connect.client.units.Power;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import java.util.concurrent.TimeUnit;


import kotlin.coroutines.EmptyCoroutineContext;
import kotlin.reflect.KClass;
import kotlin.coroutines.Continuation;
import kotlin.jvm.functions.Function1;
import kotlin.coroutines.intrinsics.IntrinsicsKt;

import kotlin.Unit;
import kotlin.coroutines.CoroutineContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import kotlinx.coroutines.BuildersKt;


import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.OneTimeWorkRequest.Builder;
import androidx.work.WorkManager;
import androidx.work.Data;

import androidx.work.Constraints;
import androidx.work.NetworkType;

import androidx.work.WorkInfo;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.os.Handler;
import android.os.Looper;

import android.content.Context;


import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;


public class HealthPlugin extends CordovaPlugin {

    /**
     * Tag used in logs
     */
    public static String TAG = "cordova-plugin-health";
	
	// Déclarer le BroadcastReceiver comme variable de classe
	private BroadcastReceiver receiver;

	 /**
     * Callback context, reference needed when used in functions initialized before
     * the plugin is called
     */
    private CallbackContext callbackContext;

    /**
     * API client
     */
    private HealthConnectClient healthConnectClient;

    /**
     * Used to get permissions
     */
    ActivityResultLauncher permissionsLauncher;

    /**
     * Constructor
     */
    public HealthPlugin() {
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        ActivityResultContract<Set<String>, Set<String>> requestPermissionActivityContract = PermissionController
                .createRequestPermissionResultContract();
        permissionsLauncher = cordova.getActivity().registerForActivityResult(requestPermissionActivityContract,
                new ActivityResultCallback<Set<String>>() {
                    @Override
                    public void onActivityResult(Set<String> result) {
                        Log.d(TAG, "got results from authorization request");
                        if (callbackContext != null) {
                            for (String res : result) {
                                LOG.d(TAG, res);
                            }
                            if (result.isEmpty()) {
                                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, false));
                            } else {
                                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, true));
                            }
                        } else {
                            LOG.e(TAG, "Got activity results before callback was created");
                        }
                    }
                });
    }

    /**
     * Executes the request.
     *
     * @param action          the action to execute.
     * @param args            the exec() arguments.
     * @param callbackContext the callback context used when calling back into
     *                        JavaScript.
     * @return whether the action was valid.
     */
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
        if (action.equals("isAvailable")) {
            int availabilityStatus = HealthConnectClient.getSdkStatus(this.cordova.getContext());
            if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE) {
                callbackContext.error("Health Connect is not available");
                return true;
            }
            if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED) {
                callbackContext.error("Health Connect is not installed");
                return true;
            }

            callbackContext.success();
        } else if (action.equals("getHealthConnectFromStore")) {
            String uriString = "market://details?id=com.google.android.apps.healthdata";
            Intent launchAppStore = new Intent(Intent.ACTION_VIEW);
            launchAppStore.setPackage("com.android.vending");
            launchAppStore.setData(Uri.parse(uriString));
            launchAppStore.putExtra("overlay", true);
            launchAppStore.putExtra("callerId", this.cordova.getContext().getPackageName());
            this.cordova.getContext().startActivity(launchAppStore);

            callbackContext.success();
        } else if (action.equals("launchPrivacyPolicy")) {
            Activity currentActivity = this.cordova.getActivity();
            Intent activityIntent = new Intent(currentActivity, PermissionsRationaleActivity.class);
            currentActivity.startActivity(activityIntent);
            callbackContext.success();
        } else if (action.equals("openHealthSettings")) {
            Activity currentActivity = this.cordova.getActivity();
            try {
                if (Build.VERSION.SDK_INT < 34) {
                    Intent activityIntent = new Intent("androidx.health.ACTION_HEALTH_CONNECT_SETTINGS");
                    currentActivity.startActivity(activityIntent);
                    callbackContext.success();
                } else {
                    Intent activityIntent = new Intent(HealthConnectClient.getHealthConnectSettingsAction());
                    currentActivity.startActivity(activityIntent);
                    callbackContext.success();
                }
            } catch (Exception ex) {
                callbackContext.error(ex.getMessage());
            }
        } else if ("isAuthorized".equals(action)) {
            cordova.getThreadPool().execute(() -> {
                try {
                    connectAPI();
                    checkAuthorization(args, false);
                } catch (Exception ex) {
                    callbackContext.error(ex.getMessage());
                }
            });
            return true;
        } else if ("requestAuthorization".equals(action)) {
            cordova.getThreadPool().execute(() -> {
                try {
                    connectAPI();
                    checkAuthorization(args, true);
                } catch (Exception ex) {
                    callbackContext.error(ex.getMessage());
                }
            });
            return true;
		} else if ("stopBackGround".equals(action)) {
            cordova.getThreadPool().execute(() -> {
                try {
                    connectAPI();
                    stopBackGround(args);
                } catch (Exception ex) {
                    callbackContext.error(ex.getMessage());
                }
            });
            return true;
		} else if ("queryInBackGround".equals(action)) {
            cordova.getThreadPool().execute(() -> {
                try {
                    connectAPI();
                    queryInBackGround(args);
                } catch (Exception ex) {
                    callbackContext.error(ex.getMessage());
                }
            });
            return true;
        } else if ("query".equals(action)) {
            cordova.getThreadPool().execute(() -> {
                try {
                    connectAPI();
                    query(args);
                } catch (Exception ex) {
                    callbackContext.error(ex.getMessage());
                }
            });
            return true;
        } else if ("queryAggregated".equals(action)) {
            cordova.getThreadPool().execute(() -> {
                try {
                    connectAPI();
                    queryAggregated(args);
                } catch (Exception ex) {
                    callbackContext.error(ex.getMessage());
                }
            });
            return true;
        } else if ("store".equals(action)) {
            cordova.getThreadPool().execute(() -> {
                try {
                    connectAPI();
                    store(args);
                } catch (Exception ex) {
                    callbackContext.error(ex.getMessage());
                }
            });
            return true;
        } else if ("delete".equals(action)) {
            cordova.getThreadPool().execute(() -> {
                try {
                    connectAPI();
                    delete(args);
                } catch (Exception ex) {
                    callbackContext.error(ex.getMessage());
                }
            });
            return true;
        } else {
            // Unsupported action
            return false;
        }
        return true;
    }

    /**
     * Connects to the HealthConnect API
     */
    private void connectAPI() {
        if (healthConnectClient == null) {
            healthConnectClient = HealthConnectClient.getOrCreate(cordova.getContext());
        }
    }

    // DATA_TYPE add here when supporting new ones
    private KClass<? extends androidx.health.connect.client.records.Record> dataTypeNameToClass(String name) {
        if (name.equalsIgnoreCase("steps")) {
            return StepsFunctions.dataTypeToClass();
        }
        if (name.equalsIgnoreCase("stairs")) {
            return StairsFunctions.dataTypeToClass();
        }
        if (name.equalsIgnoreCase("weight")) {
            return WeightFunctions.dataTypeToClass();
        }
        if (name.equalsIgnoreCase("fat_percentage")) {
            return kotlin.jvm.JvmClassMappingKt.getKotlinClass(BodyFatRecord.class);
        }
        if (name.equalsIgnoreCase("activity")) {
            return kotlin.jvm.JvmClassMappingKt.getKotlinClass(ExerciseSessionRecord.class);
        }
        if (name.equalsIgnoreCase("calories")) {
            return kotlin.jvm.JvmClassMappingKt.getKotlinClass(TotalCaloriesBurnedRecord.class);
        }
        if (name.equalsIgnoreCase("calories.active")) {
            return kotlin.jvm.JvmClassMappingKt.getKotlinClass(ActiveCaloriesBurnedRecord.class);
        }
        if (name.equalsIgnoreCase("calories.basal")) {
            return kotlin.jvm.JvmClassMappingKt.getKotlinClass(BasalMetabolicRateRecord.class);
        }
        if (name.equalsIgnoreCase("blood_glucose")) {
            return BloodGlucoseFunctions.dataTypeToClass();
        }
        if (name.equalsIgnoreCase("blood_pressure")) {
            return BloodPressureFunctions.dataTypeToClass();
        }
        if (name.equalsIgnoreCase("distance")) {
            return kotlin.jvm.JvmClassMappingKt.getKotlinClass(DistanceRecord.class);
        }
        if (name.equalsIgnoreCase("heart_rate.resting")) {
            return HeartRateFunctions.restingDataTypeToClass();
        }
        if (name.equalsIgnoreCase("heart_rate.variability")) {
            return HeartRateFunctions.variabilityDataTypeToClass();
        }
        if (name.equalsIgnoreCase("height")) {
            return HeightFunctions.dataTypeToClass();
        }
        if (name.equalsIgnoreCase("nutrition")) {
            return NutritionFunctions.dataTypeToClass();
        }
        if (name.equalsIgnoreCase("nutrition.water")) {
            return HydrationFunctions.dataTypeToClass();
        }
        if (name.toLowerCase().startsWith("nutrition.")) {
            return NutritionXFunctions.dataTypeToClass();
        }
        if (name.equalsIgnoreCase("sleep")) {
            return SleepFunctions.dataTypeToClass();
        }
        if (name.equalsIgnoreCase("heart_rate")) {
            return HeartRateFunctions.dataTypeToClass();
        }

        return null;
    }

    /**
     * Checks if permissions have been granted, if request is true, permissions are
     * also requested
     *
     * @param args    json array coming from the plugin
     * @param request if true also requests permissions
     */
    private void checkAuthorization(final JSONArray args, boolean request) {
        try {
            Log.d(TAG, "checking authorization");
            JSONObject readWriteObj = args.getJSONObject(0);

            // see https://kt.academy/article/cc-other-languages
            Set<String> grantedPermissions = BuildersKt.runBlocking(
                    EmptyCoroutineContext.INSTANCE,
                    (s, c) -> healthConnectClient.getPermissionController().getGrantedPermissions(c));

            Set<String> permissionsToRequest = new HashSet<>();

            if (readWriteObj.has("read")) {
                JSONArray readArray = readWriteObj.getJSONArray("read");
                for (int j = 0; j < readArray.length(); j++) {
                    String dataTypeStr = readArray.getString(j);
                    KClass<? extends Record> datatype = dataTypeNameToClass(dataTypeStr);
                    if (datatype == null) {
                        callbackContext.error("Data type not supported " + dataTypeStr);
                        return;
                    }
                    String perm = HealthPermission.getReadPermission(datatype);
                    if (!grantedPermissions.contains(perm)) {
                        if (request) {
                            permissionsToRequest.add(perm);
                        } else {
                            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, false));
                            return;
                        }
                    }
                }
            }
            if (readWriteObj.has("write")) {
                JSONArray writeArray = readWriteObj.getJSONArray("write");
                for (int j = 0; j < writeArray.length(); j++) {
                    String dataTypeStr = writeArray.getString(j);
                    KClass<? extends Record> datatype = dataTypeNameToClass(dataTypeStr);
                    if (datatype == null) {
                        callbackContext.error("Data type not supported " + dataTypeStr);
                        return;
                    }
                    String perm = HealthPermission.getWritePermission(datatype);
                    if (!grantedPermissions.contains(perm)) {
                        if (request) {
                            permissionsToRequest.add(perm);
                        } else {
                            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, false));
                            return;
                        }
                    }
                }
            }

            if (request && !permissionsToRequest.isEmpty()) {
                Log.d(TAG, "requesting authorization");
                permissionsLauncher.launch(permissionsToRequest);
            } else {
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, true));
            }
        } catch (JSONException ex) {
            callbackContext.error("Cannot read request object" + ex.getMessage());
        } catch (InterruptedException ex2) {
            callbackContext.error("Thread interrupted" + ex2.getMessage());
        }
    }

    protected static void populateFromMeta(JSONObject obj, Metadata meta) throws JSONException {
        String id = meta.getId();
        if (id != null) {
            obj.put("id", id);
        }

        Device dev = meta.getDevice();
        if (dev != null) {
            String manufacturer = dev.getManufacturer();
            String model = dev.getModel();
            if (manufacturer != null || model != null) {
                obj.put("sourceDevice", manufacturer + " " + model);
            }
        }

        DataOrigin origin = meta.getDataOrigin();
        if (origin != null) {
            obj.put("sourceBundleId", origin.getPackageName());
        }

        int methodInt = meta.getRecordingMethod();
        String method = "unknown";
        switch (methodInt) {
            case 1:
                method = "actively_recorded";
                break;
            case 2:
                method = "automatically_recorded";
                break;
            case 3:
                method = "manual_entry";
                break;
        }
        obj.put("entryMethod", method);
    }
	
	private void stopBackGround (final JSONArray args) {
		try {

			Log.d(TAG, "stopBackGround called");
			cleanup();	

		} catch (Exception ex) {
            Log.e(TAG, "stopBackGround is not possible", ex);
            callbackContext.error("stopBackGround is not possible");
        }
	}

	private void queryInBackGround (final JSONArray args) {
	
		try {
            if (!args.getJSONObject(0).has("startDate")) {
                callbackContext.error("Missing argument startDate");
                return;
            }
            long st = args.getJSONObject(0).getLong("startDate");
			
            if (!args.getJSONObject(0).has("endDate")) {
                callbackContext.error("Missing argument endDate");
                return;
            }
            long et = args.getJSONObject(0).getLong("endDate");
			
            if (!args.getJSONObject(0).has("dataType")) {
                callbackContext.error("Missing argument dataType");
                return;
            }
            String datatype = args.getJSONObject(0).getString("dataType");
            KClass<? extends Record> dt = dataTypeNameToClass(datatype);
			
            if (dt == null) {
                callbackContext.error("Datatype " + datatype + " not supported");
                return;
            }
			
			int limit = 1000;
            if (args.getJSONObject(0).has("limit")) {
                limit = args.getJSONObject(0).getInt("limit");
            }
            boolean ascending = false;
            if (args.getJSONObject(0).has("ascending")) {
                ascending = args.getJSONObject(0).getBoolean("ascending");
            }
			
			
			if (healthConnectClient.getFeatures().getFeatureStatus(HealthConnectFeatures.FEATURE_READ_HEALTH_DATA_IN_BACKGROUND)
                == HealthConnectFeatures.FEATURE_STATUS_AVAILABLE) {
				
				// Check if necessary permission is granted
				Set<String> grantedPermissions = BuildersKt.runBlocking(
                    EmptyCoroutineContext.INSTANCE,
                    (s, c) -> healthConnectClient.getPermissionController().getGrantedPermissions(c));

				if (!grantedPermissions.contains("android.permission.health.READ_HEALTH_DATA_IN_BACKGROUND")) {
					// Perform read in foreground
					callbackContext.error("You do not authorize background request");
					return;
				} else {
					
					Log.d(TAG, "Data queryInBackGround successful");
					
					initBroadcastReceiver(); // Enregistrer le BroadcastReceiver
					
					//Log.d(TAG, "Après initBroadcastReceiver");
					
					
					Constraints constraints = new Constraints.Builder()
						.setRequiredNetworkType(NetworkType.NOT_REQUIRED) // Pas besoin de réseau
						.setRequiresBatteryNotLow(false) // Pas besoin que la batterie soit suffisante
						.setRequiresStorageNotLow(false) // Pas besoin que l'espace de stockage soit suffisant
						.setRequiresCharging(false) // Pas besoin que l'appareil soit en charge
						.build();

					Data inputData = new Data.Builder()
										.putString("DATA_TYPE", dt.toString())
										.putLong("TIME_START", st)
										.putLong("TIME_END", et)
										.putInt("DATA_LIMIT", limit)
										.putBoolean("DATA_ASCENDING", ascending)
										.build();
										
					
					
					// Création d'un OneTimeWorkRequest
					OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(ScheduleWorker.class)
						.setConstraints(constraints)
						.setInputData(inputData)
						.build();

					// Planification du Worker
					WorkManager.getInstance(cordova.getContext().getApplicationContext())
						.enqueue(oneTimeWorkRequest); // Pas besoin de tag ou de politique pour un OneTimeWorkRequest
					
						
				}		
								
			} else {
				// Background reading is not available, perform read in foreground
				callbackContext.error("Your version of Android does not support background data reading.");
				return;
			}
			
			
			
		} catch (JSONException ex) {
            Log.e(TAG, "Could not parse query object", ex);
            callbackContext.error("Could not parse query object");
        } catch (InterruptedException ex2) {
            Log.e(TAG, "Thread interrupted", ex2);
            callbackContext.error("Thread interrupted" + ex2.getMessage());
        }
	
	}
	
	
	// Méthode pour initialiser le BroadcastReceiver
	private void initBroadcastReceiver() {
		
		try {
			receiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					String jsonResult = intent.getStringExtra("json_result");
					if (jsonResult != "") {
						try {
							JSONArray resultset = new JSONArray(jsonResult);
							Log.d(TAG, "JSON Reçu, taille : " + resultset.length());
							callbackContext.success(resultset);					
						} catch (JSONException e) {
							Log.e(TAG, "Erreur de parsing JSON : ", e);
							callbackContext.error("Erreur de parsing JSON : " + e.getMessage());
						} finally {
							cleanup(); // Nettoyage après traitement
						}
					}
					else {
						callbackContext.success();
						Log.d(TAG, "JSON Reçu, aucun résultat");
						cleanup();
					}
				}
			};
			
			if (cordova.getActivity() != null) {
				LocalBroadcastManager.getInstance(cordova.getActivity().getApplicationContext())
					.registerReceiver(receiver, new IntentFilter("cordova-plugin-health.SW_WORK_COMPLETE"));
			} else {
				Log.e(TAG, "Aucune activité disponible pour enregistrer le BroadcastReceiver");
			}
				
		
		} catch (Exception e) {
			cleanup();
			Log.e(TAG, "Erreur: ", e);
			callbackContext.error("Erreur: " + e.getMessage());
		}

	}

	// Méthode pour nettoyer (à appeler quand nécessaire)
	private void cleanup() {
		if (receiver != null) {
			
			LocalBroadcastManager.getInstance(cordova.getActivity().getApplicationContext())
				.unregisterReceiver(receiver);
			
			receiver = null;
			
		}		
	}

    private void query(final JSONArray args) {

        try {
            if (!args.getJSONObject(0).has("startDate")) {
                callbackContext.error("Missing argument startDate");
                return;
            }
            long st = args.getJSONObject(0).getLong("startDate");
            if (!args.getJSONObject(0).has("endDate")) {
                callbackContext.error("Missing argument endDate");
                return;
            }
            long et = args.getJSONObject(0).getLong("endDate");
            if (!args.getJSONObject(0).has("dataType")) {
                callbackContext.error("Missing argument dataType");
                return;
            }
            String datatype = args.getJSONObject(0).getString("dataType");
            KClass<? extends Record> dt = dataTypeNameToClass(datatype);
            if (dt == null) {
                callbackContext.error("Datatype " + datatype + " not supported");
                return;
            }

            int limit = 1000;
            if (args.getJSONObject(0).has("limit")) {
                limit = args.getJSONObject(0).getInt("limit");
            }
            boolean ascending = false;
            if (args.getJSONObject(0).has("ascending")) {
                ascending = args.getJSONObject(0).getBoolean("ascending");
            }

            // special flags:
            boolean keepSession = false;
            if (args.getJSONObject(0).has("sleepSession")) {
                keepSession = args.getJSONObject(0).getBoolean("sleepSession");
            }

            if (this.healthConnectClient == null) {
                callbackContext.error("You must call requestAuthorization() before query()");
                return;
            }

            TimeRangeFilter timeRange = TimeRangeFilter.between(Instant.ofEpochMilli(st), Instant.ofEpochMilli(et));
            HashSet<DataOrigin> dor = new HashSet<>();
            ReadRecordsRequest request = new ReadRecordsRequest(dt, timeRange, dor, ascending, limit, null);
            // see https://kt.academy/article/cc-other-languages
            ReadRecordsResponse response = BuildersKt.runBlocking(
                    EmptyCoroutineContext.INSTANCE,
                    (s, c) -> healthConnectClient.readRecords(request, c));

            Log.d(TAG, "Data query successful");
            JSONArray resultset = new JSONArray();
            // default behaviour is that each record corresponds to one element in the
            // array, but there can be exceptions
            boolean oneElementPerRecord = true;

            for (Object datapointObj : response.getRecords()) {
                if (datapointObj instanceof androidx.health.connect.client.records.Record) {
                    androidx.health.connect.client.records.Record datapoint = (androidx.health.connect.client.records.Record) datapointObj;
                    JSONObject obj = new JSONObject();

                    populateFromMeta(obj, datapoint.getMetadata());

                    // DATA_TYPES here we need to add support for each different data type
                    if (datapoint instanceof StepsRecord) {
                        StepsFunctions.populateFromQuery(datapoint, obj);
                    } else if (datapoint instanceof FloorsClimbedRecord) {
                        StairsFunctions.populateFromQuery(datapoint, obj);
                    } else if (datapoint instanceof WeightRecord) {
                        WeightFunctions.populateFromQuery(datapoint, obj);
                    } else if (datapoint instanceof HeightRecord) {
                        HeightFunctions.populateFromQuery(datapoint, obj);
                    } else if (datapoint instanceof BodyFatRecord) {
                        BodyFatRecord bodyFatDP = (BodyFatRecord) datapoint;
                        obj.put("startDate", bodyFatDP.getTime().toEpochMilli());
                        obj.put("endDate", bodyFatDP.getTime().toEpochMilli());

                        double perc = bodyFatDP.getPercentage().getValue();
                        obj.put("value", perc);
                        obj.put("unit", "%");
                    } else if (datapoint instanceof ExerciseSessionRecord) {
                        ExerciseSessionRecord activityDP = (ExerciseSessionRecord) datapoint;
                        obj.put("startDate", activityDP.getStartTime().toEpochMilli());
                        obj.put("endDate", activityDP.getEndTime().toEpochMilli());

                        int exType = activityDP.getExerciseType();
                        String activityStr = ActivityMapper.activityFromExerciseType(exType);

                        obj.put("value", activityStr);
                        obj.put("unit", "activityType");
                    } else if (datapoint instanceof TotalCaloriesBurnedRecord) {
                        TotalCaloriesBurnedRecord caloriesDP = (TotalCaloriesBurnedRecord) datapoint;
                        obj.put("startDate", caloriesDP.getStartTime().toEpochMilli());
                        obj.put("endDate", caloriesDP.getEndTime().toEpochMilli());

                        double kcals = caloriesDP.getEnergy().getKilocalories();

                        obj.put("value", kcals);
                        obj.put("unit", "kcal");
                    } else if (datapoint instanceof ActiveCaloriesBurnedRecord) {
                        ActiveCaloriesBurnedRecord caloriesDP = (ActiveCaloriesBurnedRecord) datapoint;
                        obj.put("startDate", caloriesDP.getStartTime().toEpochMilli());
                        obj.put("endDate", caloriesDP.getEndTime().toEpochMilli());

                        double kcals = caloriesDP.getEnergy().getKilocalories();

                        obj.put("value", kcals);
                        obj.put("unit", "kcal");
                    } else if (datapoint instanceof BasalMetabolicRateRecord) {
                        BasalMetabolicRateRecord basalRateDP = (BasalMetabolicRateRecord) datapoint;
                        obj.put("startDate", basalRateDP.getTime().toEpochMilli());
                        obj.put("endDate", basalRateDP.getTime().toEpochMilli());

                        Power pow = basalRateDP.getBasalMetabolicRate();
                        obj.put("value", pow.getKilocaloriesPerDay());
                        obj.put("unit", "kcal/day");
                    } else if (datapoint instanceof BloodGlucoseRecord) {
                        BloodGlucoseFunctions.populateFromQuery(datapoint, obj);
                    } else if (datapoint instanceof BloodPressureRecord) {
                        BloodPressureFunctions.populateFromQuery(datapoint, obj);
                    } else if (datapoint instanceof DistanceRecord) {
                        DistanceRecord disanceR = (DistanceRecord) datapoint;
                        obj.put("startDate", disanceR.getStartTime().toEpochMilli());
                        obj.put("endDate", disanceR.getEndTime().toEpochMilli());

                        double meters = disanceR.getDistance().getMeters();
                        obj.put("value", meters);
                        obj.put("unit", "m");
                    } else if (datapoint instanceof HydrationRecord) {
                        HydrationFunctions.populateFromQuery(datapoint, obj);
                    } else if (datapoint instanceof NutritionRecord) {
                        if (datatype.toLowerCase().startsWith("nutrition.")) {
                            NutritionXFunctions.populateFromQuery(datatype, datapoint, obj);
                        } else {
                            NutritionFunctions.populateFromQuery(datapoint, obj);
                        }
                    } else if (datapoint instanceof SleepSessionRecord) {
                        oneElementPerRecord = keepSession; // flag it, so we don't add empty objs later
                        SleepFunctions.populateFromQuery(datapoint, obj, resultset, keepSession);
                    } else if (datapoint instanceof HeartRateRecord) {
                        oneElementPerRecord = false; // bpms are sent individually
                        HeartRateFunctions.populateFromQuery(datapoint, resultset);
                    } else if (datapoint instanceof RestingHeartRateRecord) {
                        HeartRateFunctions.populateRestingFromQuery(datapoint, obj);
                    } else if (datapoint instanceof HeartRateVariabilityRmssdRecord) {
                        HeartRateFunctions.populateVariabilityFromQuery(datapoint, obj);
                    } else {
                        callbackContext.error("Sample received of unknown type " + datatype.toString());
                        return;
                    }

                    // add to result array
                    if (oneElementPerRecord) {
                        resultset.put(obj);
                    }
                } else {
                    Log.e(TAG, "Unrecognized type for record " + datapointObj.getClass());
                }
            }
            // done:
            callbackContext.success(resultset);
        } catch (JSONException ex) {
            Log.e(TAG, "Could not parse query object", ex);
            callbackContext.error("Could not parse query object");
        } catch (InterruptedException ex2) {
            Log.e(TAG, "Thread interrupted", ex2);
            callbackContext.error("Thread interrupted" + ex2.getMessage());
        }
    }

    private void queryAggregated(final JSONArray args) {
        try {
            if (!args.getJSONObject(0).has("startDate")) {
                callbackContext.error("Missing argument startDate");
                return;
            }
            long st = args.getJSONObject(0).getLong("startDate");

            if (!args.getJSONObject(0).has("endDate")) {
                callbackContext.error("Missing argument endDate");
                return;
            }
            long et = args.getJSONObject(0).getLong("endDate");

            if (!args.getJSONObject(0).has("dataType")) {
                callbackContext.error("Missing argument dataType");
                return;
            }
            String datatype = args.getJSONObject(0).getString("dataType");
            KClass<? extends Record> dt = dataTypeNameToClass(datatype);
            if (dt == null) {
                callbackContext.error("Datatype " + datatype + " not supported");
                return;
            }

            boolean hasbucket = args.getJSONObject(0).has("bucket");

            HashSet<DataOrigin> dor = new HashSet<>();

            if (hasbucket) {
                String bucketType = args.getJSONObject(0).getString("bucket");
                ZonedDateTime stZDT = ZonedDateTime.ofInstant(Instant.ofEpochMilli(st), ZoneId.systemDefault());
                ZonedDateTime etZDT = ZonedDateTime.ofInstant(Instant.ofEpochMilli(et), ZoneId.systemDefault());
                // reset unused fields
                // int year, Month month, int dayOfMonth, int hour, int minute, int second
                LocalDateTime stLDT;
                LocalDateTime etLDT = LocalDateTime.from(etZDT);
                if (bucketType.equalsIgnoreCase("hour")) {
                    stLDT = LocalDateTime.of(stZDT.getYear(), stZDT.getMonth(), stZDT.getDayOfMonth(), stZDT.getHour(),
                            0, 0, 0);
                    // etLDT = LocalDateTime.of(etZDT.getYear(), etZDT.getMonth(),
                    // etZDT.getDayOfMonth(), etZDT.getHour(), 0, 0, 0);
                } else if (bucketType.equalsIgnoreCase("day")) {
                    stLDT = LocalDateTime.of(stZDT.getYear(), stZDT.getMonth(), stZDT.getDayOfMonth(), 0, 0, 0, 0);
                    // etLDT = LocalDateTime.of(etZDT.getYear(), etZDT.getMonth(),
                    // etZDT.getDayOfMonth(), 0, 0, 0, 0);
                } else if (bucketType.equalsIgnoreCase("week")) {
                    DayOfWeek weekStart = DayOfWeek.MONDAY;
                    stLDT = LocalDateTime.of(stZDT.getYear(), stZDT.getMonth(), stZDT.getDayOfMonth(), 0, 0, 0, 0)
                            .with(TemporalAdjusters.previousOrSame(weekStart));
                    // etLDT = LocalDateTime.of(etZDT.getYear(), etZDT.getMonth(),
                    // etZDT.getDayOfMonth(), 0, 0, 0,
                    // 0).with(TemporalAdjusters.previousOrSame(weekStart));
                } else if (bucketType.equalsIgnoreCase("month")) {
                    stLDT = LocalDateTime.of(stZDT.getYear(), stZDT.getMonth(), 1, 0, 0, 0, 0);
                    // etLDT = LocalDateTime.of(etZDT.getYear(), etZDT.getMonth(), 1, 0, 0, 0, 0);
                } else if (bucketType.equalsIgnoreCase("year")) {
                    stLDT = LocalDateTime.of(stZDT.getYear(), 1, 1, 0, 0, 0, 0);
                    // etLDT = LocalDateTime.of(etZDT.getYear(), 1, 1, 0, 0, 0, 0);
                } else {
                    callbackContext.error("Bucket not recognized " + bucketType);
                    return;
                }
                TimeRangeFilter timeRange = TimeRangeFilter.between(stLDT, etLDT);

                Duration duration = null;
                Period period = null;
                if (bucketType.equalsIgnoreCase("hour")) {
                    duration = Duration.ofHours(1);
                } else if (bucketType.equalsIgnoreCase("day")) {
                    period = Period.ofDays(1);
                } else if (bucketType.equalsIgnoreCase("week")) {
                    period = Period.ofWeeks(1);
                } else if (bucketType.equalsIgnoreCase("month")) {
                    period = Period.ofMonths(1);
                } else if (bucketType.equalsIgnoreCase("year")) {
                    period = Period.ofYears(1);
                } else {
                    callbackContext.error("Bucket length not recognized " + bucketType);
                    return;
                }
                if (period != null) {
                    AggregateGroupByPeriodRequest request;
                    // DATA_TYPE: add here support for new data types
                    if (datatype.equalsIgnoreCase("steps")) {
                        request = StepsFunctions.prepareAggregateGroupByPeriodRequest(timeRange, period, dor);
                    } else if (datatype.equalsIgnoreCase("stairs")) {
                        request = StairsFunctions.prepareAggregateGroupByPeriodRequest(timeRange, period, dor);
                    } else if (datatype.equalsIgnoreCase("nutrition")) {
                        request = NutritionFunctions.prepareAggregateGroupByPeriodRequest(timeRange, period, dor);
                    } else if (datatype.equalsIgnoreCase("nutrition.water")) {
                        request = HydrationFunctions.prepareAggregateGroupByPeriodRequest(timeRange, period, dor);
                    } else if (datatype.toLowerCase().startsWith("nutrition.")) {
                        request = NutritionXFunctions.prepareAggregateGroupByPeriodRequest(datatype, timeRange, period,
                                dor);
                    } else if (datatype.equalsIgnoreCase("weight")) {
                        request = WeightFunctions.prepareAggregateGroupByPeriodRequest(timeRange, period, dor);
                    } else if (datatype.equalsIgnoreCase("height")) {
                        request = HeightFunctions.prepareAggregateGroupByPeriodRequest(timeRange, period, dor);
                    } else if (datatype.equalsIgnoreCase("activity")) {
                        Set<AggregateMetric<Duration>> metrics = new HashSet<>();
                        metrics.add(ExerciseSessionRecord.EXERCISE_DURATION_TOTAL);
                        request = new AggregateGroupByPeriodRequest(metrics, timeRange, period, dor);
                    } else if (datatype.equalsIgnoreCase("calories")) {
                        Set<AggregateMetric<Energy>> metrics = new HashSet<>();
                        metrics.add(TotalCaloriesBurnedRecord.ENERGY_TOTAL);
                        request = new AggregateGroupByPeriodRequest(metrics, timeRange, period, dor);
                    } else if (datatype.equalsIgnoreCase("calories.active")) {
                        Set<AggregateMetric<Energy>> metrics = new HashSet<>();
                        metrics.add(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL);
                        request = new AggregateGroupByPeriodRequest(metrics, timeRange, period, dor);
                    } else if (datatype.equalsIgnoreCase("calories.basal")) {
                        Set<AggregateMetric<Energy>> metrics = new HashSet<>();
                        metrics.add(BasalMetabolicRateRecord.BASAL_CALORIES_TOTAL);
                        request = new AggregateGroupByPeriodRequest(metrics, timeRange, period, dor);
                    } else if (datatype.equalsIgnoreCase("distance")) {
                        Set<AggregateMetric<Length>> metrics = new HashSet<>();
                        metrics.add(DistanceRecord.DISTANCE_TOTAL);
                        request = new AggregateGroupByPeriodRequest(metrics, timeRange, period, dor);
                    } else if (datatype.equalsIgnoreCase("sleep")) {
                        request = SleepFunctions.prepareAggregateGroupByPeriodRequest(timeRange, period, dor);
                    } else if (datatype.equalsIgnoreCase("heart_rate")) {
                        request = HeartRateFunctions.prepareAggregateGroupByPeriodRequest(timeRange, period, dor);
                    } else if (datatype.equalsIgnoreCase("heart_rate.resting")) {
                        request = HeartRateFunctions.prepareRestingAggregateGroupByPeriodRequest(timeRange, period,
                                dor);
                    } else if (datatype.equalsIgnoreCase("blood_pressure")) {
                        request = BloodPressureFunctions.prepareAggregateGroupByPeriodRequest(timeRange, period, dor);
                    } else {
                        callbackContext.error("Datatype not recognized " + datatype);
                        return;
                    }

                    List<AggregationResultGroupedByPeriod> response = BuildersKt.runBlocking(
                            EmptyCoroutineContext.INSTANCE,
                            (s, c) -> healthConnectClient.aggregateGroupByPeriod(request, c));

                    Log.d(TAG, "Got data from query aggregated");
                    JSONArray retBucketsArr = new JSONArray();

                    for (AggregationResultGroupedByPeriod bucket : response) {
                        JSONObject retObject = new JSONObject();
                        long stbkt = bucket.getStartTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                        long etbkt = bucket.getEndTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                        retObject.put("startDate", stbkt);
                        retObject.put("endDate", etbkt);
                        setAggregatedVal(datatype, retObject, bucket.getResult());

                        retBucketsArr.put(retObject);
                    }

                    callbackContext.success(retBucketsArr);
                } else {
                    AggregateGroupByDurationRequest request;
                    // DATA_TYPE: add here support for new data types
                    if (datatype.equalsIgnoreCase("steps")) {
                        request = StepsFunctions.prepareAggregateGroupByDurationRequest(timeRange, duration, dor);
                    } else if (datatype.equalsIgnoreCase("stairs")) {
                        request = StairsFunctions.prepareAggregateGroupByDurationRequest(timeRange, duration, dor);
                    } else if (datatype.equalsIgnoreCase("nutrition")) {
                        request = NutritionFunctions.prepareAggregateGroupByDurationRequest(timeRange, duration, dor);
                    } else if (datatype.equalsIgnoreCase("nutrition.water")) {
                        request = HydrationFunctions.prepareAggregateGroupByDurationRequest(timeRange, duration, dor);
                    } else if (datatype.toLowerCase().startsWith("nutrition.")) {
                        request = NutritionXFunctions.prepareAggregateGroupByDurationRequest(datatype, timeRange,
                                duration, dor);
                    } else if (datatype.equalsIgnoreCase("weight")) {
                        request = WeightFunctions.prepareAggregateGroupByDurationRequest(timeRange, duration, dor);
                    } else if (datatype.equalsIgnoreCase("height")) {
                        request = HeightFunctions.prepareAggregateGroupByDurationRequest(timeRange, duration, dor);
                    } else if (datatype.equalsIgnoreCase("activity")) {
                        Set<AggregateMetric<Duration>> metrics = new HashSet<>();
                        metrics.add(ExerciseSessionRecord.EXERCISE_DURATION_TOTAL);
                        request = new AggregateGroupByDurationRequest(metrics, timeRange, duration, dor);
                    } else if (datatype.equalsIgnoreCase("calories")) {
                        Set<AggregateMetric<Energy>> metrics = new HashSet<>();
                        metrics.add(TotalCaloriesBurnedRecord.ENERGY_TOTAL);
                        request = new AggregateGroupByDurationRequest(metrics, timeRange, duration, dor);
                    } else if (datatype.equalsIgnoreCase("calories.active")) {
                        Set<AggregateMetric<Energy>> metrics = new HashSet<>();
                        metrics.add(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL);
                        request = new AggregateGroupByDurationRequest(metrics, timeRange, duration, dor);
                    } else if (datatype.equalsIgnoreCase("calories.basal")) {
                        Set<AggregateMetric<Energy>> metrics = new HashSet<>();
                        metrics.add(BasalMetabolicRateRecord.BASAL_CALORIES_TOTAL);
                        request = new AggregateGroupByDurationRequest(metrics, timeRange, duration, dor);
                    } else if (datatype.equalsIgnoreCase("distance")) {
                        Set<AggregateMetric<Length>> metrics = new HashSet<>();
                        metrics.add(DistanceRecord.DISTANCE_TOTAL);
                        request = new AggregateGroupByDurationRequest(metrics, timeRange, duration, dor);
                    } else if (datatype.equalsIgnoreCase("sleep")) {
                        request = SleepFunctions.prepareAggregateGroupByDurationRequest(timeRange, duration, dor);
                    } else if (datatype.equalsIgnoreCase("heart_rate")) {
                        request = HeartRateFunctions.prepareAggregateGroupByDurationRequest(timeRange, duration, dor);
                    } else if (datatype.equalsIgnoreCase("heart_rate.resting")) {
                        request = HeartRateFunctions.prepareRestingAggregateGroupByDurationRequest(timeRange, duration,
                                dor);
                    } else if (datatype.equalsIgnoreCase("blood_pressure")) {
                        request = BloodPressureFunctions.prepareAggregateGroupByDurationRequest(timeRange, duration,
                                dor);
                    } else {
                        callbackContext.error("Datatype not recognized " + datatype);
                        return;
                    }

                    List<AggregationResultGroupedByDuration> response = BuildersKt.runBlocking(
                            EmptyCoroutineContext.INSTANCE,
                            (s, c) -> healthConnectClient.aggregateGroupByDuration(request, c));

                    Log.d(TAG, "Got data from query aggregated");
                    JSONArray retBucketsArr = new JSONArray();

                    for (AggregationResultGroupedByDuration bucket : response) {
                        JSONObject retObject = new JSONObject();
                        long stbkt = bucket.getStartTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                        long etbkt = bucket.getEndTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                        retObject.put("startDate", stbkt);
                        retObject.put("endDate", etbkt);
                        setAggregatedVal(datatype, retObject, bucket.getResult());

                        retBucketsArr.put(retObject);
                    }

                    callbackContext.success(retBucketsArr);
                }
            } else {
                TimeRangeFilter timeRange = TimeRangeFilter.between(Instant.ofEpochMilli(st), Instant.ofEpochMilli(et));

                AggregateRequest request;
                // DATA_TYPE add here support for new data types
                if (datatype.equalsIgnoreCase("steps")) {
                    request = StepsFunctions.prepareAggregateRequest(timeRange, dor);
                } else if (datatype.equalsIgnoreCase("stairs")) {
                    request = StairsFunctions.prepareAggregateRequest(timeRange, dor);
                } else if (datatype.equalsIgnoreCase("nutrition")) {
                    request = NutritionFunctions.prepareAggregateRequest(timeRange, dor);
                } else if (datatype.equalsIgnoreCase("nutrition.water")) {
                    request = HydrationFunctions.prepareAggregateRequest(timeRange, dor);
                } else if (datatype.toLowerCase().startsWith("nutrition.")) {
                    request = NutritionXFunctions.prepareAggregateRequest(datatype, timeRange, dor);
                } else if (datatype.equalsIgnoreCase("weight")) {
                    request = WeightFunctions.prepareAggregateRequest(timeRange, dor);
                } else if (datatype.equalsIgnoreCase("height")) {
                    request = HeightFunctions.prepareAggregateRequest(timeRange, dor);
                } else if (datatype.equalsIgnoreCase("activity")) {
                    Set<AggregateMetric<Duration>> metrics = new HashSet<>();
                    metrics.add(ExerciseSessionRecord.EXERCISE_DURATION_TOTAL);
                    request = new AggregateRequest(metrics, timeRange, dor);
                } else if (datatype.equalsIgnoreCase("calories")) {
                    Set<AggregateMetric<Energy>> metrics = new HashSet<>();
                    metrics.add(TotalCaloriesBurnedRecord.ENERGY_TOTAL);
                    request = new AggregateRequest(metrics, timeRange, dor);
                } else if (datatype.equalsIgnoreCase("calories.active")) {
                    Set<AggregateMetric<Energy>> metrics = new HashSet<>();
                    metrics.add(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL);
                    request = new AggregateRequest(metrics, timeRange, dor);
                } else if (datatype.equalsIgnoreCase("calories.basal")) {
                    Set<AggregateMetric<Energy>> metrics = new HashSet<>();
                    metrics.add(BasalMetabolicRateRecord.BASAL_CALORIES_TOTAL);
                    request = new AggregateRequest(metrics, timeRange, dor);
                } else if (datatype.equalsIgnoreCase("distance")) {
                    Set<AggregateMetric<Length>> metrics = new HashSet<>();
                    metrics.add(DistanceRecord.DISTANCE_TOTAL);
                    request = new AggregateRequest(metrics, timeRange, dor);
                } else if (datatype.equalsIgnoreCase("sleep")) {
                    request = SleepFunctions.prepareAggregateRequest(timeRange, dor);
                } else if (datatype.equalsIgnoreCase("heart_rate")) {
                    request = HeartRateFunctions.prepareAggregateRequest(timeRange, dor);
                } else if (datatype.equalsIgnoreCase("heart_rate.resting")) {
                    request = HeartRateFunctions.prepareRestingAggregateRequest(timeRange, dor);
                } else if (datatype.equalsIgnoreCase("blood_pressure")) {
                    request = BloodPressureFunctions.prepareAggregateRequest(timeRange, dor);
                } else {
                    callbackContext.error("Datatype not recognized " + datatype);
                    return;
                }

                AggregationResult response = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (s, c) -> healthConnectClient.aggregate(request, c));
                Log.d(TAG, "Got data from query aggregated");

                JSONObject retObject = new JSONObject();
                retObject.put("startDate", st);
                retObject.put("endDate", et);
                setAggregatedVal(datatype, retObject, response);

                callbackContext.success(retObject);
            }
        } catch (JSONException ex) {
            Log.e(TAG, "Could not parse query object or write response object", ex);
            callbackContext.error("Could not parse query object or write response object");
        } catch (InterruptedException ex2) {
            Log.e(TAG, "Thread interrupted", ex2);
            callbackContext.error("Thread interrupted" + ex2.getMessage());
        }
    }

    private void setAggregatedVal(String datatype, JSONObject retObj, AggregationResult response) throws JSONException {
        // DATA_TYPE add here new data types when extending
        if (datatype.equalsIgnoreCase("steps")) {
            StepsFunctions.populateFromAggregatedQuery(response, retObj);
        } else if (datatype.equalsIgnoreCase("stairs")) {
            StairsFunctions.populateFromAggregatedQuery(response, retObj);
        } else if (datatype.equalsIgnoreCase("nutrition")) {
            NutritionFunctions.populateFromAggregatedQuery(response, retObj);
        } else if (datatype.equalsIgnoreCase("nutrition.water")) {
            HydrationFunctions.populateFromAggregatedQuery(response, retObj);
        } else if (datatype.toLowerCase().startsWith("nutrition.")) {
            NutritionXFunctions.populateFromAggregatedQuery(response, retObj);
        } else if (datatype.equalsIgnoreCase("weight")) {
            WeightFunctions.populateFromAggregatedQuery(response, retObj);
        } else if (datatype.equalsIgnoreCase("height")) {
            HeightFunctions.populateFromAggregatedQuery(response, retObj);
        } else if (datatype.equalsIgnoreCase("activity")) {
            Duration val = response.get(ExerciseSessionRecord.EXERCISE_DURATION_TOTAL);
            if (val != null) {
                long millis = val.getSeconds() * 1000;
                retObj.put("value", millis);
                retObj.put("unit", "ms");
            } else {
                retObj.put("value", 0);
                retObj.put("unit", "ms");
            }
        } else if (datatype.equalsIgnoreCase("calories")) {
            if (response.get(TotalCaloriesBurnedRecord.ENERGY_TOTAL) != null) {
                double kcals = response.get(TotalCaloriesBurnedRecord.ENERGY_TOTAL).getKilocalories();
                retObj.put("value", kcals);
                retObj.put("unit", "kcal");
            } else {
                retObj.put("value", 0);
                retObj.put("unit", "kcal");
            }
        } else if (datatype.equalsIgnoreCase("calories.active")) {
            if (response.get(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL) != null) {
                double kcals = response.get(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL).getKilocalories();
                retObj.put("value", kcals);
                retObj.put("unit", "kcal");
            } else {
                retObj.put("value", 0);
                retObj.put("unit", "kcal");
            }
        } else if (datatype.equalsIgnoreCase("calories.basal")) {
            if (response.get(BasalMetabolicRateRecord.BASAL_CALORIES_TOTAL) != null) {
                double kcals = response.get(BasalMetabolicRateRecord.BASAL_CALORIES_TOTAL).getKilocalories();
                retObj.put("value", kcals);
                retObj.put("unit", "kcal");
            } else {
                retObj.put("value", 0);
                retObj.put("unit", "kcal");
            }
        } else if (datatype.equalsIgnoreCase("distance")) {
            if (response.get(DistanceRecord.DISTANCE_TOTAL) != null) {
                double meters = response.get(DistanceRecord.DISTANCE_TOTAL).getMeters();
                retObj.put("value", meters);
                retObj.put("unit", "m");
            } else {
                retObj.put("value", 0);
                retObj.put("unit", "kcal");
            }
        } else if (datatype.equalsIgnoreCase("sleep")) {
            SleepFunctions.populateFromAggregatedQuery(response, retObj);
        } else if (datatype.equalsIgnoreCase("heart_rate")) {
            HeartRateFunctions.populateFromAggregatedQuery(response, retObj);
        } else if (datatype.equalsIgnoreCase("heart_rate.resting")) {
            HeartRateFunctions.populateRestingFromAggregatedQuery(response, retObj);
        } else if (datatype.equalsIgnoreCase("blood_pressure")) {
            BloodPressureFunctions.populateFromAggregatedQuery(response, retObj);
        } else {
            LOG.e(TAG, "Data type not recognized " + datatype);
        }
    }

    /**
     * Stores a datapoint
     *
     * @param args
     */
    private void store(final JSONArray args) {
        try {
            if (!args.getJSONObject(0).has("startDate")) {
                callbackContext.error("Missing argument startDate");
                return;
            }
            long st = args.getJSONObject(0).getLong("startDate");

            if (!args.getJSONObject(0).has("endDate")) {
                callbackContext.error("Missing argument endDate");
                return;
            }
            long et = args.getJSONObject(0).getLong("endDate");

            if (!args.getJSONObject(0).has("dataType")) {
                callbackContext.error("Missing argument dataType");
                return;
            }
            String datatype = args.getJSONObject(0).getString("dataType");
            KClass<? extends Record> dt = dataTypeNameToClass(datatype);
            if (dt == null) {
                callbackContext.error("Datatype " + datatype + " not supported");
                return;
            }

            if (!args.getJSONObject(0).has("value")) {
                callbackContext.error("Missing argument value");
                return;
            }

            InsertRecordsResponse response;
            List<Record> data = new LinkedList<>();

            // DATA_TYPE here we need to add support for each different data type
            // TODO: we could add meta data when storing, including entry method, client ID
            // and device

            if (datatype.equalsIgnoreCase("steps")) {
                StepsFunctions.prepareStoreRecords(args.getJSONObject(0), st, et, data);
            } else if (datatype.equalsIgnoreCase("stairs")) {
                StairsFunctions.prepareStoreRecords(args.getJSONObject(0), st, et, data);
            } else if (datatype.equalsIgnoreCase("weight")) {
                WeightFunctions.prepareStoreRecords(args.getJSONObject(0), st, data);
            } else if (datatype.equalsIgnoreCase("height")) {
                HeightFunctions.prepareStoreRecords(args.getJSONObject(0), st, data);
            } else if (datatype.equalsIgnoreCase("fat_percentage")) {
                double perc = args.getJSONObject(0).getDouble("value");

                BodyFatRecord record = new BodyFatRecord(
                        Instant.ofEpochMilli(st), null,
                        new Percentage(perc),
                        Metadata.unknownRecordingMethod());
                data.add(record);
            } else if (datatype.equalsIgnoreCase("activity")) {
                String activityStr = args.getJSONObject(0).getString("value");
                int exerciseType = ActivityMapper.exerciseTypeFromActivity(activityStr);
                String title = null;
                String notes = null;
                List<ExerciseSegment> segments = new LinkedList<>();
                List<ExerciseLap> laps = new LinkedList<>();

                ExerciseSessionRecord record = new ExerciseSessionRecord(
                        Instant.ofEpochMilli(st), null,
                        Instant.ofEpochMilli(et), null,
                        Metadata.unknownRecordingMethod(),
						exerciseType,
                        title, notes,                        
                        segments, laps);
                data.add(record);
            } else if (datatype.equalsIgnoreCase("calories")) {
                double kcals = args.getJSONObject(0).getDouble("value");

                TotalCaloriesBurnedRecord record = new TotalCaloriesBurnedRecord(
                        Instant.ofEpochMilli(st), null,
                        Instant.ofEpochMilli(et), null,
                        Energy.kilocalories(kcals),
                        Metadata.unknownRecordingMethod());
                data.add(record);
            } else if (datatype.equalsIgnoreCase("calories.active")) {
                double kcals = args.getJSONObject(0).getDouble("value");

                ActiveCaloriesBurnedRecord record = new ActiveCaloriesBurnedRecord(
                        Instant.ofEpochMilli(st), null,
                        Instant.ofEpochMilli(et), null,
                        Energy.kilocalories(kcals),
                        Metadata.unknownRecordingMethod());
                data.add(record);
            } else if (datatype.equalsIgnoreCase("calories.basal")) {
                double kcals = args.getJSONObject(0).getDouble("value");
                // convert kcals to power
                double ms = (et - st);
                double kcalsDay = kcals / (ms / (double) (86400000));
                Power pow = Power.kilocaloriesPerDay(kcalsDay);

                BasalMetabolicRateRecord record = new BasalMetabolicRateRecord(
                        Instant.ofEpochMilli(st), null,
                        pow,
                        Metadata.unknownRecordingMethod());
                data.add(record);
            } else if (datatype.equalsIgnoreCase("blood_glucose")) {
                JSONObject glucoseobj = args.getJSONObject(0).getJSONObject("value");
                BloodGlucoseFunctions.prepareStoreRecords(glucoseobj, st, data);
            } else if (datatype.equalsIgnoreCase("blood_pressure")) {
                JSONObject pressureobj = args.getJSONObject(0).getJSONObject("value");
                BloodPressureFunctions.prepareStoreRecords(pressureobj, st, data);
            } else if (datatype.equalsIgnoreCase("distance")) {
                double meters = args.getJSONObject(0).getDouble("value");
                Length len = Length.meters(meters);

                DistanceRecord record = new DistanceRecord(
                        Instant.ofEpochMilli(st), null,
                        Instant.ofEpochMilli(et), null,
                        len,
                        Metadata.unknownRecordingMethod());

                data.add(record);
            } else if (datatype.equalsIgnoreCase("sleep")) {
                SleepFunctions.prepareStoreRecords(args.getJSONObject(0), data);
            } else if (datatype.equalsIgnoreCase("heart_rate")) {
                HeartRateFunctions.prepareStoreRecords(args.getJSONObject(0), st, et, data);
            } else if (datatype.equalsIgnoreCase("heart_rate.resting")) {
                HeartRateFunctions.prepareRestingStoreRecords(args.getJSONObject(0), st, data);
            } else if (datatype.equalsIgnoreCase("heart_rate.variability")) {
                HeartRateFunctions.prepareVariabilityStoreRecords(args.getJSONObject(0), st, data);
            } else if (datatype.equalsIgnoreCase("nutrition")) {
                NutritionFunctions.prepareStoreRecords(args.getJSONObject(0), st, et, data);
            } else if (datatype.equalsIgnoreCase("nutrition.water")) {
                HydrationFunctions.prepareStoreRecords(args.getJSONObject(0), data);
            } else if (datatype.toLowerCase().startsWith("nutrition.")) {
                NutritionXFunctions.prepareStoreRecords(datatype, args.getJSONObject(0), st, et, data);
            } else {
                callbackContext.error("Datatype not supported " + datatype);
                return;
            }

            response = BuildersKt.runBlocking(
                    EmptyCoroutineContext.INSTANCE,
                    (s, c) -> healthConnectClient.insertRecords(data, c));

            Log.d(TAG, "Data written of type " + datatype);

            String id = response.getRecordIdsList().get(0);

            callbackContext.success(id);

        } catch (JSONException ex) {
            callbackContext.error("Cannot parse request object " + ex.getMessage());
        } catch (InterruptedException ex2) {
            callbackContext.error("Thread interrupted" + ex2.getMessage());
        }
    }

    /**
     * Deletes datapoints
     *
     * @param args
     */
    private void delete(final JSONArray args) {
        try {
            String datatype = args.getJSONObject(0).getString("dataType");
            KClass<? extends Record> dt = dataTypeNameToClass(datatype);
            if (dt == null) {
                callbackContext.error("Datatype " + datatype + " not supported");
                return;
            }

            if (args.getJSONObject(0).has("id")) {
                String id = args.getJSONObject(0).getString("id");

                List<String> recordids = new LinkedList<>();
                recordids.add(id);
                BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (s, c) -> healthConnectClient.deleteRecords(dt, recordids, new LinkedList<>(), c));
                Log.d(TAG, "Data deleted by ID of type " + datatype);

                callbackContext.success();
            } else {
                if (!args.getJSONObject(0).has("startDate")) {
                    callbackContext.error("Missing argument startDate");
                    return;
                }
                final long st = args.getJSONObject(0).getLong("startDate");

                if (!args.getJSONObject(0).has("endDate")) {
                    callbackContext.error("Missing argument endDate");
                    return;
                }
                final long et = args.getJSONObject(0).getLong("endDate");

                TimeRangeFilter timeRange = TimeRangeFilter.between(Instant.ofEpochMilli(st), Instant.ofEpochMilli(et));
                BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (s, c) -> healthConnectClient.deleteRecords(dt, timeRange, c));
                Log.d(TAG, "Data deleted by time range of type " + datatype);

                callbackContext.success();
            }
        } catch (JSONException ex) {
            callbackContext.error("Cannot parse request object " + ex.getMessage());
        } catch (InterruptedException ex2) {
            callbackContext.error("Thread interrupted" + ex2.getMessage());
        }
    }
}

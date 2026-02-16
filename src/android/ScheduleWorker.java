package org.apache.cordova.health;


import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import androidx.work.Data;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import androidx.health.connect.client.HealthConnectClient;
import androidx.health.connect.client.records.BloodGlucoseRecord;
import androidx.health.connect.client.records.HeartRateRecord;
import androidx.health.connect.client.records.Record;
import androidx.health.connect.client.request.ReadRecordsRequest;
import androidx.health.connect.client.response.ReadRecordsResponse;
import androidx.health.connect.client.time.TimeRangeFilter;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import kotlin.reflect.KClass;
import kotlin.jvm.JvmClassMappingKt;
import androidx.concurrent.futures.ResolvableFuture;

import androidx.health.connect.client.records.metadata.DataOrigin;
import androidx.health.connect.client.records.metadata.Device;
import androidx.health.connect.client.records.metadata.Metadata;

import java.util.Collections;
import java.util.Set;
import android.util.Log;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlin.jvm.functions.Function1;
import kotlin.Unit;

import org.jetbrains.annotations.NotNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.content.Intent;
import android.content.IntentFilter;


public class ScheduleWorker extends ListenableWorker {

	private static final String TAG = "cordova-plugin-health"; //"ScheduleWorker";

    public ScheduleWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        ResolvableFuture<Result> future = ResolvableFuture.create();

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                
				// Récupérer les données d'entrée
                String dataType = getInputData().getString("DATA_TYPE");
                long timeStart = getInputData().getLong("TIME_START", 0L);
                long timeEnd = getInputData().getLong("TIME_END", 0L);
				int limit = getInputData().getInt("DATA_LIMIT", 1000);
				boolean ascending = getInputData().getBoolean("DATA_ASCENDING", false);
				
				// Créer le client HealthConnect
                HealthConnectClient healthConnectClient = HealthConnectClient.getOrCreate(getApplicationContext());

                // Définir la plage de temps
                Instant startTime = Instant.ofEpochMilli(timeStart);
                Instant endTime = Instant.ofEpochMilli(timeEnd);
				
				//Log.d(TAG, "SW: startTime: " + startTime.toString() + " endTime: " + endTime.toString());
				
                // Vérifier que dataType est valide
                KClass<? extends Record> type;
				
                if (dataType.contains("BloodGlucoseRecord")) {
                    type = JvmClassMappingKt.getKotlinClass(BloodGlucoseRecord.class);
				} else if (dataType.contains("HeartRateRecord")) {
                    type = JvmClassMappingKt.getKotlinClass(HeartRateRecord.class);
				} else {
					Log.e(TAG, "SW : dataType not supported: " + dataType);
                    future.set(Result.failure());
                    return;
                }

                // Initialiser le pageToken
                String pageToken = null;

				// Fonction récursive pour paginer les résultats
                fetchRecordsPage(healthConnectClient, type, startTime, endTime, ascending, limit, pageToken, future);
				
				
            } catch (Exception e) {
				Log.e(TAG, "ERROR SW: ", e);
                future.set(Result.failure());
            }
        });

        return future;
    }

    private <T extends Record> void fetchRecordsPage(
		HealthConnectClient healthConnectClient,
		KClass<T> type,
		Instant startTime,
		Instant endTime,
		boolean ascending,
		int limit,
		String pageToken,
		ResolvableFuture<Result> future) {

		try {
			// Créer un ensemble vide de DataOrigin
			Set<DataOrigin> dataOrigins = Collections.emptySet();
			
			// Construire la requête en utilisant le type explicitement
			ReadRecordsRequest<T> request = new ReadRecordsRequest<>(
				type,
				TimeRangeFilter.between(startTime, endTime),
				dataOrigins,
				ascending,
				limit,  // pageSize
				pageToken
			);
			
			// Créer une Continuation pour gérer le résultat
			healthConnectClient.readRecords(request, new Continuation<ReadRecordsResponse<T>>() {
				@NotNull
				@Override
				public CoroutineContext getContext() {
					return EmptyCoroutineContext.INSTANCE;
				}

				@Override
				public void resumeWith(@NotNull Object result) {
					try {
						if (result instanceof ReadRecordsResponse) {
							
							ReadRecordsResponse<T> response = (ReadRecordsResponse<T>) result;
							List<T> records = response.getRecords();
	
							//Log.d(TAG, "SW: records size " + records.size());
							
							JSONArray resultset = new JSONArray();
							// default behaviour is that each record corresponds to one element in the
							// array, but there can be exceptions
							boolean oneElementPerRecord = true;
							
							for (T record : records) {
								//Log.d(TAG, "Record: " + record.toString());
								
								JSONObject obj = new JSONObject();
								
								populateFromMeta(obj, record.getMetadata());
								
								if (record instanceof BloodGlucoseRecord) {
									
									BloodGlucoseFunctions.populateFromQuery(record, obj);
									//Log.d(TAG, "SW: obj size " + obj.length());
																		
								}								
								else if (record instanceof HeartRateRecord) {
									
									oneElementPerRecord = false;
									HeartRateFunctions.populateFromQuery(record, resultset);
									//Log.d(TAG, "SW: obj size " + obj.length());
																		
								}								
								else {
									
									Log.e(TAG, "SW: Record not supported : " + record.getClass().getName());
									future.set(Result.failure());
									
								}
								
								// add to result array
								if (oneElementPerRecord) {
									resultset.put(obj);
								}
							
							}
							
							String nextPageToken = response.getPageToken();
														
							if (nextPageToken == null) {
								
								String jsonString = resultset.toString();
								
								Context context = getApplicationContext();
								Intent intent = new Intent("cordova-plugin-health.SW_WORK_COMPLETE");
								intent.putExtra("json_result", jsonString);
								LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
								
								future.set(Result.success());
								//Log.d(TAG, "SW: After future.set(Result.success), records size: " + records.size());
								
							} else {
								
								fetchRecordsPage(
									healthConnectClient,
									type,
									startTime,
									endTime,
									ascending,
									limit,
									nextPageToken,
									future
								);
								
							}
							
						} else {
							Log.e(TAG, "SW: Unexpected result: " + result.getClass().getName());
							future.set(Result.failure());
						}
					} catch (Exception e) {
						Log.e(TAG, "ERROR SW: ", e);
						future.set(Result.failure());
					}
				}
			});

		} catch (Exception e) {
			future.set(Result.failure());
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
}

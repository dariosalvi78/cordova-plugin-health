package org.apache.cordova.health;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.HealthDataTypes;
import com.google.android.gms.fitness.data.HealthFields;
import com.google.android.gms.fitness.data.SleepStages;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataDeleteRequest;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Health plugin Android code. MIT licensed.
 */
public class HealthPlugin extends CordovaPlugin {
  // logger tag
  private static final String TAG = "cordova-plugin-health";

  // calling activity
  private CordovaInterface cordova;

  // Google account to access the API
  GoogleSignInAccount account;

  // instance of the call back when requesting or checking authorisation
  private CallbackContext authReqCallbackCtx;

  private static final int REQUEST_OAUTH = 1;
  private static final int REQUEST_DYN_PERMS = 2;

  private final HashSet<String> authReadTypes = new HashSet<>();
  private final HashSet<String> authReadWriteTypes = new HashSet<>();
  private boolean authAutoresolve;

  public static Map<String, DataType> datatypes = new HashMap<String, DataType>();

  static {
    datatypes.put("steps", DataType.TYPE_STEP_COUNT_DELTA);
    datatypes.put("calories", DataType.TYPE_CALORIES_EXPENDED);
    datatypes.put("calories.basal", DataType.TYPE_BASAL_METABOLIC_RATE);
    datatypes.put("activity", DataType.TYPE_ACTIVITY_SEGMENT);
    datatypes.put("height", DataType.TYPE_HEIGHT);
    datatypes.put("weight", DataType.TYPE_WEIGHT);
    datatypes.put("heart_rate", DataType.TYPE_HEART_RATE_BPM);
    datatypes.put("fat_percentage", DataType.TYPE_BODY_FAT_PERCENTAGE);
    datatypes.put("distance", DataType.TYPE_DISTANCE_DELTA);
    datatypes.put("blood_glucose", HealthDataTypes.TYPE_BLOOD_GLUCOSE);
    datatypes.put("blood_pressure", HealthDataTypes.TYPE_BLOOD_PRESSURE);
    datatypes.put("body_temperature", HealthDataTypes.TYPE_BODY_TEMPERATURE);
    datatypes.put("sleep", DataType.TYPE_SLEEP_SEGMENT);
  }

  // Helper class used for storing nutrients information (name and unit of
  // measurement)
  private static class NutrientFieldInfo {
    public String field;
    public String unit;

    public NutrientFieldInfo(String field, String unit) {
      this.field = field;
      this.unit = unit;
    }
  }

  // Lookup for nutrition fields and units
  public static Map<String, NutrientFieldInfo> nutrientFields = new HashMap<String, NutrientFieldInfo>();

  static {
    nutrientFields.put("nutrition.calories", new NutrientFieldInfo(Field.NUTRIENT_CALORIES, "kcal"));
    nutrientFields.put("nutrition.fat.total", new NutrientFieldInfo(Field.NUTRIENT_TOTAL_FAT, "g"));
    nutrientFields.put("nutrition.fat.saturated", new NutrientFieldInfo(Field.NUTRIENT_SATURATED_FAT, "g"));
    nutrientFields.put("nutrition.fat.unsaturated", new NutrientFieldInfo(Field.NUTRIENT_UNSATURATED_FAT, "g"));
    nutrientFields.put("nutrition.fat.polyunsaturated", new NutrientFieldInfo(Field.NUTRIENT_POLYUNSATURATED_FAT, "g"));
    nutrientFields.put("nutrition.fat.monounsaturated", new NutrientFieldInfo(Field.NUTRIENT_MONOUNSATURATED_FAT, "g"));
    nutrientFields.put("nutrition.fat.trans", new NutrientFieldInfo(Field.NUTRIENT_TRANS_FAT, "g"));
    nutrientFields.put("nutrition.cholesterol", new NutrientFieldInfo(Field.NUTRIENT_CHOLESTEROL, "mg"));
    nutrientFields.put("nutrition.sodium", new NutrientFieldInfo(Field.NUTRIENT_SODIUM, "mg"));
    nutrientFields.put("nutrition.potassium", new NutrientFieldInfo(Field.NUTRIENT_POTASSIUM, "mg"));
    nutrientFields.put("nutrition.carbs.total", new NutrientFieldInfo(Field.NUTRIENT_TOTAL_CARBS, "g"));
    nutrientFields.put("nutrition.dietary_fiber", new NutrientFieldInfo(Field.NUTRIENT_DIETARY_FIBER, "g"));
    nutrientFields.put("nutrition.sugar", new NutrientFieldInfo(Field.NUTRIENT_SUGAR, "g"));
    nutrientFields.put("nutrition.protein", new NutrientFieldInfo(Field.NUTRIENT_PROTEIN, "g"));
    nutrientFields.put("nutrition.vitamin_a", new NutrientFieldInfo(Field.NUTRIENT_VITAMIN_A, "IU"));
    nutrientFields.put("nutrition.vitamin_c", new NutrientFieldInfo(Field.NUTRIENT_VITAMIN_C, "mg"));
    nutrientFields.put("nutrition.calcium", new NutrientFieldInfo(Field.NUTRIENT_CALCIUM, "mg"));
    nutrientFields.put("nutrition.iron", new NutrientFieldInfo(Field.NUTRIENT_IRON, "mg"));
  }

  static {
    datatypes.put("nutrition", DataType.TYPE_NUTRITION);
    datatypes.put("nutrition.water", DataType.TYPE_HYDRATION);
    for (String dataType : nutrientFields.keySet()) {
      datatypes.put(dataType, DataType.TYPE_NUTRITION);
    }
  }

  public HealthPlugin() {
  }

  // general initialization
  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    this.cordova = cordova;
  }

  // called once custom data types have been created
  // asks for dynamic permissions on Android 6 and more
  private void requestDynamicPermissions() {
    HashSet<String> dynPerms = new HashSet<>();
    // see
    // https://developers.google.com/fit/android/authorization#data_types_that_need_android_permissions

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      // new Android 10 permissions
      if (authReadTypes.contains("steps") || authReadTypes.contains("activity") || authReadWriteTypes.contains("steps")
          || authReadWriteTypes.contains("activity") || authReadWriteTypes.contains("calories")
          || authReadWriteTypes.contains("calories.active")) {
        if (!cordova.hasPermission(Manifest.permission.ACTIVITY_RECOGNITION))
          dynPerms.add(Manifest.permission.ACTIVITY_RECOGNITION);
      }
    }
    if (authReadTypes.contains("distance") || authReadWriteTypes.contains("distance")) {
      if (!cordova.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION))
        dynPerms.add(Manifest.permission.ACCESS_FINE_LOCATION);
    }
    if (authReadTypes.contains("heart_rate")) {
      if (!cordova.hasPermission(Manifest.permission.BODY_SENSORS))
        dynPerms.add(Manifest.permission.BODY_SENSORS);
    }
    if (dynPerms.isEmpty()) {
      // no dynamic permissions to ask
      accessGoogleFit();
    } else {
      if (authAutoresolve) {
        cordova.requestPermissions(this, REQUEST_DYN_PERMS, dynPerms.toArray(new String[dynPerms.size()]));
        // the request results will be taken care of by onRequestPermissionResult()
      } else {
        // if should not autoresolve, and there are dynamic permissions needed, send a
        // false
        authReqCallbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.OK, false));
      }
    }
  }

  // called when the dynamic permissions are asked
  @Override
  public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults)
      throws JSONException {
    if (requestCode == REQUEST_DYN_PERMS) {
      for (int i = 0; i < grantResults.length; i++) {
        if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
          String errmsg = "Permission denied ";
          for (String perm : permissions) {
            errmsg += " " + perm;
          }
          authReqCallbackCtx.error("Permission denied: " + permissions[i]);
          return;
        }
      }
      // all dynamic permissions accepted!
      Log.i(TAG, "All dynamic permissions accepted");
      accessGoogleFit();
    }
  }

  /**
   * The "execute" method that Cordova calls whenever the plugin is used from the
   * JavaScript
   *
   * @param action          The action to execute.
   * @param args            The exec() arguments.
   * @param callbackContext The callback context used when calling back into
   *                        JavaScript.
   * @return
   * @throws JSONException
   */
  @Override
  public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext)
      throws JSONException {

    if ("isAvailable".equals(action)) {
      isAvailable(callbackContext);
      return true;
    } else if ("promptInstallFit".equals(action)) {
      promptInstall(callbackContext);
      return true;
    } else if ("requestAuthorization".equals(action)) {
      cordova.getThreadPool().execute(new Runnable() {
        @Override
        public void run() {
          try {
            checkAuthorization(args, callbackContext, true); // with autoresolve
          } catch (Exception ex) {
            callbackContext.error(ex.getMessage());
          }
        }
      });
      return true;
    } else if ("checkAuthorization".equals(action)) {
      cordova.getThreadPool().execute(new Runnable() {
        @Override
        public void run() {
          try {
            checkAuthorization(args, callbackContext, false); // without autoresolve
          } catch (Exception ex) {
            callbackContext.error(ex.getMessage());
          }
        }
      });
      return true;
    } else if ("isAuthorized".equals(action)) {
      cordova.getThreadPool().execute(new Runnable() {
        @Override
        public void run() {
          try {
            checkAuthorization(args, callbackContext, false); // without autoresolve
          } catch (Exception ex) {
            callbackContext.error(ex.getMessage());
          }
        }
      });
      return true;
    } else if ("disconnect".equals(action)) {
      disconnect(callbackContext);
      return true;
    } else if ("query".equals(action)) {
      cordova.getThreadPool().execute(new Runnable() {
        @Override
        public void run() {
          try {
            query(args, callbackContext);
          } catch (Exception ex) {
            callbackContext.error(ex.getMessage());
          }
        }
      });
      return true;
    } else if ("queryAggregated".equals(action)) {
      cordova.getThreadPool().execute(new Runnable() {
        @Override
        public void run() {
          try {
            queryAggregated(args, callbackContext);
          } catch (Exception ex) {
            callbackContext.error(ex.getMessage());
          }
        }
      });
      return true;
    } else if ("store".equals(action)) {
      cordova.getThreadPool().execute(new Runnable() {
        @Override
        public void run() {
          try {
            store(args, callbackContext);
          } catch (Exception ex) {
            callbackContext.error(ex.getMessage());
          }
        }
      });
      return true;
    } else if ("delete".equals(action)) {
      cordova.getThreadPool().execute(new Runnable() {
        @Override
        public void run() {
          try {
            delete(args, callbackContext);
          } catch (Exception ex) {
            callbackContext.error(ex.getMessage());
          }
        }
      });
      return true;
    }

    return false;
  }

  // detects if a) Google APIs are available, b) Google Fit is actually installed
  private void isAvailable(final CallbackContext callbackContext) {
    // first check that the Google APIs are available
    GoogleApiAvailability gapi = GoogleApiAvailability.getInstance();
    int apiresult = gapi.isGooglePlayServicesAvailable(this.cordova.getActivity());
    if (apiresult == ConnectionResult.SUCCESS) {
      // then check that Google Fit is actually installed
      PackageManager pm = cordova.getActivity().getApplicationContext().getPackageManager();
      try {
        pm.getPackageInfo("com.google.android.apps.fitness", PackageManager.GET_ACTIVITIES);
        // Success return object
        PluginResult result;
        result = new PluginResult(PluginResult.Status.OK, true);
        callbackContext.sendPluginResult(result);
      } catch (PackageManager.NameNotFoundException e) {
        Log.d(TAG, "Google Fit not installed");
      }
    }
    PluginResult result;
    result = new PluginResult(PluginResult.Status.OK, false);
    callbackContext.sendPluginResult(result);
  }

  /**
   * Disconnects the client from the Google APIs
   *
   * @param callbackContext
   */
  private void disconnect(final CallbackContext callbackContext) {
    if (this.account != null) {
      Fitness.getConfigClient(this.cordova.getContext(), this.account).disableFit().addOnSuccessListener(r -> {
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, true));
      }).addOnFailureListener(err -> {
        err.getCause().printStackTrace();
        callbackContext.error("cannot disconnect," + err.getMessage());
      });
    }
  }

  // prompts to install GooglePlayServices if not available then Google Fit if not
  // available
  private void promptInstall(final CallbackContext callbackContext) {
    GoogleApiAvailability gapi = GoogleApiAvailability.getInstance();
    int apiresult = gapi.isGooglePlayServicesAvailable(this.cordova.getActivity());
    if (apiresult != ConnectionResult.SUCCESS) {
      if (gapi.isUserResolvableError(apiresult)) {
        // show the dialog, but no action is performed afterwards
        gapi.showErrorDialogFragment(this.cordova.getActivity(), apiresult, 1000);
      }
    } else {
      // check that Google Fit is actually installed
      PackageManager pm = cordova.getActivity().getApplicationContext().getPackageManager();
      try {
        pm.getPackageInfo("com.google.android.apps.fitness", PackageManager.GET_ACTIVITIES);
      } catch (PackageManager.NameNotFoundException e) {
        // show popup for downloading app
        // code from
        // http://stackoverflow.com/questions/11753000/how-to-open-the-google-play-store-directly-from-my-android-application
        try {
          cordova.getActivity().startActivity(
              new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.fitness")));
        } catch (android.content.ActivityNotFoundException anfe) {
          cordova.getActivity().startActivity(new Intent(Intent.ACTION_VIEW,
              Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.fitness")));
        }
      }
    }
    callbackContext.success();
  }

  private void accessGoogleFit() {
    Log.d(TAG, "Building read/write fitness options");
    FitnessOptions.Builder builder = FitnessOptions.builder();
    for (String readType : authReadTypes) {
      builder.addDataType(datatypes.get(readType), FitnessOptions.ACCESS_READ);
    }
    for (String readWriteType : authReadWriteTypes) {
      // read must be explicitly added if we want to read other apps data too
      // see: https://developers.google.com/fit/improvements#what_do_you_need_to_do
      builder.addDataType(datatypes.get(readWriteType), FitnessOptions.ACCESS_READ);
      builder.addDataType(datatypes.get(readWriteType), FitnessOptions.ACCESS_WRITE);
    }
    FitnessOptions options = builder.build();

    Log.d(TAG, "Accessing account");
    this.account = GoogleSignIn.getAccountForExtension(this.cordova.getContext(), options);

    if (!GoogleSignIn.hasPermissions(this.account, options)) {
      if (!authAutoresolve) {
        authReqCallbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.OK, false));
      } else {
        // launches activity for auth, resolved in onActivityResult
        GoogleSignIn.requestPermissions(this.cordova.getActivity(), // your activity
            REQUEST_OAUTH, this.account, options);
      }
    } else {
      // all done!
      authReqCallbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.OK, true));
    }
  }

  // called when access to Google API is answered
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    if (requestCode == REQUEST_OAUTH) {
      if (resultCode == Activity.RESULT_OK) {
        Log.i(TAG, "Got authorisation from Google Fit");
        authReqCallbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.OK, true));
      } else if (resultCode == Activity.RESULT_CANCELED) {
        // The user cancelled the login dialog before selecting any action.
        authReqCallbackCtx.error("User cancelled the dialog");
      } else
        authReqCallbackCtx.error("Authorisation failed, result code " + resultCode);
    }
  }

  // check if the app is authorised to use Google fitness APIs
  // if autoresolve is set, it will try to get authorisation from the user
  // also includes some OS dynamic permissions if needed (eg location)
  private void checkAuthorization(final JSONArray args, final CallbackContext callbackContext,
      final boolean autoresolve) throws JSONException {
    this.cordova.setActivityResultCallback(this);
    authReqCallbackCtx = callbackContext;
    authAutoresolve = autoresolve;

    // build the read and read-write sets
    authReadTypes.clear();
    authReadWriteTypes.clear();

    for (int i = 0; i < args.length(); i++) {
      Object object = args.get(i);
      if (object instanceof JSONObject) {
        JSONObject readWriteObj = (JSONObject) object;
        if (readWriteObj.has("read")) {
          JSONArray readArray = readWriteObj.getJSONArray("read");
          for (int j = 0; j < readArray.length(); j++) {
            authReadTypes.add(readArray.getString(j));
          }
        }
        if (readWriteObj.has("write")) {
          JSONArray writeArray = readWriteObj.getJSONArray("write");
          for (int j = 0; j < writeArray.length(); j++) {
            authReadWriteTypes.add(writeArray.getString(j));
          }
        }
      } else if (object instanceof String) {
        authReadWriteTypes.add(String.valueOf(object));
      }
    }
    authReadTypes.removeAll(authReadWriteTypes);

    // now ask for dynamic permissiions
    requestDynamicPermissions();
  }

  // queries for datapoints
  private void query(final JSONArray args, final CallbackContext callbackContext) throws Exception {
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
    final DataType dt = datatypes.get(datatype);

    if (dt == null) {
      callbackContext.error("Datatype " + datatype + " not supported");
      return;
    }

    if (this.account == null) {
      callbackContext.error("You must call requestAuthorization() before query()");
      return;
    }

    DataReadRequest.Builder readRequestBuilder = new DataReadRequest.Builder();
    readRequestBuilder.setTimeRange(st, et, TimeUnit.MILLISECONDS);

    if (dt.equals(DataType.TYPE_STEP_COUNT_DELTA) && args.getJSONObject(0).has("filtered")
        && args.getJSONObject(0).getBoolean("filtered")) {
      // exceptional case for filtered steps
      DataSource filteredStepsSource = new DataSource.Builder().setDataType(DataType.TYPE_STEP_COUNT_DELTA)
          .setType(DataSource.TYPE_DERIVED).setStreamName("estimated_steps").setAppPackageName("com.google.android.gms")
          .build();

      readRequestBuilder.read(filteredStepsSource);
    } else {
      readRequestBuilder.read(dt);
    }

    Integer limit = null;
    if (args.getJSONObject(0).has("limit")) {
      limit = args.getJSONObject(0).getInt("limit");
      readRequestBuilder.setLimit(limit);
    }

    Task<DataReadResponse> queryTask = Fitness.getHistoryClient(this.cordova.getContext(), this.account)
        .readData(readRequestBuilder.build());

    DataReadResponse response = Tasks.await(queryTask);
    if (!response.getStatus().isSuccess()) {
      // abort
      callbackContext.error(response.getStatus().getStatusMessage());
      return;
    }

    Log.d(TAG, "Data query successful");
    JSONArray resultset = new JSONArray();
    List<DataSet> datasets = response.getDataSets();
    for (DataSet dataset : datasets) {
      for (DataPoint datapoint : dataset.getDataPoints()) {
        JSONObject obj = new JSONObject();
        obj.put("startDate", datapoint.getStartTime(TimeUnit.MILLISECONDS));
        obj.put("endDate", datapoint.getEndTime(TimeUnit.MILLISECONDS));
        DataSource dataSource = datapoint.getOriginalDataSource();
        if (dataSource != null) {
          String sourceBundleId = dataSource.getAppPackageName();
          if (sourceBundleId != null)
            obj.put("sourceBundleId", sourceBundleId);
        }

        // reference for fields:
        // https://developers.google.com/android/reference/com/google/android/gms/fitness/data/Field.html
        if (dt.equals(DataType.TYPE_STEP_COUNT_DELTA)) {
          int steps = datapoint.getValue(Field.FIELD_STEPS).asInt();
          obj.put("value", steps);
          obj.put("unit", "count");
        } else if (dt.equals(DataType.TYPE_DISTANCE_DELTA)) {
          float distance = datapoint.getValue(Field.FIELD_DISTANCE).asFloat();
          obj.put("value", distance);
          obj.put("unit", "m");
        } else if (dt.equals(DataType.TYPE_HYDRATION)) {
          float distance = datapoint.getValue(Field.FIELD_VOLUME).asFloat();
          obj.put("value", distance);
          obj.put("unit", "ml");// documentation says it's litres, but from experiments I get ml
        } else if (dt.equals(DataType.TYPE_NUTRITION)) {
          if (datatype.equalsIgnoreCase("nutrition")) {
            JSONObject dob = new JSONObject();
            if (datapoint.getValue(Field.FIELD_FOOD_ITEM) != null) {
              dob.put("item", datapoint.getValue(Field.FIELD_FOOD_ITEM).asString());
            }
            if (datapoint.getValue(Field.FIELD_MEAL_TYPE) != null) {
              int mealt = datapoint.getValue(Field.FIELD_MEAL_TYPE).asInt();
              if (mealt == Field.MEAL_TYPE_BREAKFAST)
                dob.put("meal_type", "breakfast");
              else if (mealt == Field.MEAL_TYPE_DINNER)
                dob.put("meal_type", "dinner");
              else if (mealt == Field.MEAL_TYPE_LUNCH)
                dob.put("meal_type", "lunch");
              else if (mealt == Field.MEAL_TYPE_SNACK)
                dob.put("meal_type", "snack");
              else
                dob.put("meal_type", "unknown");
            }
            if (datapoint.getValue(Field.FIELD_NUTRIENTS) != null) {
              Value v = datapoint.getValue(Field.FIELD_NUTRIENTS);
              dob.put("nutrients", getNutrients(v, null));
            }
            obj.put("value", dob);
            obj.put("unit", "nutrition");
          } else {
            Value nutrients = datapoint.getValue(Field.FIELD_NUTRIENTS);
            NutrientFieldInfo fieldInfo = nutrientFields.get(datatype);
            if (fieldInfo != null) {
              if (nutrients.getKeyValue(fieldInfo.field) != null) {
                obj.put("value", (float) nutrients.getKeyValue(fieldInfo.field));
              } else {
                obj.put("value", 0f);
              }
              obj.put("unit", fieldInfo.unit);
            }
          }
        } else if (dt.equals(DataType.TYPE_CALORIES_EXPENDED)) {
          float calories = datapoint.getValue(Field.FIELD_CALORIES).asFloat();
          obj.put("value", calories);
          obj.put("unit", "kcal");
        } else if (dt.equals(DataType.TYPE_BASAL_METABOLIC_RATE)) {
          float calories = datapoint.getValue(Field.FIELD_CALORIES).asFloat();
          obj.put("value", calories);
          obj.put("unit", "kcal");
        } else if (dt.equals(DataType.TYPE_HEIGHT)) {
          float height = datapoint.getValue(Field.FIELD_HEIGHT).asFloat();
          obj.put("value", height);
          obj.put("unit", "m");
        } else if (dt.equals(DataType.TYPE_WEIGHT)) {
          float weight = datapoint.getValue(Field.FIELD_WEIGHT).asFloat();
          obj.put("value", weight);
          obj.put("unit", "kg");
        } else if (dt.equals(DataType.TYPE_HEART_RATE_BPM)) {
          float weight = datapoint.getValue(Field.FIELD_BPM).asFloat();
          obj.put("value", weight);
          obj.put("unit", "bpm");
        } else if (dt.equals(DataType.TYPE_BODY_FAT_PERCENTAGE)) {
          float weight = datapoint.getValue(Field.FIELD_PERCENTAGE).asFloat();
          obj.put("value", weight);
          obj.put("unit", "percent");
        } else if (dt.equals(DataType.TYPE_ACTIVITY_SEGMENT)) {
          String activity = datapoint.getValue(Field.FIELD_ACTIVITY).asActivity();
          obj.put("value", activity);
          obj.put("unit", "activityType");

          // extra queries to get calorie and distance records related to the activity
          // times
          DataReadRequest.Builder readActivityRequestBuilder = new DataReadRequest.Builder();
          readActivityRequestBuilder.setTimeRange(datapoint.getStartTime(TimeUnit.MILLISECONDS),
              datapoint.getEndTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS).read(DataType.TYPE_DISTANCE_DELTA)
              .read(DataType.TYPE_CALORIES_EXPENDED);

          Task<DataReadResponse> activityTask = Fitness.getHistoryClient(this.cordova.getContext(), this.account)
              .readData(readActivityRequestBuilder.build());
          // Active wait. This is not very efficient, but otherwise the code would become
          // hard to structure
          DataReadResponse dataReadActivityResult = Tasks.await(activityTask);

          if (!dataReadActivityResult.getStatus().isSuccess()) {
            // abort
            callbackContext.error(dataReadActivityResult.getStatus().getStatusMessage());
            return;
          }

          float totaldistance = 0;
          float totalcalories = 0;

          List<DataSet> dataActivitySets = dataReadActivityResult.getDataSets();
          for (DataSet dataActivitySet : dataActivitySets) {
            for (DataPoint dataActivityPoint : dataActivitySet.getDataPoints()) {
              if (dataActivitySet.getDataType().equals(DataType.TYPE_DISTANCE_DELTA)) {
                float distance = dataActivityPoint.getValue(Field.FIELD_DISTANCE).asFloat();
                totaldistance += distance;
              } else {
                float calories = dataActivityPoint.getValue(Field.FIELD_CALORIES).asFloat();
                totalcalories += calories;
              }
            }
          }
          obj.put("distance", totaldistance);
          obj.put("calories", totalcalories);
        } else if (dt.equals(HealthDataTypes.TYPE_BLOOD_GLUCOSE)) {
          JSONObject glucob = new JSONObject();
          float glucose = datapoint.getValue(HealthFields.FIELD_BLOOD_GLUCOSE_LEVEL).asFloat();
          glucob.put("glucose", glucose);
          if (datapoint.getValue(HealthFields.FIELD_TEMPORAL_RELATION_TO_MEAL).isSet()
              && datapoint.getValue(Field.FIELD_MEAL_TYPE).isSet()) {
            int temp_to_meal = datapoint.getValue(HealthFields.FIELD_TEMPORAL_RELATION_TO_MEAL).asInt();
            String meal = "";
            if (temp_to_meal == HealthFields.FIELD_TEMPORAL_RELATION_TO_MEAL_AFTER_MEAL) {
              meal = "after_";
            } else if (temp_to_meal == HealthFields.FIELD_TEMPORAL_RELATION_TO_MEAL_BEFORE_MEAL) {
              meal = "before_";
            } else if (temp_to_meal == HealthFields.FIELD_TEMPORAL_RELATION_TO_MEAL_FASTING) {
              meal = "fasting";
            } else if (temp_to_meal == HealthFields.FIELD_TEMPORAL_RELATION_TO_MEAL_GENERAL) {
              meal = "";
            }
            if (temp_to_meal != HealthFields.FIELD_TEMPORAL_RELATION_TO_MEAL_FASTING) {
              switch (datapoint.getValue(Field.FIELD_MEAL_TYPE).asInt()) {
                case Field.MEAL_TYPE_BREAKFAST:
                  meal += "breakfast";
                  break;
                case Field.MEAL_TYPE_DINNER:
                  meal += "dinner";
                  break;
                case Field.MEAL_TYPE_LUNCH:
                  meal += "lunch";
                  break;
                case Field.MEAL_TYPE_SNACK:
                  meal += "snack";
                  break;
                default:
                  meal = "unknown";
              }
            }
            glucob.put("meal", meal);
          }
          if (datapoint.getValue(HealthFields.FIELD_TEMPORAL_RELATION_TO_SLEEP).isSet()) {
            String sleep = "";
            switch (datapoint.getValue(HealthFields.FIELD_TEMPORAL_RELATION_TO_SLEEP).asInt()) {
              case HealthFields.TEMPORAL_RELATION_TO_SLEEP_BEFORE_SLEEP:
                sleep = "before_sleep";
                break;
              case HealthFields.TEMPORAL_RELATION_TO_SLEEP_DURING_SLEEP:
                sleep = "during_sleep";
                break;
              case HealthFields.TEMPORAL_RELATION_TO_SLEEP_FULLY_AWAKE:
                sleep = "fully_awake";
                break;
              case HealthFields.TEMPORAL_RELATION_TO_SLEEP_ON_WAKING:
                sleep = "on_waking";
                break;
            }
            glucob.put("sleep", sleep);
          }
          if (datapoint.getValue(HealthFields.FIELD_BLOOD_GLUCOSE_SPECIMEN_SOURCE).isSet()) {
            String source = "";
            switch (datapoint.getValue(HealthFields.FIELD_BLOOD_GLUCOSE_SPECIMEN_SOURCE).asInt()) {
              case HealthFields.BLOOD_GLUCOSE_SPECIMEN_SOURCE_CAPILLARY_BLOOD:
                source = "capillary_blood";
                break;
              case HealthFields.BLOOD_GLUCOSE_SPECIMEN_SOURCE_INTERSTITIAL_FLUID:
                source = "interstitial_fluid";
                break;
              case HealthFields.BLOOD_GLUCOSE_SPECIMEN_SOURCE_PLASMA:
                source = "plasma";
                break;
              case HealthFields.BLOOD_GLUCOSE_SPECIMEN_SOURCE_SERUM:
                source = "serum";
                break;
              case HealthFields.BLOOD_GLUCOSE_SPECIMEN_SOURCE_TEARS:
                source = "tears";
                break;
              case HealthFields.BLOOD_GLUCOSE_SPECIMEN_SOURCE_WHOLE_BLOOD:
                source = "whole_blood";
                break;
            }
            glucob.put("source", source);
          }
          obj.put("value", glucob);
          obj.put("unit", "mmol/L");
        } else if (dt.equals(HealthDataTypes.TYPE_BLOOD_PRESSURE)) {
          JSONObject bpobj = new JSONObject();
          if (datapoint.getValue(HealthFields.FIELD_BLOOD_PRESSURE_SYSTOLIC).isSet()) {
            float systolic = datapoint.getValue(HealthFields.FIELD_BLOOD_PRESSURE_SYSTOLIC).asFloat();
            bpobj.put("systolic", systolic);
          }
          if (datapoint.getValue(HealthFields.FIELD_BLOOD_PRESSURE_DIASTOLIC).isSet()) {
            float diastolic = datapoint.getValue(HealthFields.FIELD_BLOOD_PRESSURE_DIASTOLIC).asFloat();
            bpobj.put("diastolic", diastolic);
          }
          obj.put("value", bpobj);
          obj.put("unit", "mmHg");
        } else if (dt.equals(HealthDataTypes.TYPE_BODY_TEMPERATURE)) {
          float bodyTemp = datapoint.getValue(HealthFields.FIELD_BODY_TEMPERATURE).asFloat();
          obj.put("value", bodyTemp);
          obj.put("unit", "celsius");
        } else if (dt.equals(DataType.TYPE_SLEEP_SEGMENT)) {
          String sleepSegmentType = "";
          switch (datapoint.getValue(Field.FIELD_SLEEP_SEGMENT_TYPE).asInt()) {
            case SleepStages.AWAKE:
              sleepSegmentType = "sleep.awake";
              break;
            case SleepStages.SLEEP:
              sleepSegmentType = "sleep";
              break;
            case SleepStages.OUT_OF_BED:
              sleepSegmentType = "sleep.outOfBed";
              break;
            case SleepStages.SLEEP_LIGHT:
              sleepSegmentType = "sleep.light";
              break;
            case SleepStages.SLEEP_DEEP:
              sleepSegmentType = "sleep.deep";
              break;
            case SleepStages.SLEEP_REM:
              sleepSegmentType = "sleep.rem";
              break;
          }
          obj.put("value", sleepSegmentType);
          obj.put("unit", "sleepSegmentType");
        }
        resultset.put(obj);
      }
    }
    callbackContext.success(resultset);

  }

  // utility function, gets nutrients from a Value and merges the value inside a
  // json object
  private JSONObject getNutrients(Value nutrientsMap, JSONObject mergewith) throws JSONException {
    JSONObject nutrients;
    if (mergewith != null) {
      nutrients = mergewith;
    } else {
      nutrients = new JSONObject();
    }
    mergeNutrient(Field.NUTRIENT_CALORIES, nutrientsMap, nutrients);
    mergeNutrient(Field.NUTRIENT_TOTAL_FAT, nutrientsMap, nutrients);
    mergeNutrient(Field.NUTRIENT_SATURATED_FAT, nutrientsMap, nutrients);
    mergeNutrient(Field.NUTRIENT_UNSATURATED_FAT, nutrientsMap, nutrients);
    mergeNutrient(Field.NUTRIENT_POLYUNSATURATED_FAT, nutrientsMap, nutrients);
    mergeNutrient(Field.NUTRIENT_MONOUNSATURATED_FAT, nutrientsMap, nutrients);
    mergeNutrient(Field.NUTRIENT_TRANS_FAT, nutrientsMap, nutrients);
    mergeNutrient(Field.NUTRIENT_CHOLESTEROL, nutrientsMap, nutrients);
    mergeNutrient(Field.NUTRIENT_SODIUM, nutrientsMap, nutrients);
    mergeNutrient(Field.NUTRIENT_POTASSIUM, nutrientsMap, nutrients);
    mergeNutrient(Field.NUTRIENT_TOTAL_CARBS, nutrientsMap, nutrients);
    mergeNutrient(Field.NUTRIENT_DIETARY_FIBER, nutrientsMap, nutrients);
    mergeNutrient(Field.NUTRIENT_SUGAR, nutrientsMap, nutrients);
    mergeNutrient(Field.NUTRIENT_PROTEIN, nutrientsMap, nutrients);
    mergeNutrient(Field.NUTRIENT_VITAMIN_A, nutrientsMap, nutrients);
    mergeNutrient(Field.NUTRIENT_VITAMIN_C, nutrientsMap, nutrients);
    mergeNutrient(Field.NUTRIENT_CALCIUM, nutrientsMap, nutrients);
    mergeNutrient(Field.NUTRIENT_IRON, nutrientsMap, nutrients);

    return nutrients;
  }

  // utility function, merges a nutrient in an json object
  private void mergeNutrient(String f, Value nutrientsMap, JSONObject nutrients) throws JSONException {
    if (nutrientsMap.getKeyValue(f) != null) {
      String n = null;
      for (String name : nutrientFields.keySet()) {
        if (nutrientFields.get(name).field.equalsIgnoreCase(f)) {
          n = name;
          break;
        }
      }
      if (n != null) {
        float val = nutrientsMap.getKeyValue(f);
        if (nutrients.has(n)) {
          val += nutrients.getDouble(n);
        }
        nutrients.put(n, val);
      }
    }
  }

  // queries and aggregates data
  private void queryAggregated(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
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
    long _et = et; // keep track of the original end time, needed for basal calories
    if (!args.getJSONObject(0).has("dataType")) {
      callbackContext.error("Missing argument dataType");
      return;
    }
    String datatype = args.getJSONObject(0).getString("dataType");

    boolean hasbucket = args.getJSONObject(0).has("bucket");
    boolean customBucket = false;
    String bucketType = "";
    if (hasbucket) {
      bucketType = args.getJSONObject(0).getString("bucket");
      if (!bucketType.equalsIgnoreCase("hour") && !bucketType.equalsIgnoreCase("day")) {
        customBucket = true;
        if (!bucketType.equalsIgnoreCase("week") && !bucketType.equalsIgnoreCase("month")
            && !bucketType.equalsIgnoreCase("year")) {
          // error
          callbackContext.error("Bucket type " + bucketType + " not recognised");
          return;
        }
      }
      // Google fit bucketing is different and start and end must be quantised
      Calendar c = Calendar.getInstance();

      c.setTimeInMillis(st);
      c.clear(Calendar.MINUTE);
      c.clear(Calendar.SECOND);
      c.clear(Calendar.MILLISECOND);
      if (!bucketType.equalsIgnoreCase("hour")) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        if (bucketType.equalsIgnoreCase("week")) {
          c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
        } else if (bucketType.equalsIgnoreCase("month")) {
          c.set(Calendar.DAY_OF_MONTH, 1);
        } else if (bucketType.equalsIgnoreCase("year")) {
          c.set(Calendar.DAY_OF_YEAR, 1);
        }
      }
      st = c.getTimeInMillis();

      c.setTimeInMillis(et);
      c.clear(Calendar.MINUTE);
      c.clear(Calendar.SECOND);
      c.clear(Calendar.MILLISECOND);
      if (bucketType.equalsIgnoreCase("hour")) {
        c.add(Calendar.HOUR_OF_DAY, 1);
      } else {
        c.set(Calendar.HOUR_OF_DAY, 0);
        if (bucketType.equalsIgnoreCase("day")) {
          c.add(Calendar.DAY_OF_YEAR, 1);
        } else if (bucketType.equalsIgnoreCase("week")) {
          c.add(Calendar.DAY_OF_YEAR, 7);
        } else if (bucketType.equalsIgnoreCase("month")) {
          c.add(Calendar.MONTH, 1);
        } else if (bucketType.equalsIgnoreCase("year")) {
          c.add(Calendar.YEAR, 1);
        }
      }
      et = c.getTimeInMillis();
    }

    // basal metabolic rate is treated in a different way
    // we need to query per day and not all days may have a sample
    // so we query over a week then we take the average
    float basalAVG = 0;
    if (datatype.equalsIgnoreCase("calories.basal")) {
      try {
        basalAVG = getBasalAVG(_et);
      } catch (Exception ex) {
        callbackContext.error(ex.getMessage());
        return;
      }
    }

    DataReadRequest.Builder builder = new DataReadRequest.Builder();
    builder.setTimeRange(st, et, TimeUnit.MILLISECONDS);

    if (datatype.equalsIgnoreCase("steps")) {
      if (args.getJSONObject(0).has("filtered") && args.getJSONObject(0).getBoolean("filtered")) {
        // exceptional case for filtered steps
        DataSource filteredStepsSource = new DataSource.Builder().setDataType(DataType.TYPE_STEP_COUNT_DELTA)
            .setType(DataSource.TYPE_DERIVED).setStreamName("estimated_steps")
            .setAppPackageName("com.google.android.gms").build();
        builder.aggregate(filteredStepsSource, DataType.AGGREGATE_STEP_COUNT_DELTA);
      } else {
        builder.aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA);
      }
    } else if (datatype.equalsIgnoreCase("distance")) {
      builder.aggregate(DataType.TYPE_DISTANCE_DELTA);
    } else if (datatype.equalsIgnoreCase("calories")) {
      builder.aggregate(DataType.TYPE_CALORIES_EXPENDED);
    } else if (datatype.equalsIgnoreCase("calories.basal")) {
      builder.aggregate(DataType.TYPE_BASAL_METABOLIC_RATE);
    } else if (datatype.equalsIgnoreCase("activity")) {
      builder.aggregate(DataType.TYPE_ACTIVITY_SEGMENT);
    } else if (datatype.equalsIgnoreCase("nutrition.water")) {
      builder.aggregate(DataType.TYPE_HYDRATION);
    } else if (datatype.startsWith("nutrition")) {
      builder.aggregate(DataType.TYPE_NUTRITION);
    } else {
      callbackContext.error("Datatype " + datatype + " not supported");
      return;
    }

    if (hasbucket) {
      if (bucketType.equalsIgnoreCase("hour")) {
        builder.bucketByTime(1, TimeUnit.HOURS);
      } else if (bucketType.equalsIgnoreCase("day")) {
        builder.bucketByTime(1, TimeUnit.DAYS);
      } else {
        // use days, then will need to aggregate manually
        builder.bucketByTime(1, TimeUnit.DAYS);
      }
    } else {
      if (datatype.equalsIgnoreCase("activity")) {
        builder.bucketByActivityType(1, TimeUnit.MILLISECONDS);
      } else {
        long allms = et - st;
        if (allms <= Integer.MAX_VALUE) {
          builder.bucketByTime((int) allms, TimeUnit.MILLISECONDS);
        } else {
          builder.bucketByTime((int) (allms / 1000), TimeUnit.SECONDS);
        }
      }
    }

    DataReadRequest readRequest = builder.build();
    Task<DataReadResponse> task = Fitness.getHistoryClient(this.cordova.getContext(), this.account)
        .readData(readRequest);

    try {
      DataReadResponse dataReadResult = Tasks.await(task);
      if (!dataReadResult.getStatus().isSuccess()) {
        callbackContext.error(dataReadResult.getStatus().getStatusMessage());
        return;
      }
      Log.d(TAG, "Got data from query aggregated");
      JSONObject retBucket = null;
      JSONArray retBucketsArr = new JSONArray();
      if (hasbucket) {
        if (customBucket) {
          // create custom buckets, as these are not supported by Google Fit
          Calendar cal = Calendar.getInstance();
          cal.setTimeInMillis(st);
          while (cal.getTimeInMillis() < et) {
            JSONObject customBuck = new JSONObject();
            customBuck.put("startDate", cal.getTimeInMillis());
            if (bucketType.equalsIgnoreCase("week")) {
              cal.add(Calendar.DAY_OF_YEAR, 7);
            } else if (bucketType.equalsIgnoreCase("month")) {
              cal.add(Calendar.MONTH, 1);
            } else {
              cal.add(Calendar.YEAR, 1);
            }
            customBuck.put("endDate", cal.getTimeInMillis());
            retBucketsArr.put(customBuck);
          }
        }
      } else {
        // there will be only one bucket spanning all the period
        retBucket = new JSONObject();
        retBucket.put("startDate", st);
        retBucket.put("endDate", et);
        retBucket.put("value", 0);
        if (datatype.equalsIgnoreCase("steps")) {
          retBucket.put("unit", "count");
        } else if (datatype.equalsIgnoreCase("distance")) {
          retBucket.put("unit", "m");
        } else if (datatype.equalsIgnoreCase("calories")) {
          retBucket.put("unit", "kcal");
        } else if (datatype.equalsIgnoreCase("activity")) {
          retBucket.put("unit", "activitySummary");
          // query per bucket time to get distance and calories per activity
          JSONObject actobj = getAggregatedActivityDistanceCalories(st, et);
          retBucket.put("value", actobj);
        } else if (datatype.equalsIgnoreCase("nutrition.water")) {
          retBucket.put("unit", "ml");
        } else if (datatype.equalsIgnoreCase("nutrition")) {
          retBucket.put("value", new JSONObject());
          retBucket.put("unit", "nutrition");
        } else if (datatype.startsWith("nutrition.")) {
          retBucket.put("unit", nutrientFields.get(datatype).unit);
        }
      }

      for (Bucket bucket : dataReadResult.getBuckets()) {

        if (hasbucket) {
          if (customBucket) {
            // find the bucket among customs
            for (int i = 0; i < retBucketsArr.length(); i++) {
              retBucket = retBucketsArr.getJSONObject(i);
              long bst = retBucket.getLong("startDate");
              long bet = retBucket.getLong("endDate");
              if (bucket.getStartTime(TimeUnit.MILLISECONDS) >= bst
                  && bucket.getEndTime(TimeUnit.MILLISECONDS) <= bet) {
                break;
              }
            }
          } else {
            // pick the current
            retBucket = new JSONObject();
            retBucket.put("startDate", bucket.getStartTime(TimeUnit.MILLISECONDS));
            retBucket.put("endDate", bucket.getEndTime(TimeUnit.MILLISECONDS));
            retBucketsArr.put(retBucket);
          }
          if (!retBucket.has("value")) {
            retBucket.put("value", 0);
            if (datatype.equalsIgnoreCase("steps")) {
              retBucket.put("unit", "count");
            } else if (datatype.equalsIgnoreCase("distance")) {
              retBucket.put("unit", "m");
            } else if (datatype.equalsIgnoreCase("calories")) {
              retBucket.put("unit", "kcal");
            } else if (datatype.equalsIgnoreCase("activity")) {
              retBucket.put("unit", "activitySummary");
              // query per bucket time to get distance and calories per activity
              JSONObject actobj = getAggregatedActivityDistanceCalories(bucket.getStartTime(TimeUnit.MILLISECONDS),
                  bucket.getEndTime(TimeUnit.MILLISECONDS));
              retBucket.put("value", actobj);
            } else if (datatype.equalsIgnoreCase("nutrition.water")) {
              retBucket.put("unit", "ml");
            } else if (datatype.equalsIgnoreCase("nutrition")) {
              retBucket.put("value", new JSONObject());
              retBucket.put("unit", "nutrition");
            } else if (datatype.startsWith("nutrition.")) {
              NutrientFieldInfo fieldInfo = nutrientFields.get(datatype);
              if (fieldInfo != null) {
                retBucket.put("unit", fieldInfo.unit);
              }
            }
          }
        }

        // aggregate data points over the bucket
        boolean atleastone = false;
        for (DataSet dataset : bucket.getDataSets()) {
          for (DataPoint datapoint : dataset.getDataPoints()) {
            atleastone = true;
            if (datatype.equalsIgnoreCase("steps")) {
              int nsteps = datapoint.getValue(Field.FIELD_STEPS).asInt();
              int osteps = retBucket.getInt("value");
              retBucket.put("value", osteps + nsteps);
            } else if (datatype.equalsIgnoreCase("distance")) {
              float ndist = datapoint.getValue(Field.FIELD_DISTANCE).asFloat();
              double odist = retBucket.getDouble("value");
              retBucket.put("value", odist + ndist);
            } else if (datatype.equalsIgnoreCase("calories")) {
              float ncal = datapoint.getValue(Field.FIELD_CALORIES).asFloat();
              double ocal = retBucket.getDouble("value");
              retBucket.put("value", ocal + ncal);
            } else if (datatype.equalsIgnoreCase("calories.basal")) {
              float ncal = datapoint.getValue(Field.FIELD_AVERAGE).asFloat();
              double ocal = retBucket.getDouble("value");
              retBucket.put("value", ocal + ncal);
            } else if (datatype.equalsIgnoreCase("nutrition.water")) {
              float nwat = datapoint.getValue(Field.FIELD_VOLUME).asFloat();
              double owat = retBucket.getDouble("value");
              retBucket.put("value", owat + nwat);
            } else if (datatype.equalsIgnoreCase("nutrition")) {
              JSONObject nutrsob = retBucket.getJSONObject("value");
              if (datapoint.getValue(Field.FIELD_NUTRIENTS) != null) {
                nutrsob = getNutrients(datapoint.getValue(Field.FIELD_NUTRIENTS), nutrsob);
              }
              retBucket.put("value", nutrsob);
            } else if (datatype.startsWith("nutrition.")) {
              Value nutrients = datapoint.getValue(Field.FIELD_NUTRIENTS);
              NutrientFieldInfo fieldInfo = nutrientFields.get(datatype);
              if (fieldInfo != null) {
                float value = nutrients.getKeyValue(fieldInfo.field);
                double total = retBucket.getDouble("value");
                retBucket.put("value", total + value);
              }
            } else if (datatype.equalsIgnoreCase("activity")) {
              String activity = datapoint.getValue(Field.FIELD_ACTIVITY).asActivity();
              int ndur = datapoint.getValue(Field.FIELD_DURATION).asInt();
              JSONObject actobj = retBucket.getJSONObject("value");
              JSONObject summary;
              if (actobj.has(activity)) {
                summary = actobj.getJSONObject(activity);
                int odur = summary.getInt("duration");
                summary.put("duration", odur + ndur);
              } else {
                summary = new JSONObject();
                summary.put("duration", ndur);
              }
              actobj.put(activity, summary);
              retBucket.put("value", actobj);
            }
          }
        } // end of data set loop
        if (datatype.equalsIgnoreCase("calories.basal")) {
          double basals = retBucket.getDouble("value");
          if (!atleastone) {
            // when no basal is available, use the daily average
            basals += basalAVG;
            retBucket.put("value", basals);
          }
          // if the bucket is not daily, it needs to be normalised
          if (!hasbucket || bucketType.equalsIgnoreCase("hour")) {
            long sst = retBucket.getLong("startDate");
            long eet = retBucket.getLong("endDate");
            basals = (basals / (24 * 60 * 60 * 1000)) * (eet - sst);
            retBucket.put("value", basals);
          }
        }
      } // end of buckets loop
      if (hasbucket)
        callbackContext.success(retBucketsArr);
      else
        callbackContext.success(retBucket);
    } catch (Exception e) {
      callbackContext.error(e.getMessage());
    }
  }

  private JSONObject getAggregatedActivityDistanceCalories(long st, long et) throws Exception {
    JSONObject actobj = new JSONObject();

    DataReadRequest readActivityDistCalRequest = new DataReadRequest.Builder().aggregate(DataType.TYPE_DISTANCE_DELTA)
        .aggregate(DataType.TYPE_CALORIES_EXPENDED).bucketByActivityType(1, TimeUnit.SECONDS)
        .setTimeRange(st, et, TimeUnit.MILLISECONDS).build();

    Task<DataReadResponse> task = Fitness.getHistoryClient(this.cordova.getContext(), this.account)
        .readData(readActivityDistCalRequest);

    DataReadResponse dataActivityDistCalReadResult = Tasks.await(task);
    if (!dataActivityDistCalReadResult.getStatus().isSuccess()) {
      throw new Exception(dataActivityDistCalReadResult.getStatus().getStatusMessage());
    }

    for (Bucket activityBucket : dataActivityDistCalReadResult.getBuckets()) {
      // each bucket is an activity
      float distance = 0;
      float calories = 0;
      String activity = activityBucket.getActivity();

      DataSet distanceDataSet = activityBucket.getDataSet(DataType.AGGREGATE_DISTANCE_DELTA);
      for (DataPoint datapoint : distanceDataSet.getDataPoints()) {
        distance += datapoint.getValue(Field.FIELD_DISTANCE).asFloat();
      }

      DataSet caloriesDataSet = activityBucket.getDataSet(DataType.AGGREGATE_CALORIES_EXPENDED);
      for (DataPoint datapoint : caloriesDataSet.getDataPoints()) {
        calories += datapoint.getValue(Field.FIELD_CALORIES).asFloat();
      }

      JSONObject summary;
      if (actobj.has(activity)) {
        summary = actobj.getJSONObject(activity);
        double existingdistance = summary.getDouble("distance");
        summary.put("distance", distance + existingdistance);
        double existingcalories = summary.getDouble("calories");
        summary.put("calories", calories + existingcalories);
      } else {
        summary = new JSONObject();
        summary.put("duration", 0); // sum onto this whilst aggregating over buckets.
        summary.put("distance", distance);
        summary.put("calories", calories);
      }

      actobj.put(activity, summary);
    }
    return actobj;
  }

  // utility function that gets the basal metabolic rate averaged over a week
  private float getBasalAVG(long _et) throws Exception {
    float basalAVG = 0;
    Calendar cal = java.util.Calendar.getInstance();
    cal.setTime(new Date(_et));
    // set start time to a week before end time
    cal.add(Calendar.WEEK_OF_YEAR, -1);
    long nst = cal.getTimeInMillis();

    DataReadRequest.Builder builder = new DataReadRequest.Builder();
    builder.aggregate(DataType.TYPE_BASAL_METABOLIC_RATE);
    builder.bucketByTime(1, TimeUnit.DAYS);
    builder.setTimeRange(nst, _et, TimeUnit.MILLISECONDS);
    DataReadRequest readRequest = builder.build();

    Task<DataReadResponse> task = Fitness.getHistoryClient(this.cordova.getContext(), this.account)
        .readData(readRequest);

    DataReadResponse dataReadResult = Tasks.await(task);

    if (!dataReadResult.getStatus().isSuccess()) {
      throw new Exception(dataReadResult.getStatus().getStatusMessage());
    }

    JSONObject obj = new JSONObject();
    int avgsN = 0;
    for (Bucket bucket : dataReadResult.getBuckets()) {
      // in the com.google.bmr.summary data type, each data point represents
      // the average, maximum and minimum basal metabolic rate, in kcal per day, over
      // the time interval of the data point.
      DataSet ds = bucket.getDataSet(DataType.AGGREGATE_BASAL_METABOLIC_RATE_SUMMARY);
      for (DataPoint dp : ds.getDataPoints()) {
        float avg = dp.getValue(Field.FIELD_AVERAGE).asFloat();
        basalAVG += avg;
        avgsN++;
      }
    }
    // do the average of the averages
    if (avgsN != 0)
      basalAVG /= avgsN; // this a daily average
    return basalAVG;
  }

  // stores a data point
  private void store(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
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
    if (!args.getJSONObject(0).has("value")) {
      callbackContext.error("Missing argument value");
      return;
    }
    if (!args.getJSONObject(0).has("sourceName")) {
      callbackContext.error("Missing argument sourceName");
      return;
    }

    String sourceBundleId = cordova.getActivity().getApplicationContext().getPackageName();
    if (args.getJSONObject(0).has("sourceBundleId")) {
      sourceBundleId = args.getJSONObject(0).getString("sourceBundleId");
    }

    DataType dt = datatypes.get(datatype);
    ;
    if (dt == null) {
      callbackContext.error("Datatype " + datatype + " not supported");
      return;
    }

    DataSource datasrc = new DataSource.Builder().setAppPackageName(sourceBundleId).setDataType(dt)
        .setType(DataSource.TYPE_RAW).build();

    DataSet.Builder dataSetBuilder = DataSet.builder(datasrc);
    DataPoint.Builder datapointBuilder = DataPoint.builder(datasrc);
    datapointBuilder.setTimeInterval(st, et, TimeUnit.MILLISECONDS);
    if (dt.equals(DataType.TYPE_STEP_COUNT_DELTA)) {
      String value = args.getJSONObject(0).getString("value");
      int steps = Integer.parseInt(value);
      datapointBuilder.setField(Field.FIELD_STEPS, steps);
    } else if (dt.equals(DataType.TYPE_DISTANCE_DELTA)) {
      String value = args.getJSONObject(0).getString("value");
      float dist = Float.parseFloat(value);
      datapointBuilder.setField(Field.FIELD_DISTANCE, dist);
    } else if (dt.equals(DataType.TYPE_CALORIES_EXPENDED)) {
      String value = args.getJSONObject(0).getString("value");
      float cals = Float.parseFloat(value);
      datapointBuilder.setField(Field.FIELD_CALORIES, cals);
    } else if (dt.equals(DataType.TYPE_HEIGHT)) {
      String value = args.getJSONObject(0).getString("value");
      float height = Float.parseFloat(value);
      datapointBuilder.setField(Field.FIELD_HEIGHT, height);
    } else if (dt.equals(DataType.TYPE_WEIGHT)) {
      String value = args.getJSONObject(0).getString("value");
      float weight = Float.parseFloat(value);
      datapointBuilder.setField(Field.FIELD_WEIGHT, weight);
    } else if (dt.equals(DataType.TYPE_HEART_RATE_BPM)) {
      String value = args.getJSONObject(0).getString("value");
      float hr = Float.parseFloat(value);
      datapointBuilder.setField(Field.FIELD_BPM, hr);
    } else if (dt.equals(DataType.TYPE_BODY_FAT_PERCENTAGE)) {
      String value = args.getJSONObject(0).getString("value");
      float perc = Float.parseFloat(value);
      datapointBuilder.setField(Field.FIELD_PERCENTAGE, perc);
    } else if (dt.equals(DataType.TYPE_ACTIVITY_SEGMENT)) {
      String value = args.getJSONObject(0).getString("value");
      datapointBuilder.setField(Field.FIELD_ACTIVITY, value);
    } else if (dt.equals(DataType.TYPE_HYDRATION)) {
      float nuv = (float) args.getJSONObject(0).getDouble("value");
      datapointBuilder.setField(Field.FIELD_VOLUME, nuv);
    } else if (dt.equals(DataType.TYPE_NUTRITION)) {
      if (datatype.startsWith("nutrition.")) {
        // it's a single nutrient
        NutrientFieldInfo nuf = nutrientFields.get(datatype);
        float nuv = (float) args.getJSONObject(0).getDouble("value");
        Map<String, Float> value = new HashMap<>();
        value.put(nuf.field, nuv);
        datapointBuilder.setField(Field.FIELD_NUTRIENTS, value);
      } else {
        // it's a nutrition object
        JSONObject nutrobj = args.getJSONObject(0).getJSONObject("value");
        String mealtype = nutrobj.getString("meal_type");
        if (mealtype != null && !mealtype.isEmpty()) {
          if (mealtype.equalsIgnoreCase("breakfast"))
            datapointBuilder.setField(Field.FIELD_MEAL_TYPE, Field.MEAL_TYPE_BREAKFAST);
          else if (mealtype.equalsIgnoreCase("lunch"))
            datapointBuilder.setField(Field.FIELD_MEAL_TYPE, Field.MEAL_TYPE_LUNCH);
          else if (mealtype.equalsIgnoreCase("snack"))
            datapointBuilder.setField(Field.FIELD_MEAL_TYPE, Field.MEAL_TYPE_SNACK);
          else if (mealtype.equalsIgnoreCase("dinner"))
            datapointBuilder.setField(Field.FIELD_MEAL_TYPE, Field.MEAL_TYPE_DINNER);
          else
            datapointBuilder.setField(Field.FIELD_MEAL_TYPE, Field.MEAL_TYPE_UNKNOWN);
        }
        String item = nutrobj.getString("item");
        if (item != null && !item.isEmpty()) {
          datapointBuilder.setField(Field.FIELD_FOOD_ITEM, item);
        }
        JSONObject nutrientsobj = nutrobj.getJSONObject("nutrients");
        if (nutrientsobj != null) {
          Map<String, Float> value = new HashMap<>();
          Iterator<String> nutrients = nutrientsobj.keys();
          while (nutrients.hasNext()) {
            String nutrientName = nutrients.next();
            NutrientFieldInfo nuf = nutrientFields.get(nutrientName);
            if (nuf != null) {
              float nuv = (float) nutrientsobj.getDouble(nutrientName);
              value.put(nuf.field, nuv);
            }
          }
          datapointBuilder.setField(Field.FIELD_NUTRIENTS, value);
        }
      }
    } else if (dt.equals(HealthDataTypes.TYPE_BLOOD_GLUCOSE)) {
      JSONObject glucoseobj = args.getJSONObject(0).getJSONObject("value");
      float glucose = (float) glucoseobj.getDouble("glucose");
      datapointBuilder.setField(HealthFields.FIELD_BLOOD_GLUCOSE_LEVEL, glucose);

      if (glucoseobj.has("meal")) {
        String meal = glucoseobj.getString("meal");
        int mealType = Field.MEAL_TYPE_UNKNOWN;
        int relationToMeal = HealthFields.FIELD_TEMPORAL_RELATION_TO_MEAL_GENERAL;
        if (meal.equalsIgnoreCase("fasting")) {
          mealType = Field.MEAL_TYPE_UNKNOWN;
          relationToMeal = HealthFields.FIELD_TEMPORAL_RELATION_TO_MEAL_FASTING;
        } else {
          if (meal.startsWith("before_")) {
            relationToMeal = HealthFields.FIELD_TEMPORAL_RELATION_TO_MEAL_BEFORE_MEAL;
            meal = meal.substring("before_".length());
          } else if (meal.startsWith("after_")) {
            relationToMeal = HealthFields.FIELD_TEMPORAL_RELATION_TO_MEAL_AFTER_MEAL;
            meal = meal.substring("after_".length());
          }
          if (meal.equalsIgnoreCase("dinner")) {
            mealType = Field.MEAL_TYPE_DINNER;
          } else if (meal.equalsIgnoreCase("lunch")) {
            mealType = Field.MEAL_TYPE_LUNCH;
          } else if (meal.equalsIgnoreCase("snack")) {
            mealType = Field.MEAL_TYPE_SNACK;
          } else if (meal.equalsIgnoreCase("breakfast")) {
            mealType = Field.MEAL_TYPE_BREAKFAST;
          }
        }
        datapointBuilder.setField(HealthFields.FIELD_TEMPORAL_RELATION_TO_MEAL, relationToMeal);
        datapointBuilder.setField(Field.FIELD_MEAL_TYPE, mealType);
      }

      if (glucoseobj.has("sleep")) {
        String sleep = glucoseobj.getString("sleep");
        int relationToSleep = HealthFields.TEMPORAL_RELATION_TO_SLEEP_FULLY_AWAKE;
        if (sleep.equalsIgnoreCase("before_sleep")) {
          relationToSleep = HealthFields.TEMPORAL_RELATION_TO_SLEEP_BEFORE_SLEEP;
        } else if (sleep.equalsIgnoreCase("on_waking")) {
          relationToSleep = HealthFields.TEMPORAL_RELATION_TO_SLEEP_ON_WAKING;
        } else if (sleep.equalsIgnoreCase("during_sleep")) {
          relationToSleep = HealthFields.TEMPORAL_RELATION_TO_SLEEP_DURING_SLEEP;
        }
        datapointBuilder.setField(HealthFields.FIELD_TEMPORAL_RELATION_TO_SLEEP, relationToSleep);
      }

      if (glucoseobj.has("source")) {
        String source = glucoseobj.getString("source");
        int specimenSource = HealthFields.BLOOD_GLUCOSE_SPECIMEN_SOURCE_CAPILLARY_BLOOD;
        if (source.equalsIgnoreCase("interstitial_fluid")) {
          specimenSource = HealthFields.BLOOD_GLUCOSE_SPECIMEN_SOURCE_INTERSTITIAL_FLUID;
        } else if (source.equalsIgnoreCase("plasma")) {
          specimenSource = HealthFields.BLOOD_GLUCOSE_SPECIMEN_SOURCE_PLASMA;
        } else if (source.equalsIgnoreCase("serum")) {
          specimenSource = HealthFields.BLOOD_GLUCOSE_SPECIMEN_SOURCE_SERUM;
        } else if (source.equalsIgnoreCase("tears")) {
          specimenSource = HealthFields.BLOOD_GLUCOSE_SPECIMEN_SOURCE_TEARS;
        } else if (source.equalsIgnoreCase("whole_blood")) {
          specimenSource = HealthFields.BLOOD_GLUCOSE_SPECIMEN_SOURCE_WHOLE_BLOOD;
        }
        datapointBuilder.setField(HealthFields.FIELD_BLOOD_GLUCOSE_SPECIMEN_SOURCE, specimenSource);
      }
    } else if (dt == HealthDataTypes.TYPE_BLOOD_PRESSURE) {
      JSONObject bpobj = args.getJSONObject(0).getJSONObject("value");
      if (bpobj.has("systolic")) {
        float systolic = (float) bpobj.getDouble("systolic");
        datapointBuilder.setField(HealthFields.FIELD_BLOOD_PRESSURE_SYSTOLIC, systolic);
      }
      if (bpobj.has("diastolic")) {
        float diastolic = (float) bpobj.getDouble("diastolic");
        datapointBuilder.setField(HealthFields.FIELD_BLOOD_PRESSURE_DIASTOLIC, diastolic);
      }
    } else if (dt == HealthDataTypes.TYPE_BODY_TEMPERATURE) {
      float bodyTemp = (float) args.getJSONObject(0).getDouble("value");
      datapointBuilder.setField(HealthFields.FIELD_BLOOD_PRESSURE_DIASTOLIC, bodyTemp);
    }
    dataSetBuilder.add(datapointBuilder.build());

    Fitness.getHistoryClient(this.cordova.getContext(), this.account).insertData(dataSetBuilder.build())
        .addOnSuccessListener(r -> {
          callbackContext.success();
        }).addOnFailureListener(err -> {
          err.getCause().printStackTrace();
          callbackContext.error(err.getMessage());
        });
  }

  // deletes data points in a given time window
  private void delete(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
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
    if (!args.getJSONObject(0).has("dataType")) {
      callbackContext.error("Missing argument dataType");
      return;
    }
    final String datatype = args.getJSONObject(0).getString("dataType");

    DataType dt = datatypes.get(datatype);
    if (dt == null) {
      callbackContext.error("Datatype " + datatype + " not supported");
      return;
    }

    DataDeleteRequest request = new DataDeleteRequest.Builder().setTimeInterval(st, et, TimeUnit.MILLISECONDS)
        .addDataType(dt).build();

    Fitness.getHistoryClient(this.cordova.getContext(), this.account).deleteData(request).addOnSuccessListener(r -> {
      callbackContext.success();
    }).addOnFailureListener(err -> {
      err.getCause().printStackTrace();
      callbackContext.error(err.getMessage());
    });
  }
}

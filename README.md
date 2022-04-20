# Cordova Health Plugin


A plugin that abstracts fitness and health repositories like Apple HealthKit or Google Fit.

This work is based on [cordova plugin googlefit](https://github.com/2dvisio/cordova-plugin-googlefit) and on [cordova healthkit plugin](https://github.com/Telerik-Verified-Plugins/HealthKit)

For an introduction about Google Fit versus HealthKit see [this very good article](https://yalantis.com/blog/how-can-healthkit-and-googlefit-help-you-develop-healthcare-and-fitness-apps/).

This plugin is kept up to date and requires a recent version of cordova (6 and on) as well as recent iOS and Android SDKs.

If you have any question or small issue, please use the [gitter channel](https://gitter.im/cordova-plugin-health/Lobby).

## Warning

Google is rolling out a new, stricter, policy for using their Fitness API. Read about it [here](https://developers.google.com/fit/policy).
The API has also introduced [new ways for retrieving heart rate and sleep](https://developers.google.com/fit/android/releases#october_19_2020), but these have not been implemented yet in this plugin (help is appreciated!).

## Installation

In Cordova:

```
cordova plugin add cordova-plugin-health --variable HEALTH_READ_PERMISSION='App needs read access' --variable HEALTH_WRITE_PERMISSION='App needs write access'
```

`HEALTH_READ_PERMISSION` and `HEALTH_WRITE_PERMISSION` are shown when the app tries to grant access to data in HealthKit.

`FIT_API_VERSION` and `PLAY_AUTH_VERSION` allow you to override the google services version.

Phonegap Build `config.xml`:

```
<!-- Health plugin -->
<plugin name="cordova-plugin-health" source="npm">
  <variable name="HEALTH_READ_PERMISSION" value="App needs read access"/>
  <variable name="HEALTH_WRITE_PERMISSION" value="App needs write access"/>
  <variable name="PLAY_AUTH_VERSION" value="19.0.0"/>
  <variable name="FIT_API_VERSION" value="20.0.0"/>
</plugin>

<!-- Only if iOS -->

<!-- Read access -->
<config-file platform="ios" parent="NSHealthShareUsageDescription">
  <string>App needs read access</string>
</config-file>
<!-- Write access -->
<config-file platform="ios" parent="NSHealthUpdateUsageDescription">
  <string>App needs write access</string>
</config-file>
```

If, for some reason, the Info.plist loses the HEALTH_READ_PERMISSION and HEALTH_WRITE_PERMISSION, you probably need to add the following to your project's package.json:

```
{
  "cordova": {
    "plugins": {
     "cordova-plugin-health": {
        "HEALTH_READ_PERMISSION": "App needs read access",
        "HEALTH_WRITE_PERMISSION": "App needs write access"
      },
    },
  }
}
```

This is known to happen when using the Ionic Package cloud service.

## iOS requirements

* Make sure your app id has the 'HealthKit' entitlement when this plugin is installed (see iOS dev center).
* Also, make sure your app and App Store description comply with the [Apple review guidelines](https://developer.apple.com/app-store/review/guidelines/#healthkit).
* There are [two keys](https://developer.apple.com/library/content/documentation/General/Reference/InfoPlistKeyReference/Articles/CocoaKeys.html#//apple_ref/doc/uid/TP40009251-SW48) to be added to the info.plist file: `NSHealthShareUsageDescription` and `NSHealthUpdateUsageDescription`. These are assigned with a default string by the plugin, but you may want to contextualise them for your app.

## Android requirements

* You need to have the Google Services API downloaded in your SDK.
* Be sure to give your app access to the Google Fitness API, see [this](https://developers.google.com/fit/android/get-api-key).
* If you are wondering what key your compiled app is using, you can type `keytool -list -printcert -jarfile yourapp.apk`.
* If you haven't configured the APIs correctly, particularly the OAuth requirements, you are likely to get 'User cancelled the dialog' as an error message. This often happens if you mismatch the signing certificate and SHA-1 fingerprint.
* You will need to add permissions in AndroidManifest.xml via config.xml depending on the data types you are requesting. See [this issue](https://github.com/dariosalvi78/cordova-plugin-health/issues/178#issuecomment-645937435) for an example
* You can use the Google Fitness API even if the user doesn't have Google Fit installed, but there has to be some other fitness app putting data into the Fitness API otherwise your queries will always be empty. See the [the original documentation](https://developers.google.com/fit/overview).
* Google has launched a [verification process](https://support.google.com/cloud/answer/9110914) for the app. This is needed for all the data types in Google Fit. Check [their guidelines](https://developers.google.com/terms/api-services-user-data-policy).
* You can change which Google Play Services Fitness API version this plugin uses by setting the `FIT_API_VERSION` variable in `config.xml` and the version of the Auth API using `PLAY_AUTH_VERSION`. By default it will use version `20.0.0` for play-services-fitness and version `19.0.0` for play-services-auth (see [release notes](https://developers.google.com/android/guides/releases#october_19_2020)). From version 15 of the Play Services [you don't have to use the same version](https://developers.google.com/android/guides/versioning) accross all your cordova plugins. You can track google services releases [here](https://developers.google.com/android/guides/releases).
* This plugin uses AndroidX. You will need to [activate AndroidX](https://cordova.apache.org/announcements/2020/06/29/cordova-android-9.0.0.html) in the Android platform and make sure all other plugins you use are AndroidX compatible.

## Supported data types

As HealthKit does not allow adding custom data types, only a subset of data types supported by HealthKit has been chosen.

| Data type       | Unit  |    HealthKit equivalent                       |  Google Fit equivalent                   |
|-----------------|-------|-----------------------------------------------|------------------------------------------|
| mindfulness     | min   | HKCategoryTypeIdentifierMindfulSession        | N/A                                      |
| steps           | count | HKQuantityTypeIdentifierStepCount             | TYPE_STEP_COUNT_DELTA                    |
| stairs          | count | HKQuantityTypeIdentifierFlightsClimbed             | NA                    |
| distance        | m     | HKQuantityTypeIdentifierDistanceWalkingRunning + HKQuantityTypeIdentifierDistanceCycling | TYPE_DISTANCE_DELTA |
| appleExerciseTime | min | HKQuantityTypeIdentifierAppleExerciseTime     | NA                                       |
| flights_climbed | count | HKQuantityTypeIdentifierFlightsClimbed       |                                          |
| stand_time | min | HKQuantityTypeIdentifierDietaryVitaminE       |                                          |
| calories        | kcal  | HKQuantityTypeIdentifierActiveEnergyBurned + HKQuantityTypeIdentifierBasalEnergyBurned | TYPE_CALORIES_EXPENDED |
| calories.active | kcal  | HKQuantityTypeIdentifierActiveEnergyBurned    | TYPE_CALORIES_EXPENDED - (TYPE_BASAL_METABOLIC_RATE * time window) |
| calories.basal  | kcal  | HKQuantityTypeIdentifierBasalEnergyBurned     | TYPE_BASAL_METABOLIC_RATE * time window  |
| activity        | activityType | HKWorkoutTypeIdentifier + HKCategoryTypeIdentifierSleepAnalysis | TYPE_ACTIVITY_SEGMENT |
| height          | m     | HKQuantityTypeIdentifierHeight                | TYPE_HEIGHT                              |
| weight          | kg    | HKQuantityTypeIdentifierBodyMass              | TYPE_WEIGHT                              |
| bmi            | count | HKQuantityTypeIdentifierBodyMassIndex       |                                          |
| lean_body_mass | kg | HKQuantityTypeIdentifierLeanBodyMass       |                                          |
| heart_rate      | count/min | HKQuantityTypeIdentifierHeartRate         | TYPE_HEART_RATE_BPM                      |
| heart_rate.resting | count/min | HKQuantityTypeIdentifierRestingHearRate | TBD                      |
| heart_rate.variability      | ms | HKQuantityTypeIdentifierHeartRateVariabilitySDNN         | NA                   |
| resp_rate       | count/min | HKQuantityTypeIdentifierRespiratoryRate   | TBD                      |
| oxygen_saturation | %       | HKQuantityTypeIdentifierOxygenSaturation  | TYPE_OXYGEN_SATURATION                   |
| vo2max          | ml/(kg * min) | HKQuantityTypeIdentifierVO2Max   | TBD                      |
| temperature     | Celsius | HKQuantityTypeIdentifierBodyTemperature       | TBD                      |
| fat_percentage  | %     | HKQuantityTypeIdentifierBodyFatPercentage     | TYPE_BODY_FAT_PERCENTAGE                 |
| waist_circumference | m     | HKQuantityTypeIdentifierWaistCircumference     | NA                    |
| blood_glucose   | mmol/L | HKQuantityTypeIdentifierBloodGlucose         | TYPE_BLOOD_GLUCOSE                       |
| insulin         | IU    | HKQuantityTypeIdentifierInsulinDelivery       | NA                                       |
| blood_pressure  | mmHg  | HKCorrelationTypeIdentifierBloodPressure      | TYPE_BLOOD_PRESSURE                      |
| blood_pressure_systolic | mmHg     | HKQuantityTypeIdentifierBloodPressureSystolic     | NA                    |
| blood_pressure_diastolic | mmHg     | HKQuantityTypeIdentifierBloodPressureDiastolic     | NA                    |
| gender          |       | HKCharacteristicTypeIdentifierBiologicalSex   | NA        |
| date_of_birth   |       | HKCharacteristicTypeIdentifierDateOfBirth     | NA        |
| mindfulness     | sec   | HKCategoryTypeIdentifierMindfulSession        | NA                                       |
| sleep | N/A | HKCategoryTypeIdentifierSleepAnalysis       |                                          |
| nutrition       |       | HKCorrelationTypeIdentifierFood               | TYPE_NUTRITION                           |
| nutrition.biotin | mcg | HKQuantityTypeIdentifierDietaryBiotin         |                                          |
| nutrition.chloride | mg | HKQuantityTypeIdentifierDietaryChloride       |                                          |
| nutrition.chromium | mcg | HKQuantityTypeIdentifierDietaryChromium       |                                          |
| nutrition.copper | mg | HKQuantityTypeIdentifierDietaryCopper       |                                          |
| nutrition.folate | mcg | HKQuantityTypeIdentifierDietaryFolate       |                                          |
| nutrition.iodine | mcg | HKQuantityTypeIdentifierDietaryIodine       |                                          |
| nutrition.magnesium | mg | HKQuantityTypeIdentifierDietaryMagnesium       |                                          |
| nutrition.molybdenum | mg | HKQuantityTypeIdentifierDietaryMolybdenum       |                                          |
| nutrition.niacin | mg | HKQuantityTypeIdentifierDietaryNiacin       |                                          |
| nutrition.pantothenic_acid | mg | HKQuantityTypeIdentifierDietaryPantothenicAcid       |                                          |
| nutrition.phosphorus | mg | HKQuantityTypeIdentifierDietaryPhosphorus       |                                          |
| nutrition.riboflavin | mg | HKQuantityTypeIdentifierDietaryRiboflavin       |                                          |
| nutrition.selenium | mcg | HKQuantityTypeIdentifierDietarySelenium       |                                          |
| nutrition.thiamin | mg | HKQuantityTypeIdentifierDietaryThiamin       |                                          |
| nutrition.vitamin_B6 | mg | HKQuantityTypeIdentifierDietaryVitaminB6       |                                          |
| nutrition.vitamin_B12 | mcg | HKQuantityTypeIdentifierDietaryVitaminB12       |                                          |
| nutrition.vitamin_D | mcg | HKQuantityTypeIdentifierDietaryVitaminD       |                                          |
| nutrition.vitamin_E | mg | HKQuantityTypeIdentifierDietaryVitaminE       |                                          |
| nutrition.vitamin_K | mcg | HKQuantityTypeIdentifierDietaryVitaminK       |                                          |
| nutrition.zinc | mg | HKQuantityTypeIdentifierDietaryZinc       |                                          |
| nutrition.calories | kcal | HKQuantityTypeIdentifierDietaryEnergyConsumed | TYPE_NUTRITION, NUTRIENT_CALORIES      |
| nutrition.fat.total | g | HKQuantityTypeIdentifierDietaryFatTotal       | TYPE_NUTRITION, NUTRIENT_TOTAL_FAT       |
| nutrition.fat.saturated | g | HKQuantityTypeIdentifierDietaryFatSaturated | TYPE_NUTRITION, NUTRIENT_SATURATED_FAT |
| nutrition.fat.unsaturated | g | NA                                      | TYPE_NUTRITION, NUTRIENT_UNSATURATED_FAT |
| nutrition.fat.polyunsaturated | g | HKQuantityTypeIdentifierDietaryFatPolyunsaturated | TYPE_NUTRITION, NUTRIENT_POLYUNSATURATED_FAT |
| nutrition.fat.monounsaturated | g | HKQuantityTypeIdentifierDietaryFatMonounsaturated | TYPE_NUTRITION, NUTRIENT_MONOUNSATURATED_FAT |
| nutrition.fat.trans | g | NA                                            | TYPE_NUTRITION, NUTRIENT_TRANS_FAT (g)   |
| nutrition.cholesterol | mg | HKQuantityTypeIdentifierDietaryCholesterol | TYPE_NUTRITION, NUTRIENT_CHOLESTEROL     |
| nutrition.sodium | mg   | HKQuantityTypeIdentifierDietarySodium         | TYPE_NUTRITION, NUTRIENT_SODIUM          |
| nutrition.potassium | mg | HKQuantityTypeIdentifierDietaryPotassium     | TYPE_NUTRITION, NUTRIENT_POTASSIUM       |
| nutrition.carbs.total | g | HKQuantityTypeIdentifierDietaryCarbohydrates | TYPE_NUTRITION, NUTRIENT_TOTAL_CARBS    |
| nutrition.dietary_fiber | g | HKQuantityTypeIdentifierDietaryFiber      | TYPE_NUTRITION, NUTRIENT_DIETARY_FIBER   |
| nutrition.sugar | g     | HKQuantityTypeIdentifierDietarySugar          | TYPE_NUTRITION, NUTRIENT_SUGAR           |
| nutrition.protein | g   | HKQuantityTypeIdentifierDietaryProtein        | TYPE_NUTRITION, NUTRIENT_PROTEIN         |
| nutrition.vitamin_a | mcg (HK), IU (GF) | HKQuantityTypeIdentifierDietaryVitaminA | TYPE_NUTRITION, NUTRIENT_VITAMIN_A |
| nutrition.vitamin_c | mg | HKQuantityTypeIdentifierDietaryVitaminC | TYPE_NUTRITION, NUTRIENT_VITAMIN_C            |
| nutrition.calcium | mg  | HKQuantityTypeIdentifierDietaryCalcium        | TYPE_NUTRITION, NUTRIENT_CALCIUM         |
| nutrition.iron  | mg    | HKQuantityTypeIdentifierDietaryIron           | TYPE_NUTRITION, NUTRIENT_IRON            |
| nutrition.water | ml    | HKQuantityTypeIdentifierDietaryWater          | TYPE_HYDRATION                           |
| nutrition.caffeine | g  | HKQuantityTypeIdentifierDietaryCaffeine       | NA                                       |

**Note**: units of measurement are fixed!

Returned objects contain a set of fixed fields:

- startDate: {type: Date} a date indicating when the data point starts
- endDate: {type: Date} a date indicating when the data point ends
- unit: {type: String} the unit of measurement
- value: the actual value
- sourceBundleId: {type: String} the identifier of the app that produced the data
- sourceName: {type: String} (only on iOS) the name of the app that produced the data (as it appears to the user)
- id: {type: String} (only on iOS) the unique identifier of that measurement

Example values:

| Data type      | Value                             |
|----------------|-----------------------------------|
| steps          | 34                                |
| distance       | 101.2                             |
| appleExerciseTime | 24 <br/>**Notes**: only available on iOS|
| calories       | 245.3                             |
| activity       | "walking"<br />**Notes**: recognized activities and their mappings in Google Fit / HealthKit can be found [here](activities_map.md) <br /> the query also returns calories (kcal) and distance (m)<br />**Warning** If you want to fetch activities you also have to request permission for 'calories' and 'distance' (Android). |
| height         | 1.85                             |
| weight         | 83.3                              |
| heart_rate     | 66                                |
| heart_rate.resting | 63                            |
| heart_rate.variability | 100                       |
| resp_rate      | 12                                |
| vo2max         | 34                                |
| temperature    | 36.2                              |
| fat_percentage | 0.312                             |
| waist_circumference | 0.65                         |
| blood_glucose  | { glucose: 5.5, meal: 'breakfast', sleep: 'fully_awake', source: 'capillary_blood' }<br />**Notes**: <br />to convert to mg/dL, multiply by `18.01559` ([The molar mass of glucose is 180.1559](http://www.convertunits.com/molarmass/Glucose))<br />`meal` can be: 'before_meal' (iOS only), 'after_meal' (iOS only), 'fasting', 'breakfast', 'dinner', 'lunch', 'snack', 'unknown', 'before_breakfast', 'before_dinner', 'before_lunch', 'before_snack', 'after_breakfast', 'after_dinner', 'after_lunch', 'after_snack'<br />`sleep` can be: 'fully_awake', 'before_sleep', 'on_waking', 'during_sleep'<br />`source` can be: 'capillary_blood' ,'interstitial_fluid', 'plasma', 'serum', 'tears', whole_blood' |
| insulin        | { insulin: 2.3, reason: 'bolus' }<br />**Notes**: only available on iOS<br />`reason` can be 'bolus' or 'basal' |
| blood_pressure | { systolic: 110, diastolic: 70 }  |
| blood_pressure_systolic | 110  |
| blood_pressure_diastolic | 70  |
| gender         | "male" <br/>**Notes**: only available on iOS |
| date_of_birth  | { day: 3, month: 12, year: 1978 } <br/>**Notes**: currently only available on iOS |
| mindfulness     | 1800 <br/>**Notes**: only available on iOS |
| nutrition      | { item: "cheese", meal_type: "lunch", brand_name: "McDonald's", nutrients: { nutrition.fat.saturated: 11.5, nutrition.calories: 233.1 } } <br/>**Note**: the `brand_name` property is only available on iOS |
| nutrition.X    | 12.4                              |
| sleep       | 'sleep.light' <br />**Notes**: can be sleep, sleep.awake, sleep.outOfBed, sleep.deep, sleep.light, sleep.rem <br/> recognized sleep stages and their mappings in Google Fit / HealthKit can be found [here](activities_map.md) |

## Methods

### isAvailable()

Tells if either Google Fit or HealthKit are available.

```
navigator.health.isAvailable(successCallback, errorCallback)
```

- successCallback: {type: function(available)}, if available a true is passed as argument, false otherwise
- errorCallback: {type: function(err)}, called if something went wrong, err contains a textual description of the problem

#### Android quirks

- To check for installation of Google Fit, you will need to add `<package android:name="com.google.android.apps.fitness" />` under `queries` to AndroidManifest.xml via config.xml. See [this article](https://medium.com/androiddevelopers/package-visibility-in-android-11-cc857f221cd9) for info

### promptInstallFit() - Android only

Checks if recent Google Play Services and Google Fit are installed.
If the play services are not installed, or are obsolete, it will show a pop-up suggesting to download them.
If Google Fit is not installed, it will open the Play Store at the location of the Google Fit app.
The plugin does not wait until the missing packages are installed, it will return immediately.
If both Play Services and Google Fit are available, this function just returns without any visible effect.

This function is only available on Android.


```
navigator.health.promptInstallFit(successCallback, errorCallback)
```

- successCallback: {type: function()}, called if the function was called
- errorCallback: {type: function(err)}, called if something went wrong


### requestAuthorization()

Requests read and write access to a set of data types.
It is recommendable to always explain why the app needs access to the data before asking the user to authorize it.

**Important:** this method must be called before using the query and store methods, even if the authorization has already been given at some point in the past.
Failure to do so may cause your app to crash, or in the case of Android, Google Fit may not be ready.

```
navigator.health.requestAuthorization(datatypes, successCallback, errorCallback)
```

- datatypes: {type: Mixed array}, a list of data types you want to be granted access to. You can also specify read or write only permissions.
```javascript
[
  'calories', 'distance',   // Read and write permissions
  {
    read : ['steps'],       // Read only permission
    write : ['height', 'weight']  // Write only permission
  }
]
```
- successCallback: {type: function}, called if all OK
- errorCallback: {type: function(err)}, called if something went wrong, err contains a textual description of the problem

#### Android quirks

- It will try to get authorization from the Google fitness APIs. It is necessary that the app's package name and the signing key are registered in the Google API console (see [here](https://developers.google.com/fit/android/get-api-key)).
- Be aware that if the activity is destroyed (e.g. after a rotation) or is put in background, the connection to Google Fit may be lost without any callback. Going through the authorization will ensure that the app is connected again.
- Be aware that if you want to fetch activities you also have to request permission for 'calories' and 'distance'.
- In Android 6 and over, this function will also ask for some dynamic permissions if needed (e.g. in the case of "distance" or "activity", it will need access to ACCESS_FINE_LOCATION).

#### iOS quirks

- Once the user has allowed (or not allowed) the app, this function will not prompt the user again, but will call the callback immediately. See [this](https://developer.apple.com/documentation/healthkit/hkhealthstore/1614152-requestauthorization) for further explanation.
- The datatype `activity` also includes sleep. If you want to get authorization only for workouts, you can specify `workouts` as datatype, but be aware that this is only availabe in iOS.

### isAuthorized()

Check if the app has authorization to read/write a set of datatypes.

```
navigator.health.isAuthorized(datatypes, successCallback, errorCallback)
```

- datatypes: {type: Mixed array}, a list of data types you want to check access of, same as in requestAuthorization
- successCallback: {type: function(authorized)}, if the argument is true, the app is authorized
- errorCallback: {type: function(err)}, called if something went wrong, err contains a textual description of the problem

#### iOS quirks

- This method will only check authorization status for writeable data. Read-only data will always be considered as not authorized.
This is [an intended behaviour of HealthKit](https://developer.apple.com/reference/healthkit/hkhealthstore/1614154-authorizationstatus).


### disconnect() - Android only

Removes authorization from the app. Works only on Android.

```
navigator.health.disconnect(successCallback, errorCallback)
```

- successCallback: {type: function(disconnected)}, if the argument is true, the app has been disconnected from Google Fit
- errorCallback: {type: function(err)}, called if something went wrong, err contains a textual description of the problem


### query()

Gets all the data points of a certain data type within a certain time window.

**Warning:** if the time span is big, it can generate long arrays!

```
navigator.health.query({
  startDate: new Date(new Date().getTime() - 3 * 24 * 60 * 60 * 1000), // three days ago
  endDate: new Date(), // now
  dataType: 'height',
  limit: 1000
}, successCallback, errorCallback)
```

- startDate: {type: Date}, start date from which to get data
- endDate: {type: Date}, end data to which to get the data
- dataType: {type: String}, the data type to be queried (see above)
- limit: {type: integer}, optional, sets a maximum number of returned values
- successCallback: {type: function(data) }, called if all OK, data contains the result of the query in the form of an array of: { startDate: Date, endDate: Date, value: xxx, unit: 'xxx', sourceName: 'aaaa', sourceBundleId: 'bbbb' }
- errorCallback: {type: function(err)}, called if something went wrong, err contains a textual description of the problem

#### iOS quirks

- Limit is set to 1000 by default.
- Limit is not currently supported in workouts.
- Datapoints are ordered in an descending fashion (from newer to older). You can revert this behaviour by adding `ascending: true` to your query object.
- HealthKit does not calculate active and basal calories - these must be inputted from an app
- HealthKit does not detect specific activities - these must be inputted from an app
- Activities in HealthKit may include two extra fields: calories (kcal) and distance (m)
- When querying for nutrition, HealthKit only returns those stored as correlation. To be sure to get all stored quantities, it's better to query nutrients individually (e.g. MyFitnessPal doesn't store meals as correlations).
- nutrition.vitamin_a is given in micrograms. Automatic conversion to international units is not trivial and depends on the actual substance (see [here](https://dietarysupplementdatabase.usda.nih.gov/ingredient_calculator/help.php#q9)).
- When querying for activities, only events whose startDate and endDate are **both** in the query range will be returned.
- If you want to query for activity but only want workouts, you can specify the `workouts` datatype, but be aware that this will only be availabe in iOS.
- The blood glucose meal information is stored by the Health App as preprandial (before a meal) or postprandial (after a meal), which are mapped to 'before_meal' and 'after_meal'. These two specific values are only used in iOS and can't be used in Android apps.

#### Android quirks

- It is possible to query for "raw" steps or to select those as filtered by the Google Fit app. In the latter case the query object must contain the field `filtered: true`.
- calories.basal is returned as an average per day, and usually is not available in all days.
- calories.active is computed by subtracting the basal calories from the total. As basal energy expenditure, an average is computed from the week before endDate.
- Active and basal calories can be automatically calculated
- Some activities can be determined automatically (still, walking, running, biking, in vehicle)
- When querying for nutrition, Google Fit always returns all the nutrition elements it has.
- nutrition.vitamin_a is given in international units. Automatic conversion to micrograms is not trivial and depends on the actual substance (see [here](https://dietarysupplementdatabase.usda.nih.gov/ingredient_calculator/help.php#q9)).
- When querying for activities, if an event's startDate is out of the query range but its endDate is within, Google Fit will truncate the startDate to match that of the query.

### queryAggregated()

Gets aggregated data in a certain time window.
Usually the sum is returned for the given quantity.

```
navigator.health.queryAggregated({
  startDate: new Date(new Date().getTime() - 3 * 24 * 60 * 60 * 1000), // three days ago
  endDate: new Date(), // now
  dataType: 'steps',
  bucket: 'day'
}, successCallback, errorCallback)
```

- startDate: {type: Date}, start date from which to get data
- endDate: {type: Date}, end data to which to get the data
- dataType: {type: String}, the data type to be queried (see below for supported data types)
- bucket: {type: String}, if specified, aggregation is grouped an array of "buckets" (windows of time), supported values are: 'hour', 'day', 'week', 'month', 'year'
- successCallback: {type: function(data)}, called if all OK, data contains the result of the query, see below for returned data types. If no buckets is specified, the result is an object. If a bucketing strategy is specified, the result is an array.
- errorCallback: {type: function(err)}, called if something went wrong, err contains a textual description of the problem

Not all data types are supported for aggregated queries.
The following table shows what types are supported and examples of the returned object:

| Data type       | Example of returned object |
|-----------------|----------------------------|
| steps           | { startDate: Date, endDate: Date, value: 5780, unit: 'count' } |
| distance        | { startDate: Date, endDate: Date, value: 12500.0, unit: 'm' } |
| calories        | { startDate: Date, endDate: Date, value: 25698.1, unit: 'kcal' } |
| calories.active | { startDate: Date, endDate: Date, value: 3547.4, unit: 'kcal' } |
| calories.basal  | { startDate: Date, endDate: Date, value: 13146.1, unit: 'kcal' } |
| activity        | { startDate: Date, endDate: Date, value: { still: { duration: 520000, calories: 30, distance: 0 }, walking: { duration: 223000, calories: 20, distance: 15 }}, unit: 'activitySummary' }<br />**Note:** duration is expressed in milliseconds, distance in meters and calories in kcal |
| nutrition       | { startDate: Date, endDate: Date, value: { nutrition.fat.saturated: 11.5, nutrition.calories: 233.1 }, unit: 'nutrition' }<br />**Note:** units of measurement for nutrients are fixed according to the table at the beginning of this README |
| nutrition.x     | { startDate: Date, endDate: Date, value: 23, unit: 'mg'} |

#### Quirks

- The start and end dates returned are the date of the first and the last available samples. If no samples are found, start and end may not be set.
- When bucketing, buckets will include the whole hour / day / month / week / year where start and end times fall into. For example, if your start time is 2016-10-21 10:53:34, the first daily bucket will start at 2016-10-21 00:00:00.
- Weeks start on Monday.
- You can query for "filtered steps" adding the flag `filtered: true` to the query object. This returns the steps as filtered out by Google Fit, or the non-manual ones from HealthKit.

#### iOS quirks

- Activities in HealthKit may include two extra fields: calories (kcal) and distance (m)
- When querying for nutrition, HealthKit only returns those stored as correlation. To be sure to get all stored quantities, it's better to query nutrients individually (e.g. MyFitnessPal doesn't store meals as correlations).
- nutrition.vitamin_a is given in micrograms. Automatic conversion to international units is not trivial and depends on the actual substance (see [here](https://dietarysupplementdatabase.usda.nih.gov/ingredient_calculator/help.php#q9)).

#### Android quirks
- Activities will include two extra fields: calories (kcal) and distance (m) and requires the user to grant access to location
- nutrition.vitamin_a is given in international units. Automatic conversion to micrograms is not trivial and depends on the actual substance (see [here](https://dietarysupplementdatabase.usda.nih.gov/ingredient_calculator/help.php#q9)).

### store()

Stores a data point.

```
navigator.health.store({
	startDate:  new Date(new Date().getTime() - 3 * 60 * 1000), // three minutes ago
	endDate: new Date(),
	dataType: 'steps',
	value: 180,
	sourceBundleId: 'com.example.my_app'
}, successCallback, errorCallback)
```

- startDate: {type: Date}, start date from which the new data starts
- endDate: {type: Date}, end date to which he new data ends
- dataType: {type: a String}, the data type
- value: {type: a number or an Object}, the value, depending on the actual data type. In the case of activity, the value must be set as the activity name.
- sourceBundleId: {type: String}, the complete package of the source that produced this data. In Android, if not specified, it's assigned to the package of the App. In iOS this is ignored and set automatically to the bundle id of the app.
- successCallback: {type: function}, called if all OK
- errorCallback: {type: function(err)}, called if something went wrong, err contains a textual description of the problem

#### iOS quirks

- When storing an activity, you can also specify calories (active, in kcal) and distance (walked or run, in meters). For example: `dataType: 'activity', value: 'walking', calories: 20, distance: 520`. Be aware, though, that you need permission to write calories and distance first, or the call will fail.
- In iOS you cannot store the total calories, you need to specify either basal or active. If you use total calories, the active ones will be stored.
- In iOS distance is assumed to be of type WalkingRunning, if you want to explicitly set it to Cycling you need to add the field `cycling: true`.
- The blood glucose meal information can be stored as 'before_meal' and 'after_meal', but these two can't be used in Android apps.

#### Android quirks

- Google Fit doesn't allow you to overwrite data points that overlap with others already stored of the same type (see [here](https://developers.google.com/fit/android/history#manageConflicting)). At the moment there is no support for [update](https://developers.google.com/fit/android/history#updateData).
- Storing of nutrients is not supported at the moment in Android.
- In Android you can only store active calories, as the basal are estimated automatically. If you store total calories, these will be treated as active.

### delete()

Deletes a range of data points.

```
navigator.health.delete({
	startDate:  new Date(new Date().getTime() - 3 * 60 * 1000), // three minutes ago
	endDate: new Date(),
	dataType: 'steps'
}, successCallback, errorCallback)
```

- startDate: {type: Date}, start date from which to delete data
- endDate: {type: Date}, end date to which to delete the data
- dataType: {type: a String}, the data type to be deleted
- successCallback: {type: function}, called if all OK
- errorCallback: {type: function(err)}, called if something went wrong, err contains a textual description of the problem

#### iOS quirks

- You cannot delete the total calories, you need to specify either basal or active. If you use total calories, the active ones will be delete.
- Distance is assumed to be of type WalkingRunning, if you want to explicitly set it to Cycling you need to add the field `cycling: true`.
- Deleting sleep activities is not supported at the moment.

#### Android quirks

- Google Fit doesn't allow you to delete data points that were generated by other apps
- You can only delete active calories, as the basal are estimated automatically. If you try to delete total calories, these will be treated as active.

## Differences between HealthKit and Google Fit

* HealthKit includes medical data (e.g. blood glucose), whereas Google Fit is mainly meant for fitness data (although [now supports some medical data too](https://developers.google.com/android/reference/com/google/android/gms/fitness/data/HealthDataTypes)).
* HealthKit provides a data model that is not extensible, while Google Fit allows defining custom data types.
* HealthKit allows to insert data with the unit of measurement of your choice, and automatically translates units when queried, whereas Google Fit uses fixed units of measurement.
* HealthKit automatically counts steps and distance when you carry your phone with you and if your phone has the CoreMotion chip. Google Fit does it independently on the HW chip and also detects the kind of activity (sedentary, running, walking, cycling, in vehicle).
* HealthKit can only compute distance for running/walking activities, while Google Fit can also do so for bicycle events.

## External resources

* The official Apple documentation for HealthKit [can be found here](https://developer.apple.com/library/ios/documentation/HealthKit/Reference/HealthKit_Framework/index.html#//apple_ref/doc/uid/TP40014707).
* For functions that require the `unit` attribute, you can find the comprehensive list of possible units from the [Apple Developers documentation](https://developer.apple.com/library/ios/documentation/HealthKit/Reference/HKUnit_Class/index.html#//apple_ref/doc/uid/TP40014727-CH1-SW2).
* [HealthKit constants](https://developer.apple.com/library/ios/documentation/HealthKit/Reference/HealthKit_Constants/index.html), used throughout the code.
* Google Fit [supported data types](https://developers.google.com/fit/android/data-types).

## Roadmap

Short term:

- Add more datatypes (body fat percentage, oxygen saturation, temperature, respiratory rate)

Long term:

- Include [Core Motion Activity API](https://developer.apple.com/reference/coremotion/cmmotionactivitymanager)
- Add registration to updates (e.g. in Google Fit:  HistoryApi#registerDataUpdateListener()).
- Add support for Samsung Health as an alternate health record for Android.

## Contributions

Any help is more than welcome!

I don't know Objective C and I am not interested in learning it now, so I would particularly appreciate someone who could give me a hand with the iOS part.
Also, I would love to know from you if the plugin is currently used in any app actually available online.
Just send me an email to my_username at gmail.com.
For donations, you can use my [monzo.me](https://monzo.me/dariosalvi) account.

Thanks!

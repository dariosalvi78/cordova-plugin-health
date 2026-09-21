var Health = function () {
  this.name = 'health';
};

var dataTypes = [];
dataTypes['mindfulness'] = 'HKCategoryTypeIdentifierMindfulSession';
dataTypes['steps'] = 'HKQuantityTypeIdentifierStepCount';
dataTypes['stairs'] = 'HKQuantityTypeIdentifierFlightsClimbed';
dataTypes['distance'] = 'HKQuantityTypeIdentifierDistanceWalkingRunning'; // and HKQuantityTypeIdentifierDistanceCycling
dataTypes['calories'] = 'HKQuantityTypeIdentifierActiveEnergyBurned'; // and HKQuantityTypeIdentifierBasalEnergyBurned
dataTypes['calories.active'] = 'HKQuantityTypeIdentifierActiveEnergyBurned';
dataTypes['calories.basal'] = 'HKQuantityTypeIdentifierBasalEnergyBurned';
dataTypes['height'] = 'HKQuantityTypeIdentifierHeight';
dataTypes['weight'] = 'HKQuantityTypeIdentifierBodyMass';
dataTypes['bmi'] = 'HKQuantityTypeIdentifierBodyMassIndex';
dataTypes['heart_rate'] = 'HKQuantityTypeIdentifierHeartRate';
dataTypes['workout_route'] = 'HKWorkoutRouteType';
dataTypes['heart_rate.resting'] = 'HKQuantityTypeIdentifierRestingHeartRate';
dataTypes['heart_rate.variability'] = 'HKQuantityTypeIdentifierHeartRateVariabilitySDNN';
dataTypes['fat_percentage'] = 'HKQuantityTypeIdentifierBodyFatPercentage';
dataTypes['waist_circumference'] = 'HKQuantityTypeIdentifierWaistCircumference';
dataTypes['activity'] = 'HKWorkoutTypeIdentifier';
dataTypes['sleep'] = 'HKCategoryTypeIdentifierSleepAnalysis';
dataTypes['nutrition'] = 'HKCorrelationTypeIdentifierFood';
dataTypes['nutrition.calories'] = 'HKQuantityTypeIdentifierDietaryEnergyConsumed';
dataTypes['nutrition.fat.total'] = 'HKQuantityTypeIdentifierDietaryFatTotal';
dataTypes['nutrition.fat.saturated'] = 'HKQuantityTypeIdentifierDietaryFatSaturated';
dataTypes['nutrition.fat.polyunsaturated'] = 'HKQuantityTypeIdentifierDietaryFatPolyunsaturated';
dataTypes['nutrition.fat.monounsaturated'] = 'HKQuantityTypeIdentifierDietaryFatMonounsaturated';
dataTypes['nutrition.cholesterol'] = 'HKQuantityTypeIdentifierDietaryCholesterol';
dataTypes['nutrition.sodium'] = 'HKQuantityTypeIdentifierDietarySodium';
dataTypes['nutrition.potassium'] = 'HKQuantityTypeIdentifierDietaryPotassium';
dataTypes['nutrition.carbs.total'] = 'HKQuantityTypeIdentifierDietaryCarbohydrates';
dataTypes['nutrition.dietary_fiber'] = 'HKQuantityTypeIdentifierDietaryFiber';
dataTypes['nutrition.sugar'] = 'HKQuantityTypeIdentifierDietarySugar';
dataTypes['nutrition.protein'] = 'HKQuantityTypeIdentifierDietaryProtein';
dataTypes['nutrition.vitamin_a'] = 'HKQuantityTypeIdentifierDietaryVitaminA';
dataTypes['nutrition.vitamin_c'] = 'HKQuantityTypeIdentifierDietaryVitaminC';
dataTypes['nutrition.calcium'] = 'HKQuantityTypeIdentifierDietaryCalcium';
dataTypes['nutrition.iron'] = 'HKQuantityTypeIdentifierDietaryIron';
dataTypes['nutrition.water'] = 'HKQuantityTypeIdentifierDietaryWater';
dataTypes['nutrition.caffeine'] = 'HKQuantityTypeIdentifierDietaryCaffeine';
dataTypes['blood_glucose'] = 'HKQuantityTypeIdentifierBloodGlucose';
dataTypes['insulin'] = 'HKQuantityTypeIdentifierInsulinDelivery';
dataTypes['appleExerciseTime'] = 'HKQuantityTypeIdentifierAppleExerciseTime';
dataTypes['blood_pressure'] = 'HKCorrelationTypeIdentifierBloodPressure'; // when requesting auth it's HKQuantityTypeIdentifierBloodPressureSystolic and HKQuantityTypeIdentifierBloodPressureDiastolic
dataTypes['blood_pressure_systolic'] = 'HKQuantityTypeIdentifierBloodPressureSystolic';
dataTypes['blood_pressure_diastolic'] = 'HKQuantityTypeIdentifierBloodPressureDiastolic';
dataTypes['resp_rate'] = 'HKQuantityTypeIdentifierRespiratoryRate';
dataTypes['oxygen_saturation'] = 'HKQuantityTypeIdentifierOxygenSaturation';
dataTypes['vo2max'] = 'HKQuantityTypeIdentifierVO2Max';
dataTypes['basal_body_temperature'] = 'HKQuantityTypeIdentifierBasalBodyTemperature';
dataTypes['core_body_temperature'] = 'HKQuantityTypeIdentifierBodyTemperature';
dataTypes['apple_sleep_skin_temperature'] = 'HKQuantityTypeIdentifierAppleSleepingWristTemperature';
dataTypes['appleSleepingBreathingDisturbances'] = 'HKQuantityTypeIdentifierAppleSleepingBreathingDisturbances';
dataTypes['UVexposure'] = 'HKQuantityTypeIdentifierUVExposure';
dataTypes['menstruation_flow'] = 'HKCategoryTypeIdentifierMenstrualFlow';

// for parseable units in HK, see https://developer.apple.com/documentation/healthkit/hkunit/1615733-unitfromstring?language=objc
var units = [];
units['steps'] = 'count';
units['distance'] = 'm';
units['calories'] = 'kcal';
units['calories.active'] = 'kcal';
units['calories.basal'] = 'kcal';
units['height'] = 'm';
units['weight'] = 'kg';
units['bmi'] = 'count';
units['heart_rate'] = 'count/min';
units['heart_rate.resting'] = 'count/min';
units['heart_rate.variability'] = 'ms';
units['fat_percentage'] = '%';
units['waist_circumference'] = 'm';
units['nutrition'] = ['g', 'ml', 'kcal'];
units['nutrition.calories'] = 'kcal';
units['nutrition.fat.total'] = 'g';
units['nutrition.fat.saturated'] = 'g';
units['nutrition.fat.polyunsaturated'] = 'g';
units['nutrition.fat.monounsaturated'] = 'g';
units['nutrition.cholesterol'] = 'mg';
units['nutrition.sodium'] = 'mg';
units['nutrition.potassium'] = 'mg';
units['nutrition.carbs.total'] = 'g';
units['nutrition.dietary_fiber'] = 'g';
units['nutrition.sugar'] = 'g';
units['nutrition.protein'] = 'g';
units['nutrition.vitamin_a'] = 'mcg';
units['nutrition.vitamin_c'] = 'mg';
units['nutrition.calcium'] = 'mg';
units['nutrition.iron'] = 'mg';
units['nutrition.water'] = 'ml';
units['nutrition.caffeine'] = 'g';
units['blood_glucose'] = 'mmol/L';
units['insulin'] = 'IU';
units['appleExerciseTime'] = 'min';
units['blood_pressure'] = 'mmHg';
units['blood_pressure_systolic'] = 'mmHg';
units['blood_pressure_diastolic'] = 'mmHg';
units['resp_rate'] = 'count/min';
units['oxygen_saturation'] = '%';
units['vo2max'] = 'ml/(kg*min)';
units['basal_body_temperature'] = 'degC';
units['core_body_temperature'] = 'degC';
units['apple_sleep_skin_temperature'] = 'degC';
units['appleSleepingBreathingDisturbances'] = 'count';
units['UVexposure'] = 'count';

// just a wrapper for querying Telerik's if HK is available
Health.prototype.isAvailable = function (success, error) {
  window.plugins.healthkit.available(success, error);
};


Health.prototype.openHealthSettings = function (success, error) {
  window.plugins.healthkit.openHealthSettings(success, error);
};

// returns the equivalent native HealthKit data type from the custom one
var getHKDataTypes = function (dtArr) {
  var HKDataTypes = [];
  for (var i = 0; i < dtArr.length; i++) {
    if ((dtArr[i] !== 'gender') && (dtArr[i] !== 'date_of_birth')) { // ignore gender and DOB
      if (dtArr[i] === 'nutrition') {
        // add all nutrition stuff
        for (var dataType in dataTypes) {
          if (dataType.startsWith('nutrition.')) HKDataTypes.push(dataTypes[dataType]);
        }
      } else if (dtArr[i] === 'blood_pressure') {
        HKDataTypes.push('HKQuantityTypeIdentifierBloodPressureSystolic');
        HKDataTypes.push('HKQuantityTypeIdentifierBloodPressureDiastolic');
      } else if (dataTypes[dtArr[i]]) {
        HKDataTypes.push(dataTypes[dtArr[i]]);
        if (dtArr[i] === 'distance') HKDataTypes.push('HKQuantityTypeIdentifierDistanceCycling');
        if (dtArr[i] === 'calories') HKDataTypes.push('HKQuantityTypeIdentifierBasalEnergyBurned');
      } else {
        // return the not found dataType instead of array
        return dtArr[i];
      }
    }
  }
  return HKDataTypes;
};

var getReadWriteTypes = function (dts, success, error) {
  let readTypes = [];
  let writeTypes = [];
  let HKDataTypes = [];
  if (dts.read) {
    HKDataTypes = getHKDataTypes(dts.read);
    if (Array.isArray(HKDataTypes)) {
      readTypes = readTypes.concat(HKDataTypes);
    } else {
      error('unknown read data type - ' + HKDataTypes);
      return;
    }
  }
  if (dts.write) {
    HKDataTypes = getHKDataTypes(dts.write);
    if (Array.isArray(HKDataTypes)) {
      writeTypes = writeTypes.concat(HKDataTypes);
    } else {
      error('unknown write data type - ' + HKDataTypes);
      return;
    }
  }
  success(dedupe(readTypes), dedupe(writeTypes));
};

// requests authorization to HK, a wrapper on top of Telerik's plugin
Health.prototype.requestAuthorization = function (dts, onSuccess, onError) {
  getReadWriteTypes(dts, function (readTypes, writeTypes) {
    window.plugins.healthkit.requestAuthorization({
      'readTypes': readTypes,
      'writeTypes': writeTypes
    }, onSuccess, onError);
  }, onError);
};

// checks if a datatype has been authorized
Health.prototype.isAuthorized = function (dts, onSuccess, onError) {
  getReadWriteTypes(dts, function (readTypes, writeTypes) {
    var HKDataTypes = dedupe(readTypes.concat(writeTypes));
    var check = function () {
      if (HKDataTypes.length > 0) {
        var dt = HKDataTypes.shift();
        window.plugins.healthkit.checkAuthStatus({
          type: dt
        }, function (auth) {
          if (auth === 'authorized') check();
          else onSuccess(false);
        }, onError);
      } else onSuccess(true);
    };
    check();
  }, onError);
};

let addWorkoutRoutesAndHeartRate = async function addWorkoutRoutesAndHeartRate(opts, workouts){
  for(let i= 0; i < workouts.length; i++){
   if(opts.includeRoute) {
     try {
       console.log('Querying route for workout', workouts[i].id);
       let route = await new Promise((resolve, reject) => {
         window.plugins.healthkit.queryWorkoutRoutes(workouts[i].id, resolve, reject);
       });
      console.log('Route for workout', workouts[i].id, route);
       route.forEach((point) => {
         point.timestamp = new Date(point.timestamp);
       });

       workouts[i].route = route;
     } catch (e) {
       console.log('Error querying route during workout', e);
     }
   }
   
   if(opts.includeHeartRate) {
     try {
       let heartRate = await new Promise((resolve, reject) => {
         window.plugins.healthkit.queryHeartRateDuringWorkout(workouts[i].id, resolve, reject);
       });

       heartRate.forEach((point) => {
         point.timestamp = new Date(point.timestamp);
       });

       workouts[i].heartRate = heartRate;
     } catch (e) {
       console.log('Error querying heart rate during workout', e);
     }
    }
  }
}

// queries for a datatype
Health.prototype.query = function (opts, onSuccess, onError) {
  var startD = opts.startDate;
  var endD = opts.endDate;

  if (opts.dataType === 'gender') {
    window.plugins.healthkit.readGender(function (data) {
      var res = [];
      res[0] = {
        startDate: startD,
        endDate: endD,
        value: data,
        sourceName: 'Health',
        sourceBundleId: 'com.apple.Health'
      };
      onSuccess(res);
    }, onError);
  } else if (opts.dataType === 'date_of_birth') {
    window.plugins.healthkit.readDateOfBirth(function (data) {
      data.startDate = startD;
      data.endDate = endD;
      var res = [];
      var date = new Date(data);
      res[0] = {
        startDate: opts.startDate,
        endDate: opts.endDate,
        value: { day: date.getDate(), month: date.getMonth() + 1, year: date.getFullYear() },
        sourceName: 'Health',
        sourceBundleId: 'com.apple.Health'
      };
      onSuccess(res);
    }, onError);
  } else if (opts.dataType === 'activity') {
    // opts is not really used, Telerik's plugin just returns ALL workouts
    window.plugins.healthkit.findWorkouts(opts, function (data) {
      let result = [];
      for (let i = 0; i < data.length; i++) {
        let res = {};
        res.id = data[i].UUID
        res.startDate = new Date(data[i].startDate);
        res.endDate = new Date(data[i].endDate);
        // filter the results based on the dates
        if ((res.startDate >= opts.startDate) && (res.endDate <= opts.endDate)) {
          res.value = data[i].activityType;
          res.unit = 'activityType';
          if (data[i].energy) res.calories = parseInt(data[i].energy);
          if (data[i].distance) res.distance = parseInt(data[i].distance);
          if (data[i].duration) res.duration = data[i].duration;
          res.sourceName = data[i].sourceName;
          res.sourceBundleId = data[i].sourceBundleId;
          result.push(res);
        }
      }
      
      if(opts.includeRoute || opts.includeHeartRate) {
        addWorkoutRoutesAndHeartRate(opts, result)
            .then(() => onSuccess(result), onError);
      } else {
        onSuccess(result);
      }
    }, onError);
  } else if (opts.dataType === 'nutrition' || opts.dataType === 'blood_pressure') {
    // do the correlation queries
    var result = [];
    var qops = { // query-specific options
      startDate: opts.startDate,
      endDate: opts.endDate,
      correlationType: dataTypes[opts.dataType]
    }
    if (units[opts.dataType].constructor.name == "Array") qops.units = units[opts.dataType];
    else qops.units = [units[opts.dataType]];

    window.plugins.healthkit.queryCorrelationType(qops, function (data) {
      for (var i = 0; i < data.length; i++) {
        result.push(prepareCorrelation(data[i], opts.dataType));
      }
      onSuccess(result);
    }, onError);
  } else if (dataTypes[opts.dataType]) {
    opts.sampleType = dataTypes[opts.dataType];
    if (units[opts.dataType]) {
      opts.unit = units[opts.dataType];
    }
    window.plugins.healthkit.querySampleType(opts, function (data) {
      let result = [];
      let convertSamples = function (samples) {
        for (let i = 0; i < samples.length; i++) {
          let res = {};
          res.id = samples[i].UUID
          res.startDate = new Date(samples[i].startDate);
          res.endDate = new Date(samples[i].endDate);
          if (opts.dataType === 'blood_glucose') {
            res.value = {
              glucose: samples[i].quantity
            }
            if (samples[i].metadata && samples[i].metadata.HKBloodGlucoseMealTime) {
              if (samples[i].metadata.HKBloodGlucoseMealTime == 1) res.value.meal = 'before_meal'
              else res.value.meal = 'after_meal'
            }
            if (samples[i].metadata && samples[i].metadata.HKMetadataKeyBloodGlucoseMealTime) res.value.meal = samples[i].metadata.HKMetadataKeyBloodGlucoseMealTime; // overwrite HKBloodGlucoseMealTime
            if (samples[i].metadata && samples[i].metadata.HKMetadataKeyBloodGlucoseSleepTime) res.value.sleep = samples[i].metadata.HKMetadataKeyBloodGlucoseSleepTime;
            if (samples[i].metadata && samples[i].metadata.HKMetadataKeyBloodGlucoseSource) res.value.source = samples[i].metadata.HKMetadataKeyBloodGlucoseSource;
          } else if (opts.dataType === 'insulin') {
            res.value = {
              insulin: samples[i].quantity
            }
            if (samples[i].metadata && samples[i].metadata.HKInsulinDeliveryReason) {
              if (samples[i].metadata.HKInsulinDeliveryReason == 1) res.value.reason = 'basal'
              else res.value.reason = 'bolus'
            }
            if (samples[i].metadata && samples[i].metadata.HKMetadataKeyInsulinDeliveryReason) res.value.reason = samples[i].metadata.HKMetadataKeyInsulinDeliveryReason; // overwrite HKInsulinDeliveryReason
          } else if (opts.dataType === 'sleep') {
            switch (data[i].value) {
              case 0:
                res.value = 'sleep.inBed';
                break;
              case 1:
              default:
                res.value = 'sleep';
                break;
              case 2:
                res.value = 'sleep.awake';
                break;
              case 3:
                res.value = 'sleep.light';
                break;
              case 4:
                res.value = 'sleep.deep';
                break;
              case 5:
                res.value = 'sleep.rem';
                break;
            }
            res.unit = 'sleep';
          } else if (opts.dataType === 'menstruation_flow') {
            let isFirst = samples[i].metadata.HKMetadataKeyMenstrualCycleStart;
            res.followsFlowInPeriod = !isFirst;

            switch (samples[i].value) {
              case 1:
                res.value = 'unspecified';
                break;
              case 2:
                res.value = 'light';
                break;
              case 3:
                res.value = 'medium';
                break;
              case 4:
                res.value = 'heavy';
                break;
              default:
                  res.value = 'unspecified';
            }
            res.unit = 'flow';
          } else {
            res.value = samples[i].quantity;
          }
          if (samples[i].unit) res.unit = samples[i].unit;
          else if (opts.unit) res.unit = opts.unit;
          res.sourceName = samples[i].sourceName;
          res.sourceBundleId = samples[i].sourceBundleId;
          result.push(res);
        }
      };
      convertSamples(data);
      if (opts.dataType === 'distance') { // in the case of the distance, add the cycling distances
        opts.sampleType = 'HKQuantityTypeIdentifierDistanceCycling';
        // re-assign start and end times (because the plugin modifies them later)
        opts.startDate = startD;
        opts.endDate = endD;
        window.plugins.healthkit.querySampleType(opts, function (data) {
          convertSamples(data);
          onSuccess(result);
        }, onError);
      } else if (opts.dataType === 'calories') { // in the case of the calories, add the basal
        opts.sampleType = 'HKQuantityTypeIdentifierBasalEnergyBurned';
        opts.startDate = startD;
        opts.endDate = endD;
        window.plugins.healthkit.querySampleType(opts, function (data) {
          convertSamples(data);
          onSuccess(result);
        }, onError);
      } else onSuccess(result);
    }, onError); // first call to querySampleType
  } else {
    onError('unknown data type ' + opts.dataType);
  }
};

Health.prototype.queryAggregated = function (opts, onSuccess, onError) {
  if ((opts.dataType !== 'steps') && (opts.dataType !== 'distance') &&
    (opts.dataType !== 'calories') && (opts.dataType !== 'calories.active') &&
    (opts.dataType !== 'calories.basal') && (opts.dataType !== 'activity') &&
    (opts.dataType !== 'heart_rate') && (opts.dataType !== 'heart_rate.resting') &&
    (opts.dataType !== 'heart_rate.variability') && (opts.dataType !== 'weight') &&
    (!opts.dataType.startsWith('nutrition')) && (opts.dataType !== 'appleExerciseTime')) {
    // unsupported datatype
    onError('Datatype ' + opts.dataType + ' not supported in queryAggregated');
    return;
  }
  var startD = opts.startDate;
  var endD = opts.endDate;
  opts.sampleType = dataTypes[opts.dataType];
  if (units[opts.dataType]) opts.unit = units[opts.dataType];
  if (opts.bucket) {
    // ----- with buckets
    opts.aggregation = opts.bucket;
    if (opts.dataType === 'activity') {
      // query and manually aggregate
      cordova.plugins.health.query(opts, function (data) {
        onSuccess(bucketize(data, opts.bucket, startD, endD, 'activitySummary', mergeActivitySamples));
      }, onError);
    } else if (opts.dataType === 'nutrition') {
      // query and manually aggregate
      cordova.plugins.health.query(opts, function (data) {
        onSuccess(bucketize(data, opts.bucket, startD, endD, 'nutrition', mergeNutritionSamples));
      }, onError);
    } else {
      window.plugins.healthkit.querySampleTypeAggregated(opts, function (value) {
        if (opts.dataType === 'distance') {
          // add cycled distance
          let rundists = value;
          opts.sampleType = 'HKQuantityTypeIdentifierDistanceCycling';
          opts.startDate = startD;
          opts.endDate = endD;
          window.plugins.healthkit.querySampleTypeAggregated(opts, function (dists) {
            onSuccess(prepareResults(rundists, opts.unit, dists));
          }, onError);
        } else if (opts.dataType === 'calories') {
          // add basal calories
          let activecals = value;
          opts.sampleType = 'HKQuantityTypeIdentifierBasalEnergyBurned';
          opts.startDate = startD;
          opts.endDate = endD;
          window.plugins.healthkit.querySampleTypeAggregated(opts, function (cals) {
            onSuccess(prepareResults(activecals, opts.unit, cals));
          }, onError);
        } else {
          //simply refactor the result and send it
          onSuccess(prepareResults(value, opts.unit));
        }
      }, onError);
    }
  } else {
    // ---- no bucketing, just sum
    if (opts.dataType === 'activity') {
      cordova.plugins.health.query(opts, function (data) {
        // manually aggregate by activity
        onSuccess(aggregateIntoResult(data, 'activitySummary', mergeActivitySamples));
      }, onError);
    } else if (opts.dataType === 'nutrition') {
      // manually aggregate by nutrition
      cordova.plugins.health.query(opts, function (data) {
        onSuccess(aggregateIntoResult(data, 'nutrition', mergeNutritionSamples));
      }, onError);
    } else {
      window.plugins.healthkit.sumQuantityType(opts, function (value) {
        if (opts.dataType === 'distance') {
          // add cycled distance
          let dist = value;
          opts.sampleType = 'HKQuantityTypeIdentifierDistanceCycling';
          opts.startDate = startD;
          opts.endDate = endD;
          window.plugins.healthkit.sumQuantityType(opts, function (value) {
            onSuccess({
              startDate: startD,
              endDate: endD,
              value: value + dist,
              unit: opts.unit
            });
          }, onError);
        } else if (opts.dataType === 'calories') {
          // add basal calories
          let activecals = value;
          opts.sampleType = 'HKQuantityTypeIdentifierBasalEnergyBurned';
          opts.startDate = startD;
          opts.endDate = endD;
          window.plugins.healthkit.sumQuantityType(opts, function (basalcals) {
            onSuccess({
              startDate: startD,
              endDate: endD,
              value: basalcals + activecals,
              unit: opts.unit
            });
          }, onError);
        } else {
          onSuccess({
            startDate: startD,
            endDate: endD,
            value: value,
            unit: opts.unit
          });
        }
      }, onError);
    }
  }
};

Health.prototype.store = function (data, onSuccess, onError) {
  if (data.dataType === 'gender') {
    onError('Gender is not writeable');
  } else if (data.dataType === 'date_of_birth') {
    onError('Date of birth is not writeable');
  } else if (data.dataType === 'activity') {
    // activity / workouts are treated separately
    data.activityType = data.value;
    data.requestReadPermission = false // do not ask for read permission too
    if (data.calories) {
      data.energy = data.calories;
      data.energyUnit = 'kcal';
    }
    if (data.distance) {
      data.distance = data.distance;
      data.distanceUnit = 'm';
    }
    window.plugins.healthkit.saveWorkout(data, onSuccess, onError);
  } else if (data.dataType === 'sleep') {
    // sleep needs to be mapped to a specific value
    data.sampleType = 'HKCategoryTypeIdentifierSleepAnalysis';

    if (data.value === 'sleep') {
      if (getOSVersion() >= 16) {
        data.value = 'HKCategoryValueSleepAnalysisAsleepUnspecified';
      } else {
        data.value = 'HKCategoryValueSleepAnalysisAsleep';
      }
    } else if (data.value === 'sleep.inBed') {
      data.value = 'HKCategoryValueSleepAnalysisInBed';
    } else if (data.value === 'sleep.light') {
      data.value = 'HKCategoryValueSleepAnalysisAsleepCore';
    } else if (data.value === 'sleep.deep') {
      data.value = 'HKCategoryValueSleepAnalysisAsleepDeep';
    } else if (data.value === 'sleep.rem') {
      data.value = 'HKCategoryValueSleepAnalysisAsleepREM';
    } else if (data.value === 'sleep.awake') {
      data.value = 'HKCategoryValueSleepAnalysisAwake';
    } else if (data.value === 'sleep.outOfBed') {
      data.value = 'HKCategoryValueSleepAnalysisAwake';
    }
    window.plugins.healthkit.saveSample(data, onSuccess, onError);

  } else if (data.dataType === 'nutrition') {
    data.correlationType = 'HKCorrelationTypeIdentifierFood';
    if (!data.metadata) data.metadata = {};
    if (data.value.item) data.metadata.HKFoodType = data.value.item;
    if (data.value.meal_type) data.metadata.HKFoodMeal = data.value.meal_type;
    if (data.value.brand_name) data.metadata.HKFoodBrandName = data.value.brand_name;
    data.samples = [];
    for (let nutrientName in data.value.nutrients) {
      let unit = units[nutrientName];
      let sampletype = dataTypes[nutrientName];
      if (!sampletype) {
        onError('Cannot recognise nutrition item ' + nutrientName);
        return;
      }
      let sample = {
        'startDate': data.startDate,
        'endDate': data.endDate,
        'sampleType': sampletype,
        'unit': unit,
        'amount': data.value.nutrients[nutrientName]
      };
      data.samples.push(sample)
    }
    window.plugins.healthkit.saveCorrelation(data, onSuccess, onError);
  } else if (data.dataType === 'blood_pressure') {
    data.correlationType = 'HKCorrelationTypeIdentifierBloodPressure';
    data.samples = [{
      'startDate': data.startDate,
      'endDate': data.endDate,
      'sampleType': 'HKQuantityTypeIdentifierBloodPressureSystolic',
      'unit': 'mmHg',
      'amount': data.value.systolic
    }, {
      'startDate': data.startDate,
      'endDate': data.endDate,
      'sampleType': 'HKQuantityTypeIdentifierBloodPressureDiastolic',
      'unit': 'mmHg',
      'amount': data.value.diastolic
    }];
    window.plugins.healthkit.saveCorrelation(data, onSuccess, onError);
  } else if (data.dataType === 'menstruation_flow') {
    data.sampleType = 'HKCategoryTypeIdentifierMenstrualFlow';

    if (data.value === 'unspecified') {
      data.value = 'HKCategoryValueMenstrualFlowUnspecified';
    } else if (data.value === 'light') {
      data.value = 'HKCategoryValueMenstrualFlowLight';
    } else if (data.value === 'medium') {
      data.value = 'HKCategoryValueMenstrualFlowMedium';
    } else if (data.value === 'heavy') {
      data.value = 'HKCategoryValueMenstrualFlowHeavy';
    } else if (data.value === 'none') {
      data.value = 'HKCategoryValueMenstrualFlowNone';
    }

    if (!data.metadata) data.metadata = {};

    let isFirstInPeriod = true;
    if (data.followsFlowInPeriod) {
      isFirstInPeriod = false;
    }
    data.metadata.HKMetadataKeyMenstrualCycleStart = isFirstInPeriod;

    window.plugins.healthkit.saveSample(data, onSuccess, onError);

  } else if (dataTypes[data.dataType]) {
    // generic case
    data.sampleType = dataTypes[data.dataType];
    if ((data.dataType === 'distance') && data.cycling) {
      data.sampleType = 'HKQuantityTypeIdentifierDistanceCycling';
    }
    if (data.dataType === 'blood_glucose') {
      data.amount = data.value.glucose;
      if (!data.metadata) data.metadata = {};
      if (data.value.meal) {
        data.metadata.HKMetadataKeyBloodGlucoseMealTime = data.value.meal;
        if (data.value.meal.startsWith('before_')) data.metadata.HKBloodGlucoseMealTime = 1;
        else if (data.value.meal.startsWith('after_')) data.metadata.HKBloodGlucoseMealTime = 2;
      }
      if (data.value.sleep) data.metadata.HKMetadataKeyBloodGlucoseSleepTime = data.value.sleep;
      if (data.value.source) data.metadata.HKMetadataKeyBloodGlucoseSource = data.value.source;
    } else if (data.dataType === 'insulin') {
      data.amount = data.value.insulin;
      if (!data.metadata) data.metadata = {};
      if (data.value.reason) {
        data.metadata.HKMetadataKeyInsulinDeliveryReason = data.value.reason;
        if (data.value.reason.toLowerCase() === 'basal') data.metadata.HKInsulinDeliveryReason = 1;
        else if (data.value.reason.toLowerCase() === 'bolus') data.metadata.HKInsulinDeliveryReason = 2;
      }
    } else {
      data.amount = data.value;
    }
    if (units[data.dataType]) {
      data.unit = units[data.dataType];
    }
    window.plugins.healthkit.saveSample(data, onSuccess, onError);
  } else {
    onError('unknown data type ' + data.dataType);
  }
};


Health.prototype.delete = function (data, onSuccess, onError) {
  if (data.dataType === 'gender') {
    onError('Gender is not deletable');
  } else if (data.dataType === 'date_of_birth') {
    onError('Date of birth is not deletable');
  } else if (data.dataType === 'activity') {
    data.sampleType = 'workoutType';
  } else if ((data.dataType === 'distance') && data.cycling) {
    data.sampleType = 'HKQuantityTypeIdentifierDistanceCycling';
  } else if (data.dataType === 'menstruation_flow') {
    data.sampleType = 'HKCategoryTypeIdentifierMenstrualFlow';
  } else if (dataTypes[data.dataType]) {
    data.sampleType = dataTypes[data.dataType];
  } else {
    onError('unknown data type ' + data.dataType);
    return;
  }
  window.plugins.healthkit.deleteSamples(data, onSuccess, onError);
};

cordova.addConstructor(function () {
  cordova.plugins.health = new Health();
  return cordova.plugins.health;
});


// UTILITY functions

// get iOS version number
getOSVersion = function () {
  var agent = window.navigator.userAgent,
    start = agent.indexOf('OS ');
  if ((agent.indexOf('iPhone') > -1 || agent.indexOf('iPad') > -1) && start > -1) {
    return window.Number(agent.substr(start + 3, 3).replace('_', '.'));
  }
  return 0;
}

// shallow removal of duplicates in an array
var dedupe = function (arr) {
  return arr.filter(function (el, i, arr) {
    return arr.indexOf(el) === i;
  });
};

// converts from grams into another unit
// if the unit is not specified or is not weight, then the original quantity is returned
var convertFromGrams = function (toUnit, q) {
  if (toUnit === 'mcg') return q * 1000000;
  if (toUnit === 'mg') return q * 1000;
  if (toUnit === 'kg') return q / 1000;
  return q;
}

// converts to grams from another unit
var convertToGrams = function (fromUnit, q) {
  if (fromUnit === 'mcg') return q / 1000000;
  if (fromUnit === 'mg') return q / 1000;
  if (fromUnit === 'kg') return q * 1000;
  return q;
}

// refactors the result of a quantity type query into returned type
var prepareResult = function (data, unit) {
  let res = {
    id: data.UUID,
    startDate: new Date(data.startDate),
    endDate: new Date(data.endDate),
    value: data.quantity,
    unit: unit
  };
  if (data.sourceName) res.sourceName = data.sourceName;
  if (data.sourceBundleId) res.sourceBundleId = data.sourceBundleId;
  return res;
};

// refactors the result of a correlation query into returned type
var prepareCorrelation = function (data, dataType) {
  let res = {
    id: data.UUID,
    startDate: new Date(data.startDate),
    endDate: new Date(data.endDate),
    value: {}
  };
  if (data.sourceName) res.sourceName = data.sourceName;
  if (data.sourceBundleId) res.sourceBundleId = data.sourceBundleId;
  if (dataType === 'nutrition') {
    res.unit = 'nutrition'
    if (data.metadata && data.metadata.HKFoodType) res.value.item = data.metadata.HKFoodType;
    if (data.metadata && data.metadata.HKFoodMeal) res.value.meal_type = data.metadata.HKFoodMeal;
    if (data.metadata && data.metadata.HKFoodBrandName) res.value.brand_name = data.metadata.HKFoodBrandName;
    res.value.nutrients = {};
    for (let j = 0; j < data.samples.length; j++) {
      let sample = data.samples[j];
      for (let dataname in dataTypes) {
        if (dataTypes[dataname] === sample.sampleType) {
          res.value.nutrients[dataname] = convertFromGrams(units[dataname], sample.value);
          break;
        }
      }
    }
  } else if (dataType === 'blood_pressure') {
    res.unit = 'mmHG'
    for (let j = 0; j < data.samples.length; j++) {
      let sample = data.samples[j];
      if (sample.sampleType === 'HKQuantityTypeIdentifierBloodPressureSystolic') res.value.systolic = sample.value;
      if (sample.sampleType === 'HKQuantityTypeIdentifierBloodPressureDiastolic') res.value.diastolic = sample.value;
    }
  }
  return res;
};

// merges activity (workout) samples
// fromObj is formatted as returned by query
var mergeActivitySamples = function (fromObj, intoObj) {
  if (!intoObj.value) intoObj.value = {};
  let dur = (fromObj.endDate - fromObj.startDate);
  let dist = fromObj.distance;
  let cals = fromObj.calories;
  if (intoObj.value[fromObj.value]) {
    intoObj.value[fromObj.value].duration += dur;
    intoObj.value[fromObj.value].distance += dist;
    intoObj.value[fromObj.value].calories += cals;
  } else {
    intoObj.value[fromObj.value] = {
      duration: dur,
      distance: dist,
      calories: cals
    };
  }
};

// merges nutrition samples
var mergeNutritionSamples = function (fromObj, intoObj) {
  if (!intoObj.value) intoObj.value = {};
  for (let dataname in fromObj.value.nutrients) {
    if (!intoObj.value[dataname]) intoObj.value[dataname] = fromObj.value.nutrients[dataname];
    else intoObj.value[dataname] += fromObj.value.nutrients[dataname];
  }
};

// aggregates the results into one
var aggregateIntoResult = function (data, unit, merge) {
  var res = {
    startDate: new Date(new Date().getTime() + 10 * 365 * 24 * 60 * 60 * 1000), // big date
    endDate: new Date(new Date().getTime() - 10 * 365 * 24 * 60 * 60 * 1000), // small date
    value: {},
    unit: unit
  };

  for (var i = 0; i < data.length; i++) {
    if (data[i].endDate > res.endDate) res.endDate = data[i].endDate;
    if (data[i].startDate < res.startDate) res.startDate = data[i].startDate;
    merge(data[i], res);
  }
  return res;
};

// takes the result of a query (data) and transforms them, also merges with (unprocessed) results of another query
var prepareResults = function (data, unit, mergeWith) {
  var retval = [];
  for (var i = 0; i < data.length; i++) {
    var retsample = prepareResult(data[i], unit);
    if (mergeWith) { // merge with existing array returned by a query
      for (var j = 0; j < mergeWith.length; j++) {
        // we expect the buckets to have the same start and end dates
        var mergeSample = prepareResult(mergeWith[j], unit);
        if (retsample.startDate.getTime() === mergeSample.startDate.getTime()) {
          retsample.value += mergeSample.value;
        }
      }
    }
    retval.push(retsample);
  }
  return retval;
};

// takes the results of a query (data) and merges it into a bucketized result (returned)
var bucketize = function (data, bucket, startD, endD, unit, merge) {
  var retval = [];
  // create buckets
  var sd;
  if (bucket === 'hour') {
    sd = new Date(startD.getFullYear(), startD.getMonth(), startD.getDate(), startD.getHours());
  } else if (bucket === 'day') {
    sd = new Date(startD.getFullYear(), startD.getMonth(), startD.getDate());
  } else if (bucket === 'week') {
    sd = new Date(startD.getTime());
    sd.setDate(startD.getDate() - (startD.getDay() === 0 ? 6 : startD.getDay() - 1)); // last monday
  } else if (bucket === 'month') {
    sd = new Date(startD.getFullYear(), startD.getMonth());
  } else if (bucket === 'year') {
    sd = new Date(startD.getFullYear());
  } else {
    throw 'Bucket not recognised ' + bucket;
  }
  while (sd <= endD) {
    var ed;
    if (bucket === 'hour') {
      ed = new Date(sd.getFullYear(), sd.getMonth(), sd.getDate(), sd.getHours() + 1);
    } else if (bucket === 'day') {
      ed = new Date(sd.getFullYear(), sd.getMonth(), sd.getDate() + 1);
    } else if (bucket === 'week') {
      ed = new Date(sd.getFullYear(), sd.getMonth(), sd.getDate() + 7);
    } else if (bucket === 'month') {
      ed = new Date(sd.getFullYear(), sd.getMonth() + 1);
    } else if (bucket === 'year') {
      ed = new Date(sd.getFullYear() + 1);
    }
    retval.push({
      startDate: sd,
      endDate: ed,
      value: {},
      unit: unit
    });
    sd = ed;
  }
  for (var i = 0; i < data.length; i++) {
    // select the bucket
    for (var j = 0; j < retval.length; j++) {
      if ((data[i].endDate <= retval[j].endDate) && (data[i].startDate >= retval[j].startDate)) {
        merge(data[i], retval[j]);
      }
    }
  }
  return retval;
};

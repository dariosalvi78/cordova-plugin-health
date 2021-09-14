var exec = require("cordova/exec");

var Health = function () {
  this.name = "health";
  this.supported_queries = new Set(["steps", "calories", "calories.basal", "activity",
                            "height", "weight", "heart_rate", "fat_percentage",
                            "distance", "oxygen_saturation", "blood_glucose",
                            "blood_pressure", "sleep", "nutrition.calories",
                            "nutrition.fat.total", "nutrition.fat.saturated",
                            "nutrition.fat.unsaturated","nutrition.fat.polyunsaturated",
                            "nutrition.fat.monounsaturated", "nutrition.fat.trans",
                            "nutrition.cholesterol", "nutrition.sodium",
                            "nutrition.potassium", "nutrition.carbs.total",
                            "nutrition.dietary_fiber", "nutrition.sugar", "nutrition.water",
                            "nutrition.protein", "nutrition.vitamin_a", "nutrition",
                            "nutrition.vitamin_c", "nutrition.calcium", "nutrition.iron"]);
};

Health.prototype.filter_datatypes = function(datatypes){
  var new_data_type = [];
  for(var i=0; i<datatypes.length; i++){
    if (typeof(datatypes[i]) == "object"){
      var readOnlyDtypes = datatypes[i]["read"];
      var writeOnlyDtypes = datatypes[i]["write"];
      var newWriteOnlyDtypes = [];
      var newReadOnlyDtypes = [];
      if(!!readOnlyDtypes){
        newReadOnlyDtypes = this.filter_datatypes(readOnlyDtypes);
      }
      if(!!writeOnlyDtypes){
        newWriteOnlyDtypes = this.filter_datatypes(writeOnlyDtypes);
      }
      new_data_type.push({"read" : newReadOnlyDtypes, "write" : newWriteOnlyDtypes});
    }
    else {
      if (this.supported_queries.has(datatypes[i])){
        new_data_type.push(datatypes[i]);
      }
    }
  }
  return new_data_type;
}

Health.prototype.isAvailable = function (onSuccess, onError) {
  exec(onSuccess, onError, "health", "isAvailable", []);
};

Health.prototype.disconnect = function (onSuccess, onError) {
  exec(onSuccess, onError, "health", "disconnect", []);
};

Health.prototype.promptInstallFit = function (onSuccess, onError) {
  exec(onSuccess, onError, "health", "promptInstallFit", []);
};

Health.prototype.requestAuthorization = function (datatypes, onSuccess, onError) {
  exec(onSuccess, onError, "health", "requestAuthorization", this.filter_datatypes(datatypes));
};

Health.prototype.isAuthorized = function (datatypes, onSuccess, onError) {
  exec(onSuccess, onError, "health", "isAuthorized", datatypes);
};

Health.prototype.query = function (opts, onSuccess, onError) {
  //calories.active is done by asking all calories and subtracting the basal
  if(opts.dataType =='calories.active'){
    //get basal average in the time window between endDate and BASAL_CALORIES_QUERY_PERIOD
    navigator.health.queryAggregated({
      dataType:'calories.basal',
      endDate: opts.endDate,
      startDate: opts.startDate,
      bucket: opts.bucket
    }, function(data){
      var basal_ms = data.value / (opts.endDate - opts.startDate);
      //now get the total
      opts.dataType ='calories';
      navigator.health.query(opts, function(data){
        //and subtract the basal
        for(var i=0; i<data.length; i++){
          data[i].value -= basal_ms * (data[i].endDate.getTime() - data[i].startDate.getTime());

          //although it shouldn't happen, after subtracting, sometimes the values are negative,
          //in that case let's return 0 (negative values don't make sense)
          if(data[i].value <0) data[i].value = 0;
        }
        onSuccess(data);
      }, onError);
    }, onError);
  } else {
    //standard case, just use the name as it is
    if(opts.startDate && (typeof opts.startDate == 'object'))
    opts.startDate = opts.startDate.getTime();
    if(opts.endDate && (typeof opts.endDate == 'object'))
    opts.endDate = opts.endDate.getTime();
    exec(function(data){
      for(var i=0; i<data.length; i++){
        data[i].startDate = new Date(data[i].startDate);
        data[i].endDate = new Date(data[i].endDate);
      }
      // if nutrition, add water
      if(opts.dataType == 'nutrition'){
        opts.dataType ='nutrition.water';
        navigator.health.query(opts, function(water){
            // merge and sort
            for(var i=0; i<water.length; i++) {
                water[i].value = { item: "water", nutrients: { "nutrition.water": water[i].value } };
                water[i].unit = "nutrition";
            }
            data.concat(water);
            data = data.concat(water);
            data.sort(function(a,b){
                return a.startDate - b.startDate;
            })
            onSuccess(data);
        }, onError)
      } else {
        onSuccess(data);
      }
    }, onError, "health", "query", [opts]);
  }
};

Health.prototype.queryAggregated = function (opts, onSuccess, onError) {
  if(opts.dataType =='calories.active'){
    //get basal average
    navigator.health.queryAggregated({
      dataType:'calories.basal',
      endDate: opts.endDate,
      startDate: opts.startDate
    }, function(data){
      var basal_ms = data.value / (opts.endDate - opts.startDate);
      //now get the total
      opts.dataType ='calories';
      navigator.health.queryAggregated(opts, function(retval){
        //and remove the basal
        retval.value -= basal_ms * (retval.endDate.getTime() - retval.startDate.getTime());
        //although it shouldn't happen....
        if(retval.value <0) retval.value = 0;
        onSuccess(retval);
      }, onError);
    }, onError);
  } else {
    if(typeof opts.startDate == 'object') opts.startDate = opts.startDate.getTime();
    if(typeof opts.endDate == 'object') opts.endDate = opts.endDate.getTime();
    exec(function(data){
      //reconvert the dates back to Date objects
      if(Object.prototype.toString.call( data ) === '[object Array]'){
        //it's an array
        for(var i=0; i<data.length; i++){
          data[i].startDate = new Date(data[i].startDate);
          data[i].endDate = new Date(data[i].endDate);
        }
      } else {
        data.startDate = new Date(data.startDate);
        data.endDate = new Date(data.endDate);
      }

      // if nutrition, add water
      if(opts.dataType == 'nutrition'){
        opts.dataType ='nutrition.water';
        navigator.health.queryAggregated(opts, function(water){
            data.value['nutrition.water'] = water.value;
            onSuccess(data);
        }, onError)
      } else {
          onSuccess(data);
      }
    }, onError, 'health', 'queryAggregated', [opts]);
  }
};

Health.prototype.store = function (data, onSuccess, onError) {
  if(data.dataType =='calories.basal'){
    onError('basal calories cannot be stored in Android');
    return;
  }
  if(data.dataType =='calories.active'){
    //rename active calories to total calories
    data.dataType ='calories';
  }
  if(data.startDate && (typeof data.startDate == 'object'))
  data.startDate = data.startDate.getTime();
  if(data.endDate && (typeof data.endDate == 'object'))
  data.endDate = data.endDate.getTime();
  if(data.dataType =='activity'){
    data.value = navigator.health.toFitActivity(data.value);
  }
  exec(onSuccess, onError, "health", "store", [data]);
};

Health.prototype.delete = function (data, onSuccess, onError) {
  if(data.dataType =='calories.basal'){
    onError('basal calories cannot be deleted in Android');
    return;
  }
  if(data.dataType =='calories.active'){
    //rename active calories to total calories
    data.dataType ='calories';
  }
  if(data.startDate && (typeof data.startDate == 'object'))
  data.startDate = data.startDate.getTime();
  if(data.endDate && (typeof data.endDate == 'object'))
  data.endDate = data.endDate.getTime();
  if(data.dataType =='activity'){
    data.value = navigator.health.toFitActivity(data.value);
  }
  exec(onSuccess, onError, "health", "delete", [data]);
};

Health.prototype.toFitActivity = function (act) {
  if (act === 'core_training') return 'strength_training';
  if (act === 'flexibility') return 'gymnastics';
  if (act === 'stairs') return 'stair_climbing';
  if (act === 'wheelchair.walkpace') return 'wheelchair';
  if (act === 'wheelchair.runpace') return 'wheelchair';
  if (act === 'sleep.inBed') return 'sleep.awake';
  // unsupported activities are mapped to 'other'
  if ((act === 'archery') ||
  (act === 'barre') ||
  (act === 'bowling') ||
  (act === 'fishing') ||
  (act === 'functional_strength') ||
  (act === 'hunting') ||
  (act === 'lacrosse') ||
  (act === 'mixed_metabolic_cardio') ||
  (act === 'paddle_sports') ||
  (act === 'play') ||
  (act === 'preparation_and_recovery') ||
  (act === 'snow_sports') ||
  (act === 'softball') ||
  (act === 'water_fitness') ||
  (act === 'water_sports') ||
  (act === 'wrestling')) return 'other';
  else return act;
};

cordova.addConstructor(function () {
  navigator.health = new Health();
  return navigator.health;
});

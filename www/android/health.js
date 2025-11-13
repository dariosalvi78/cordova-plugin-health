var exec = require("cordova/exec");


module.exports = {

  name: "Health",

  isAvailable (onSuccess, onError) {
    exec(onSuccess, onError, "health", "isAvailable", []);
  },

  getHealthConnectFromStore (onSuccess, onError) {
    exec(onSuccess, onError, "health", "getHealthConnectFromStore", []);
  },

  launchPrivacyPolicy (onSuccess, onError) {
    exec(onSuccess, onError, "health", "launchPrivacyPolicy", [])
  },

  openHealthSettings (onSuccess, onError) {
    exec(onSuccess, onError, "health", "openHealthSettings", [])
  },

  isAuthorized (authObj, onSuccess, onError) {
    exec(onSuccess, onError, "health", "isAuthorized", [authObj])
  },

  requestAuthorization (authObj, onSuccess, onError) {
    exec(onSuccess, onError, "health", "requestAuthorization", [authObj])
  },

  query (opts, onSuccess, onError) {
    if (opts.startDate && (typeof opts.startDate == 'object'))
      opts.startDate = opts.startDate.getTime()
    if (opts.endDate && (typeof opts.endDate == 'object'))
      opts.endDate = opts.endDate.getTime();
    const subQuery = (param, startDate, endDate) => {
      return new Promise((resolve, reject) => {
        this.queryAggregated({
          startDate: startDate,
          endDate: endDate,
          dataType: param
        },
          (result) => resolve(result.value),
          (error) => reject(error));
      });
    }
    exec((data) => {
      data.map(async (item) => {
        if (item.startDate) item.startDate = new Date(item.startDate);
        if (item.endDate) item.endDate = new Date(item.endDate);

        if (opts.dataType == 'sleep' && opts.sleepSession) {
          // convert start and end dates for single stages
          item.value.map((stage) => {
            stage.startDate = new Date(stage.startDate);
            stage.endDate = new Date(stage.endDate);
          });
        }
        if (opts.dataType == 'activity' && (opts.includeCalories || opts.includeDistance)) {
          // we need to also fetch calories and/or distance
          if (opts.includeCalories) {
            // calories are needed, fetch them
            try {
              item.calories = await subQuery('calories.active', item.startDate, item.endDate)
            } catch (error) {
              onError(error);
            }
          } else {
            // distance only is needed
            try {
              item.distance = await subQuery('distance', item.startDate, item.endDate)
            } catch (error) {
              onError(error);
            }
          }
        }
      });
      onSuccess(data);
    }, onError, "health", "query", [opts])
  },

  queryAggregated (opts, onSuccess, onError) {
    if (typeof opts.startDate == 'object') opts.startDate = opts.startDate.getTime()
    if (typeof opts.endDate == 'object') opts.endDate = opts.endDate.getTime()
    exec((data) => {
      //reconvert the dates back to Date objects
      if (Object.prototype.toString.call(data) === '[object Array]') {
        //it's an array, iterate through each item
        for (var i = 0; i < data.length; i++) {
          data[i].startDate = new Date(data[i].startDate)
          data[i].endDate = new Date(data[i].endDate)
        }
      } else { // not an array
        data.startDate = new Date(data.startDate)
        data.endDate = new Date(data.endDate)
      }

      onSuccess(data)
    }, onError, 'health', 'queryAggregated', [opts])
  },

  store (data, onSuccess, onError) {
    if (data.startDate && (typeof data.startDate == 'object'))
      data.startDate = data.startDate.getTime()
    if (data.endDate && (typeof data.endDate == 'object'))
      data.endDate = data.endDate.getTime()

    if (data.dataType == 'sleep' && data.sleepSession) {
      // convert start and end dates for single stages
      for (let stageI = 0; stageI < data.value.length; stageI++) {
        if (data.value[stageI].startDate && (typeof data.value[stageI].startDate == 'object'))
          data.value[stageI].startDate = data.value[stageI].startDate.getTime()

        if (data.value[stageI].endDate && (typeof data.value[stageI].endDate == 'object'))
          data.value[stageI].endDate = data.value[stageI].endDate.getTime()
      }
    }

    exec(onSuccess, onError, "health", "store", [data])
  },

  delete (data, onSuccess, onError) {
    if (data.startDate && (typeof data.startDate == 'object'))
      data.startDate = data.startDate.getTime()
    if (data.endDate && (typeof data.endDate == 'object'))
      data.endDate = data.endDate.getTime()
    exec(onSuccess, onError, "health", "delete", [data]);
  }
}

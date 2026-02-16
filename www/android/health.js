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
  
  stopBackGround (opts, onSuccess, onError) {
    exec(onSuccess, onError, "health", "stopBackGround", [opts]);
  },

  queryInBackGround (opts, onSuccess, onError) {
    if (opts.startDate && (typeof opts.startDate == 'object'))
      opts.startDate = opts.startDate.getTime()
    if (opts.endDate && (typeof opts.endDate == 'object'))
      opts.endDate = opts.endDate.getTime();
    exec((data) => {
      // here we use a recursive function instead of a simple loop
      // this is to deal with additional queries required for the special case
      // of activity with calories and/or distance
      finalizeResult = (i) => {
        if (i >= data.length) {
          // completed, return results
          onSuccess(data);
        } else {
          // iterate
          // convert timestamps to date
          if (data[i].startDate) data[i].startDate = new Date(data[i].startDate)
          if (data[i].endDate) data[i].endDate = new Date(data[i].endDate)

          if (opts.dataType == 'sleep' && opts.sleepSession) {
            // convert start and end dates for single stages
            for (let stageI = 0; stageI < data[i].value.length; stageI++) {
              data[i].value[stageI].startDate = new Date(data[i].value[stageI].startDate)
              data[i].value[stageI].endDate = new Date(data[i].value[stageI].endDate)
            }
          }

          if (opts.dataType == 'activity' && (opts.includeCalories || opts.includeDistance)) {
            // we need to also fetch calories and/or distance

            // helper function to get aggregated calories for that activity
            getCals = (onDone) => {
              this.queryAggregated({
                startDate: data[i].startDate,
                endDate: data[i].endDate,
                dataType: 'calories.active'
              }, (cals) => {
                data[i].calories = cals.value
                onDone()
              }, onError)
            }
            // helper function to get aggregated distance for that activity
            getDist = (onDone) => {
              this.queryAggregated({
                startDate: data[i].startDate,
                endDate: data[i].endDate,
                dataType: 'distance'
              }, (dist) => {
                data[i].distance = dist.value
                onDone()
              }, onError)
            }

            if (opts.includeCalories) {
              // calories are needed, fetch them
              getCals(() => {
                // now get the distance, if needed
                if (opts.includeDistance) {
                  getDist(() => {
                    finalizeResult(i + 1)
                  })
                } else {
                  // no distance needed, move on
                  finalizeResult(i + 1)
                }
              })
            } else {
              // distance only is needed
              getDist(() => {
                finalizeResult(i + 1)
              })
            }
          } else {
            finalizeResult(i + 1)
          }
        }
      }
      finalizeResult(0);
    }, onError, "health", "queryInBackGround", [opts])
  },
  
  query (opts, onSuccess, onError) {
    if (opts.startDate && (typeof opts.startDate == 'object'))
      opts.startDate = opts.startDate.getTime()
    if (opts.endDate && (typeof opts.endDate == 'object'))
      opts.endDate = opts.endDate.getTime();
    exec((data) => {
      // here we use a recursive function instead of a simple loop
      // this is to deal with additional queries required for the special case
      // of activity with calories and/or distance
      finalizeResult = (i) => {
        if (i >= data.length) {
          // completed, return results
          onSuccess(data);
        } else {
          // iterate
          // convert timestamps to date
          if (data[i].startDate) data[i].startDate = new Date(data[i].startDate)
          if (data[i].endDate) data[i].endDate = new Date(data[i].endDate)

          if (opts.dataType == 'sleep' && opts.sleepSession) {
            // convert start and end dates for single stages
            for (let stageI = 0; stageI < data[i].value.length; stageI++) {
              data[i].value[stageI].startDate = new Date(data[i].value[stageI].startDate)
              data[i].value[stageI].endDate = new Date(data[i].value[stageI].endDate)
            }
          }

          if (opts.dataType == 'activity' && (opts.includeCalories || opts.includeDistance)) {
            // we need to also fetch calories and/or distance

            // helper function to get aggregated calories for that activity
            getCals = (onDone) => {
              this.queryAggregated({
                startDate: data[i].startDate,
                endDate: data[i].endDate,
                dataType: 'calories.active'
              }, (cals) => {
                data[i].calories = cals.value
                onDone()
              }, onError)
            }
            // helper function to get aggregated distance for that activity
            getDist = (onDone) => {
              this.queryAggregated({
                startDate: data[i].startDate,
                endDate: data[i].endDate,
                dataType: 'distance'
              }, (dist) => {
                data[i].distance = dist.value
                onDone()
              }, onError)
            }

            if (opts.includeCalories) {
              // calories are needed, fetch them
              getCals(() => {
                // now get the distance, if needed
                if (opts.includeDistance) {
                  getDist(() => {
                    finalizeResult(i + 1)
                  })
                } else {
                  // no distance needed, move on
                  finalizeResult(i + 1)
                }
              })
            } else {
              // distance only is needed
              getDist(() => {
                finalizeResult(i + 1)
              })
            }
          } else {
            finalizeResult(i + 1)
          }
        }
      }
      finalizeResult(0);
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

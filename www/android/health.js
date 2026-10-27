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
    exec((data) => {
      // convert timestamps to dates with a plain loop (no recursion, so no stack overflow on large results)
      for (let i = 0; i < data.length; i++) {
        if (data[i].startDate) data[i].startDate = new Date(data[i].startDate)
        if (data[i].endDate) data[i].endDate = new Date(data[i].endDate)

        if (opts.dataType == 'sleep' && opts.sleepSession) {
          // convert start and end dates for single stages
          for (let stageI = 0; stageI < data[i].value.length; stageI++) {
            data[i].value[stageI].startDate = new Date(data[i].value[stageI].startDate)
            data[i].value[stageI].endDate = new Date(data[i].value[stageI].endDate)
          }
        }
      }

      if (!(opts.dataType == 'activity' && (opts.includeCalories || opts.includeDistance))) {
        onSuccess(data)
        return
      }

      // activity with calories and/or distance: additional queries are needed,
      // run them sequentially. Each step resumes from an asynchronous native
      // callback, so the call stack does not grow with the number of samples.
      let i = 0
      const processNext = () => {
        if (i >= data.length) {
          onSuccess(data)
          return
        }
        const item = data[i]
        const steps = []
        if (opts.includeCalories) steps.push(['calories.active', 'calories'])
        if (opts.includeDistance) steps.push(['distance', 'distance'])
        let s = 0
        const runStep = () => {
          if (s >= steps.length) {
            i++
            processNext()
            return
          }
          const [dataType, field] = steps[s++]
          this.queryAggregated({
            startDate: item.startDate,
            endDate: item.endDate,
            dataType: dataType
          }, (res) => {
            item[field] = res.value
            runStep()
          }, onError)
        }
        runStep()
      }
      processNext()
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

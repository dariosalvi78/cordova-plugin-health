function HealthKit() {
}

var matches = function(object, typeOrClass) {
  return (typeof typeOrClass === 'string') ?
    typeof object === typeOrClass : object instanceof typeOrClass;
};

var rounds = function(object, prop) {
  var val = object[prop];
  if (!matches(val, Date)) return;
  object[prop] = Math.round(val.getTime() / 1000);
};

var hasValidDates = function(object) {
  if (!matches(object.startDate, Date)) {
    throw new TypeError("startDate must be a JavaScript Date Object");
  }
  if (!matches(object.endDate, Date)) {
    throw new TypeError("endDate must be a JavaScript Date Object");
  }
  rounds(object, 'startDate');
  rounds(object, 'endDate');
  return object;
};

var getChecker = function(options) {
  return function paramChecker(type) {
    var value = options[type];
    if (type === 'startDate' || type === 'endDate') {
      if (!matches(value, Date)) throw new TypeError(type + ' must be a JavaScript Date');
    } else if (type === 'samples') {
      if (!Array.isArray(value)) throw new TypeError(type + ' must be a JavaScript Array');
    } else {
      if (!value) throw new TypeError('Missing required paramter ' + type);
    }
  };
};

// Supports:
// define('type');
// define('type', fn);
// define('type', obj);
// define('type', obj, fn)
var define = function(methodName, params, fn) {
  if (params == null) params = {};
  if (typeof params === 'function') {
    fn = params;
    params = {};
  }
  if (!fn) fn = Function.prototype;

  var isEmpty = !!(params && params.noArgs);
  var checks = params.required || [];
  if (!Array.isArray(checks)) checks = [checks];

  if (isEmpty) {
    HealthKit.prototype[methodName] = function(callback, onError) {
      cordova.exec(callback, onError, 'HealthKit', methodName, []);
    };
  } else {
    HealthKit.prototype[methodName] = function(options, callback, onError) {
      if (!options) options = {};
      try {
        checks.forEach(getChecker(options));
        fn(options);
      } catch (error) {
        onError(error.message);
      }

      var args = options ? [options] : [];
      cordova.exec(callback, onError, 'HealthKit', methodName, args);
    };
  };
};

define('available', {noArgs: true});
define('checkAuthStatus');
define('requestAuthorization');
define('readDateOfBirth', {noArgs: true});
define('readGender', {noArgs: true});
define('readFitzpatrickSkinType', {noArgs: true});
define('findWorkouts');
define('delete');
define('readWeight');
define('readHeight');
define('readBloodType', {noArgs: true});
define('queryActivitySummary', {}, hasValidDates);
define('queryElectrocardiogramSamples', {}, hasValidDates);
define('queryAudiogramSamples', {}, hasValidDates);

define('saveWeight', function(options) {
  if (options.date == null) options.date = new Date();
  if (typeof options.date === 'object') rounds(options, 'date');
});

define('saveHeight', function(options) {
  if (options.date == null) options.date = new Date();
  if (typeof options.date === 'object') rounds(options, 'date');
});

define('saveWorkout', {required: 'startDate'}, function(options) {
  var hasEnd = matches(options.endDate, Date);
  var hasDuration = options.duration && options.duration > 0;
  rounds(options, 'startDate');

  if (!hasEnd && !hasDuration) {
    throw new TypeError("endDate must be JavaScript Date Object, or the duration must be set");
  }
  if (hasEnd) rounds(options, 'endDate');
});

define('monitorSampleType', {required: 'sampleType'});
define('querySampleType', {required: 'sampleType'}, hasValidDates);

define('querySampleTypeAggregated', {required: 'sampleType'}, hasValidDates);

define('deleteSamples', {required: 'sampleType'}, hasValidDates);

define('queryCorrelationType', {required: 'correlationType'}, hasValidDates);
define('saveSample', {required: 'sampleType'}, hasValidDates);

define('saveCorrelation', {required: ['correlationType', 'samples']}, function(options) {
  hasValidDates(options);
  options.objects = options.samples.map(hasValidDates);
});

define('sumQuantityType', {required: ['sampleType']}, hasValidDates);

HealthKit.install = function() {
  if (!window.plugins) window.plugins = {};
  window.plugins.healthkit = new HealthKit();
  return window.plugins.healthkit;
};

cordova.addConstructor(HealthKit.install);

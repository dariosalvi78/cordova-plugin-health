#import "HealthKit.h"
#import "HKHealthStore+AAPLExtensions.h"
#import "WorkoutActivityConversion.h"

#pragma clang diagnostic push
#pragma ide diagnostic ignored "OCNotLocalizedStringInspection"
#define HKPLUGIN_DEBUG

#pragma mark Property Type Constants
static NSString *const HKPluginError = @"HKPluginError";
static NSString *const HKPluginKeyReadTypes = @"readTypes";
static NSString *const HKPluginKeyWriteTypes = @"writeTypes";
static NSString *const HKPluginKeyType = @"type";
static NSString *const HKPluginKeyStartDate = @"startDate";
static NSString *const HKPluginKeyEndDate = @"endDate";
static NSString *const HKPluginKeySampleType = @"sampleType";
static NSString *const HKPluginKeyAggregation = @"aggregation";
static NSString *const HKPluginKeyUnit = @"unit";
static NSString *const HKPluginKeyUnits = @"units";
static NSString *const HKPluginKeyAmount = @"amount";
static NSString *const HKPluginKeyValue = @"value";
static NSString *const HKPluginKeyCorrelationType = @"correlationType";
static NSString *const HKPluginKeyObjects = @"samples";
static NSString *const HKPluginKeySourceName = @"sourceName";
static NSString *const HKPluginKeySourceBundleId = @"sourceBundleId";
static NSString *const HKPluginKeyMetadata = @"metadata";
static NSString *const HKPluginKeyUUID = @"UUID";

#pragma mark Categories

// NSDictionary check if there is a value for a required key and populate an error if not present
@interface NSDictionary (RequiredKey)
- (BOOL)hasAllRequiredKeys:(NSArray<NSString *> *)keys error:(NSError **)error;
@end

// Public Interface extension category
@interface HealthKit ()
+ (HKHealthStore *)sharedHealthStore;
@end

// Internal interface
@interface HealthKit (Internal)
- (void)checkAuthStatusWithCallbackId:(NSString *)callbackId
                              forType:(HKObjectType *)type
                        andCompletion:(void (^)(CDVPluginResult *result, NSString *innerCallbackId))completion;
@end


// Internal interface helper methods
@interface HealthKit (InternalHelpers)
+ (NSString *)stringFromDate:(NSDate *)date;

+ (HKUnit *)getUnit:(NSString *)type expected:(NSString *)expected;

+ (HKObjectType *)getHKObjectType:(NSString *)elem;

+ (HKQuantityType *)getHKQuantityType:(NSString *)elem;

+ (HKSampleType *)getHKSampleType:(NSString *)elem;

- (HKQuantitySample *)loadHKSampleFromInputDictionary:(NSDictionary *)inputDictionary error:(NSError **)error;

- (HKCorrelation *)loadHKCorrelationFromInputDictionary:(NSDictionary *)inputDictionary error:(NSError **)error;

+ (HKQuantitySample *)getHKQuantitySampleWithStartDate:(NSDate *)startDate endDate:(NSDate *)endDate sampleTypeString:(NSString *)sampleTypeString unitTypeString:(NSString *)unitTypeString value:(double)value metadata:(NSDictionary *)metadata error:(NSError **)error;

- (HKCorrelation *)getHKCorrelationWithStartDate:(NSDate *)startDate endDate:(NSDate *)endDate correlationTypeString:(NSString *)correlationTypeString objects:(NSSet *)objects metadata:(NSDictionary *)metadata error:(NSError **)error;

+ (void)triggerErrorCallbackWithMessage: (NSString *) message command: (CDVInvokedUrlCommand *) command delegate: (id<CDVCommandDelegate>) delegate;
@end

/**
 * Implementation of internal interface
 * **************************************************************************************
 */
#pragma mark Internal Interface

@implementation HealthKit (Internal)

/**
 * Check the authorization status for a HealthKit type and dispatch the callback with result
 *
 * @param callbackId    *NSString
 * @param type          *HKObjectType
 * @param completion    void(^)
 */
- (void)checkAuthStatusWithCallbackId:(NSString *)callbackId forType:(HKObjectType *)type andCompletion:(void (^)(CDVPluginResult *, NSString *))completion {

    CDVPluginResult *pluginResult = nil;

    if (type == nil) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"type is an invalid value"];
    } else {
        HKAuthorizationStatus status = [[HealthKit sharedHealthStore] authorizationStatusForType:type];

        NSString *authorizationResult = nil;
        switch (status) {
            case HKAuthorizationStatusSharingAuthorized:
                authorizationResult = @"authorized";
                break;
            case HKAuthorizationStatusSharingDenied:
                authorizationResult = @"denied";
                break;
            default:
                authorizationResult = @"undetermined";
        }

#ifdef HKPLUGIN_DEBUG
        NSLog(@"Health store returned authorization status: %@ for type %@", authorizationResult, [type description]);
#endif

        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:authorizationResult];
    }

    completion(pluginResult, callbackId);
}

@end

/**
 * Implementation of internal helpers interface
 * **************************************************************************************
 */
#pragma mark Internal Helpers

@implementation HealthKit (InternalHelpers)

/**
 * Get a string representation of an NSDate object
 *
 * @param date  *NSDate
 * @return      *NSString
 */
+ (NSString *)stringFromDate:(NSDate *)date {
    __strong static NSDateFormatter *formatter = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        formatter = [[NSDateFormatter alloc] init];
        [formatter setLocale:[NSLocale localeWithLocaleIdentifier:@"en_US_POSIX"]];
        [formatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ssZZZZZ"];
    });

    return [formatter stringFromDate:date];
}

/**
 * Get a HealthKit unit and make sure its local representation matches what is expected
 *
 * @param type      *NSString
 * @param expected  *NSString
 * @return          *HKUnit
 */
+ (HKUnit *)getUnit:(NSString *)type expected:(NSString *)expected {
    HKUnit *localUnit;
    @try {
        // this throws an exception instead of returning nil if type is unknown
        localUnit = [HKUnit unitFromString:type];
        if ([[[localUnit class] description] isEqualToString:expected]) {
            return localUnit;
        } else {
            return nil;
        }
    }
    @catch (NSException *e) {
        return nil;
    }
}

/**
 * Get a HealthKit object type by name
 *
 * @param elem  *NSString
 * @return      *HKObjectType
 */
+ (HKObjectType *)getHKObjectType:(NSString *)elem {

    HKObjectType *type = nil;

    type = [HKObjectType quantityTypeForIdentifier:elem];
    if (type != nil) {
        return type;
    }

    type = [HKObjectType characteristicTypeForIdentifier:elem];
    if (type != nil) {
        return type;
    }

    // @TODO | The fall through here is inefficient.
    // @TODO | It needs to be refactored so the same HK method isnt called twice
    return [HealthKit getHKSampleType:elem];
}

/**
 * Get a HealthKit quantity type by name
 *
 * @param elem  *NSString
 * @return      *HKQuantityType
 */
+ (HKQuantityType *)getHKQuantityType:(NSString *)elem {
    return [HKQuantityType quantityTypeForIdentifier:elem];
}

/**
 * Get sample type by name
 *
 * @param elem  *NSString
 * @return      *HKSampleType
 */
+ (HKSampleType *)getHKSampleType:(NSString *)elem {

    HKSampleType *type = nil;

    type = [HKObjectType quantityTypeForIdentifier:elem];
    if (type != nil) {
        return type;
    }

    type = [HKObjectType categoryTypeForIdentifier:elem];
    if (type != nil) {
        return type;
    }

    type = [HKObjectType correlationTypeForIdentifier:elem];
    if (type != nil) {
        return type;
    }

    if ([elem isEqualToString:@"workoutType"]) {
        return [HKObjectType workoutType];
    }

    // leave this here for if/when apple adds other sample types
    return type;

}

/**
 * Parse out a sample from a dictionary and perform error checking
 *
 * @param inputDictionary   *NSDictionary
 * @param error             **NSError
 * @return                  *HKQuantitySample
 */
- (HKSample *)loadHKSampleFromInputDictionary:(NSDictionary *)inputDictionary error:(NSError **)error {
    //Load quantity sample from args to command

    if (![inputDictionary hasAllRequiredKeys:@[HKPluginKeyStartDate, HKPluginKeyEndDate, HKPluginKeySampleType] error:error]) {
        return nil;
    }

    NSDate *startDate = [NSDate dateWithTimeIntervalSince1970:[inputDictionary[HKPluginKeyStartDate] longValue]];
    NSDate *endDate = [NSDate dateWithTimeIntervalSince1970:[inputDictionary[HKPluginKeyEndDate] longValue]];
    NSString *sampleTypeString = inputDictionary[HKPluginKeySampleType];

    //Load optional metadata key
    NSDictionary *metadata = inputDictionary[HKPluginKeyMetadata];
    if (metadata == nil) {
      metadata = @{};
    }

    if ([inputDictionary objectForKey:HKPluginKeyUnit]) {
        if (![inputDictionary hasAllRequiredKeys:@[HKPluginKeyUnit] error:error]) return nil;
        NSString *unitString = [inputDictionary objectForKey:HKPluginKeyUnit];

            return [HealthKit getHKQuantitySampleWithStartDate:startDate
                                                   endDate:endDate
                                          sampleTypeString:sampleTypeString
                                            unitTypeString:unitString
                                                     value:[inputDictionary[HKPluginKeyAmount] doubleValue]
                                                  metadata:metadata error:error];
    } else {
            if (![inputDictionary hasAllRequiredKeys:@[HKPluginKeyValue] error:error]) return nil;
            NSString *categoryString = [inputDictionary objectForKey:HKPluginKeyValue];

            return [self getHKCategorySampleWithStartDate:startDate
                                                       endDate:endDate
                                              sampleTypeString:sampleTypeString
                                                categoryString:categoryString
                                                      metadata:metadata
                                                         error:error];
        }
  }

/**
 * Parse out a correlation from a dictionary and perform error checking
 *
 * @param inputDictionary   *NSDictionary
 * @param error             **NSError
 * @return                  *HKCorrelation
 */
- (HKCorrelation *)loadHKCorrelationFromInputDictionary:(NSDictionary *)inputDictionary error:(NSError **)error {
    //Load correlation from args to command

    if (![inputDictionary hasAllRequiredKeys:@[HKPluginKeyStartDate, HKPluginKeyEndDate, HKPluginKeyCorrelationType, HKPluginKeyObjects] error:error]) {
        return nil;
    }

    NSDate *startDate = [NSDate dateWithTimeIntervalSince1970:[inputDictionary[HKPluginKeyStartDate] longValue]];
    NSDate *endDate = [NSDate dateWithTimeIntervalSince1970:[inputDictionary[HKPluginKeyEndDate] longValue]];
    NSString *correlationTypeString = inputDictionary[HKPluginKeyCorrelationType];
    NSArray *objectDictionaries = inputDictionary[HKPluginKeyObjects];

    NSMutableSet *objects = [NSMutableSet set];
    for (NSDictionary *objectDictionary in objectDictionaries) {
        HKSample *sample = [self loadHKSampleFromInputDictionary:objectDictionary error:error];
        if (sample == nil) {
            return nil;
        }
        [objects addObject:sample];
    }

    NSDictionary *metadata = inputDictionary[HKPluginKeyMetadata];
    if (metadata == nil) {
        metadata = @{};
    }
    return [self getHKCorrelationWithStartDate:startDate
                                       endDate:endDate
                         correlationTypeString:correlationTypeString
                                       objects:objects
                                      metadata:metadata
                                         error:error];
}

/**
 * Query HealthKit to get a quantity sample in a specified date range
 *
 * @param startDate         *NSDate
 * @param endDate           *NSDate
 * @param sampleTypeString  *NSString
 * @param unitTypeString    *NSString
 * @param value             double
 * @param metadata          *NSDictionary
 * @param error             **NSError
 * @return                  *HKQuantitySample
 */
+ (HKQuantitySample *)getHKQuantitySampleWithStartDate:(NSDate *)startDate
                                               endDate:(NSDate *)endDate
                                      sampleTypeString:(NSString *)sampleTypeString
                                        unitTypeString:(NSString *)unitTypeString
                                                 value:(double)value
                                              metadata:(NSDictionary *)metadata
                                                 error:(NSError **)error {
    HKQuantityType *type = [HealthKit getHKQuantityType:sampleTypeString];
    if (type == nil) {
        if (error != nil) {
            *error = [NSError errorWithDomain:HKPluginError code:0 userInfo:@{NSLocalizedDescriptionKey: @"quantity type string was invalid"}];
        }

        return nil;
    }

    HKUnit *unit = nil;
    @try {
        if (unitTypeString != nil) {
            if ([unitTypeString isEqualToString:@"mmol/L"]) {
                // @see https://stackoverflow.com/a/30196642/1214598
                unit = [[HKUnit moleUnitWithMetricPrefix:HKMetricPrefixMilli molarMass:HKUnitMolarMassBloodGlucose] unitDividedByUnit:[HKUnit literUnit]];
            } else {
                // issue 51
                // @see https://github.com/Telerik-Verified-Plugins/HealthKit/issues/51
                if ([unitTypeString isEqualToString:@"percent"]) {
                    unitTypeString = @"%";
                }
                unit = [HKUnit unitFromString:unitTypeString];
            }
        } else {
            if (error != nil) {
                *error = [NSError errorWithDomain:HKPluginError code:0 userInfo:@{NSLocalizedDescriptionKey: @"unit is invalid"}];
            }
            return nil;
        }
    } @catch (NSException *e) {
        if (error != nil) {
            *error = [NSError errorWithDomain:HKPluginError code:0 userInfo:@{NSLocalizedDescriptionKey: @"unit is invalid"}];
        }
        return nil;
    }

    HKQuantity *quantity = [HKQuantity quantityWithUnit:unit doubleValue:value];
    if (![quantity isCompatibleWithUnit:unit]) {
        if (error != nil) {
            *error = [NSError errorWithDomain:HKPluginError code:0 userInfo:@{NSLocalizedDescriptionKey: @"unit is not compatible with quantity"}];
        }

        return nil;
    }

    return [HKQuantitySample quantitySampleWithType:type quantity:quantity startDate:startDate endDate:endDate metadata:metadata];
}

// Helper to handle the functionality with HealthKit to get a category sample
- (HKCategorySample*) getHKCategorySampleWithStartDate:(NSDate*) startDate endDate:(NSDate*) endDate sampleTypeString:(NSString*) sampleTypeString categoryString:(NSString*) categoryString metadata:(NSDictionary*) metadata error:(NSError**) error {
    HKCategoryType *type = [HKCategoryType categoryTypeForIdentifier:sampleTypeString];
    if (type==nil) {
      *error = [NSError errorWithDomain:HKPluginError code:0 userInfo:@{NSLocalizedDescriptionKey:@"quantity type string is invalid"}];
      return nil;
    }
    NSNumber* value = [self getCategoryValueByName:categoryString type:type];
    if (value == nil && ![type.identifier isEqualToString:@"HKCategoryTypeIdentifierMindfulSession"]) {
        *error = [NSError errorWithDomain:HKPluginError code:0 userInfo:@{NSLocalizedDescriptionKey:[NSString stringWithFormat:@"%@,%@,%@",@"category value is not compatible with category",type.identifier,categoryString]}];
        return nil;
    }

    return [HKCategorySample categorySampleWithType:type value:[value integerValue] startDate:startDate endDate:endDate];
}

- (NSNumber*) getCategoryValueByName:(NSString *) categoryValue type:(HKCategoryType*) type {
    NSDictionary * map = @{
      @"HKCategoryTypeIdentifierSleepAnalysis":@{
        @"HKCategoryValueSleepAnalysisInBed":@(HKCategoryValueSleepAnalysisInBed),
        @"HKCategoryValueSleepAnalysisAsleep":@(HKCategoryValueSleepAnalysisAsleep),
        @"HKCategoryValueSleepAnalysisAwake":@(HKCategoryValueSleepAnalysisAwake)
      }
    };

    NSDictionary * valueMap = map[type.identifier];
    if (!valueMap) {
      return HKCategoryValueNotApplicable;
    }
    return valueMap[categoryValue];
}

/**
 * Query HealthKit to get correlation data within a specified date range
 *
 * @param startDate
 * @param endDate
 * @param correlationTypeString
 * @param objects
 * @param metadata
 * @param error
 * @return
 */
- (HKCorrelation *)getHKCorrelationWithStartDate:(NSDate *)startDate
                                         endDate:(NSDate *)endDate
                           correlationTypeString:(NSString *)correlationTypeString
                                         objects:(NSSet *)objects
                                        metadata:(NSDictionary *)metadata
                                           error:(NSError **)error {
#ifdef HKPLUGIN_DEBUG
    NSLog(@"correlation type is %@", correlationTypeString);
#endif

    HKCorrelationType *correlationType = [HKCorrelationType correlationTypeForIdentifier:correlationTypeString];
    if (correlationType == nil) {
        if (error != nil) {
            *error = [NSError errorWithDomain:HKPluginError code:0 userInfo:@{NSLocalizedDescriptionKey: @"correlation type string is invalid"}];
        }

        return nil;
    }

    return [HKCorrelation correlationWithType:correlationType startDate:startDate endDate:endDate objects:objects metadata:metadata];
}

/**
 * Trigger a generic error callback
 *
 * @param message   *NSString
 * @param command   *CDVInvokedUrlCommand
 * @param delegate  id<CDVCommandDelegate>
 */
+ (void)triggerErrorCallbackWithMessage: (NSString *) message command: (CDVInvokedUrlCommand *) command delegate: (id<CDVCommandDelegate>) delegate {
    @autoreleasepool {
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:message];
        [delegate sendPluginResult:result callbackId:command.callbackId];
    }
}

@end

/**
 * Implementation of NSDictionary (RequiredKey)
 */
#pragma mark NSDictionary (RequiredKey)

@implementation NSDictionary (RequiredKey)

/**
 *
 * @param keys  *NSArray
 * @param error **NSError
 * @return      BOOL
 */
- (BOOL)hasAllRequiredKeys:(NSArray<NSString *> *)keys error:(NSError **)error {
    NSMutableArray *missing = [NSMutableArray arrayWithCapacity:0];

    for (NSString *key in keys) {
        if (self[key] == nil) {
            [missing addObject:key];
        }
    }

    if (missing.count == 0) {
        return YES;
    }

    if (error != nil) {
        NSString *errMsg = [NSString stringWithFormat:@"required value(s) -%@- is missing from dictionary %@", [missing componentsJoinedByString:@", "], [self description]];
        *error = [NSError errorWithDomain:HKPluginError code:0 userInfo:@{NSLocalizedDescriptionKey: errMsg}];
    }

    return NO;
}

@end

/**
 * Implementation of public interface
 * **************************************************************************************
 */
#pragma mark Public Interface

@implementation HealthKit

/**
 * Get shared health store
 *
 * @return *HKHealthStore
 */
+ (HKHealthStore *)sharedHealthStore {
    __strong static HKHealthStore *store = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        store = [[HKHealthStore alloc] init];
    });

    return store;
}

/**
 * Tell delegate whether or not health data is available
 *
 * @param command *CDVInvokedUrlCommand
 */
- (void)available:(CDVInvokedUrlCommand *)command {
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:[HKHealthStore isHealthDataAvailable]];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

/**
 * Request authorization for read and/or write permissions
 *
 * @param command *CDVInvokedUrlCommand
 */
- (void)requestAuthorization:(CDVInvokedUrlCommand *)command {
    NSMutableDictionary *args = command.arguments[0];

    // read types
    NSArray<NSString *> *readTypes = args[HKPluginKeyReadTypes];
    NSMutableSet *readDataTypes = [[NSMutableSet alloc] init];

    for (NSString *elem in readTypes) {
#ifdef HKPLUGIN_DEBUG
        NSLog(@"Requesting read permissions for %@", elem);
#endif
        HKObjectType *type = nil;

        if ([elem isEqual:@"HKWorkoutTypeIdentifier"]) {
            type = [HKObjectType workoutType];
        } else {
            type = [HealthKit getHKObjectType:elem];
        }

        if (type == nil) {
            CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"readTypes contains an invalid value"];
            [result setKeepCallbackAsBool:YES];
            [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
            // not returning deliberately to be future proof; other permissions are still asked
        } else {
            [readDataTypes addObject:type];
        }
    }

    // write types
    NSArray<NSString *> *writeTypes = args[HKPluginKeyWriteTypes];
    NSMutableSet *writeDataTypes = [[NSMutableSet alloc] init];

    for (NSString *elem in writeTypes) {
#ifdef HKPLUGIN_DEBUG
        NSLog(@"Requesting write permission for %@", elem);
#endif
        HKObjectType *type = nil;

        if ([elem isEqual:@"HKWorkoutTypeIdentifier"]) {
            type = [HKObjectType workoutType];
        } else {
            type = [HealthKit getHKObjectType:elem];
        }

        if (type == nil) {
            CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"writeTypes contains an invalid value"];
            [result setKeepCallbackAsBool:YES];
            [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
            // not returning deliberately to be future proof; other permissions are still asked
        } else {
            [writeDataTypes addObject:type];
        }
    }

    [[HealthKit sharedHealthStore] requestAuthorizationToShareTypes:writeDataTypes readTypes:readDataTypes completion:^(BOOL success, NSError *error) {
        if (success) {
            dispatch_sync(dispatch_get_main_queue(), ^{
                CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
                [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
            });
        } else {
            dispatch_sync(dispatch_get_main_queue(), ^{
                CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error.localizedDescription];
                [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
            });
        }
    }];
}

/**
 * Check the authorization status for a specified permission
 *
 * @param command *CDVInvokedUrlCommand
 */
- (void)checkAuthStatus:(CDVInvokedUrlCommand *)command {
    // If status = denied, prompt user to go to settings or the Health app
    // Note that read access is not reflected. We're not allowed to know
    // if a user grants/denies read access, *only* write access.
    NSMutableDictionary *args = command.arguments[0];
    NSString *checkType = args[HKPluginKeyType];
    HKObjectType *type;

    if ([checkType isEqual:@"HKWorkoutTypeIdentifier"]) {
        type = [HKObjectType workoutType];
    } else {
        type = [HealthKit getHKObjectType:checkType];
    }

    __block HealthKit *bSelf = self;
    [self checkAuthStatusWithCallbackId:command.callbackId forType:type andCompletion:^(CDVPluginResult *result, NSString *callbackId) {
        [bSelf.commandDelegate sendPluginResult:result callbackId:callbackId];
    }];
}

/**
 * Save workout data
 *
 * @param command *CDVInvokedUrlCommand
 */
- (void)saveWorkout:(CDVInvokedUrlCommand *)command {
    NSMutableDictionary *args = command.arguments[0];

    NSString *activityType = args[@"activityType"];
    NSString *quantityType = args[@"quantityType"]; // TODO verify this value

    HKWorkoutActivityType activityTypeEnum = [WorkoutActivityConversion convertStringToHKWorkoutActivityType:activityType];

    BOOL requestReadPermission = (args[@"requestReadPermission"] == nil || [args[@"requestReadPermission"] boolValue]);
    BOOL *cycling = (args[@"cycling"] == nil || [args[@"cycling"] boolValue]);

    // optional energy
    NSNumber *energy = args[@"energy"];
    NSString *energyUnit = args[@"energyUnit"];
    HKQuantity *nrOfEnergyUnits = nil;
    if (energy != nil && energy != (id) [NSNull null]) { // better safe than sorry
        HKUnit *preferredEnergyUnit = [HealthKit getUnit:energyUnit expected:@"HKEnergyUnit"];
        if (preferredEnergyUnit == nil) {
            [HealthKit triggerErrorCallbackWithMessage:@"invalid energyUnit is passed" command:command delegate:self.commandDelegate];
            return;
        }
        nrOfEnergyUnits = [HKQuantity quantityWithUnit:preferredEnergyUnit doubleValue:energy.doubleValue];
    }

    // optional distance
    NSNumber *distance = args[@"distance"];
    NSString *distanceUnit = args[@"distanceUnit"];
    HKQuantity *nrOfDistanceUnits = nil;
    if (distance != nil && distance != (id) [NSNull null]) { // better safe than sorry
        HKUnit *preferredDistanceUnit = [HealthKit getUnit:distanceUnit expected:@"HKLengthUnit"];
        if (preferredDistanceUnit == nil) {
            [HealthKit triggerErrorCallbackWithMessage:@"invalid distanceUnit is passed" command:command delegate:self.commandDelegate];
            return;
        }
        nrOfDistanceUnits = [HKQuantity quantityWithUnit:preferredDistanceUnit doubleValue:distance.doubleValue];
    }

    int duration = 0;
    NSDate *startDate = [NSDate dateWithTimeIntervalSince1970:[args[HKPluginKeyStartDate] doubleValue]];


    NSDate *endDate;
    if (args[@"duration"] != nil) {
        duration = [args[@"duration"] intValue];
        endDate = [NSDate dateWithTimeIntervalSince1970:startDate.timeIntervalSince1970 + duration];
    } else if (args[HKPluginKeyEndDate] != nil) {
        endDate = [NSDate dateWithTimeIntervalSince1970:[args[HKPluginKeyEndDate] doubleValue]];
    } else {
        [HealthKit triggerErrorCallbackWithMessage:@"no duration or endDate is set" command:command delegate:self.commandDelegate];
        return;
    }

    NSSet *types = [NSSet setWithObjects:
            [HKWorkoutType workoutType],
            [HKQuantityType quantityTypeForIdentifier:HKQuantityTypeIdentifierActiveEnergyBurned],
            [HKQuantityType quantityTypeForIdentifier:quantityType],
                    nil];
    [[HealthKit sharedHealthStore] requestAuthorizationToShareTypes:types readTypes:(requestReadPermission ? types : nil) completion:^(BOOL success_requestAuth, NSError *error) {
        __block HealthKit *bSelf = self;
        if (!success_requestAuth) {
            dispatch_sync(dispatch_get_main_queue(), ^{
                [HealthKit triggerErrorCallbackWithMessage:error.localizedDescription command:command delegate:bSelf.commandDelegate];
            });
        } else {
            HKWorkout *workout = [HKWorkout workoutWithActivityType:activityTypeEnum
                                                          startDate:startDate
                                                            endDate:endDate
                                                           duration:0 // the diff between start and end is used
                                                  totalEnergyBurned:nrOfEnergyUnits
                                                      totalDistance:nrOfDistanceUnits
                                                           metadata:nil]; // TODO find out if needed

            [[HealthKit sharedHealthStore] saveObject:workout withCompletion:^(BOOL success_save, NSError *innerError) {
                if (success_save) {
                    // now store the samples, so it shows up in the health app as well (pass this in as an option?)
                    if (energy != nil || distance != nil) {
                        HKQuantitySample *sampleActivity = nil;
                        if(cycling != nil && cycling){
                            sampleActivity = [HKQuantitySample quantitySampleWithType:[HKQuantityType quantityTypeForIdentifier:
                                            HKQuantityTypeIdentifierDistanceCycling]
                                                                                            quantity:nrOfDistanceUnits
                                                                                            startDate:startDate
                                                                                                endDate:endDate];
                        } else {
                            sampleActivity = [HKQuantitySample quantitySampleWithType:[HKQuantityType quantityTypeForIdentifier:
                                            HKQuantityTypeIdentifierDistanceWalkingRunning]
                                                                                            quantity:nrOfDistanceUnits
                                                                                            startDate:startDate
                                                                                                endDate:endDate];

                        }
                        HKQuantitySample *sampleCalories = [HKQuantitySample quantitySampleWithType:[HKQuantityType quantityTypeForIdentifier:
                                        HKQuantityTypeIdentifierActiveEnergyBurned]
                                                                                           quantity:nrOfEnergyUnits
                                                                                          startDate:startDate
                                                                                            endDate:endDate];
                        NSArray *samples = @[sampleActivity, sampleCalories];

                        [[HealthKit sharedHealthStore] addSamples:samples toWorkout:workout completion:^(BOOL success_addSamples, NSError *mostInnerError) {
                            if (success_addSamples) {
                                dispatch_sync(dispatch_get_main_queue(), ^{
                                    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
                                    [bSelf.commandDelegate sendPluginResult:result callbackId:command.callbackId];
                                });
                            } else {
                                dispatch_sync(dispatch_get_main_queue(), ^{
                                    [HealthKit triggerErrorCallbackWithMessage:mostInnerError.localizedDescription command:command delegate:bSelf.commandDelegate];
                                });
                            }
                        }];
                    } else {
                      // no samples, all OK then!
                      dispatch_sync(dispatch_get_main_queue(), ^{
                          CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
                          [bSelf.commandDelegate sendPluginResult:result callbackId:command.callbackId];
                      });
                    }
                } else {
                    dispatch_sync(dispatch_get_main_queue(), ^{
                        [HealthKit triggerErrorCallbackWithMessage:innerError.localizedDescription command:command delegate:bSelf.commandDelegate];
                    });
                }
            }];
        }
    }];
}

/**
 * Find workout data
 *
 * @param command *CDVInvokedUrlCommand
 */
- (void)findWorkouts:(CDVInvokedUrlCommand *)command {
    NSPredicate *workoutPredicate = nil;
    // TODO if a specific workouttype was passed, use that
    //  if (false) {
    //    workoutPredicate = [HKQuery predicateForWorkoutsWithWorkoutActivityType:HKWorkoutActivityTypeCycling];
    //  }

    NSSet *types = [NSSet setWithObjects:[HKWorkoutType workoutType], nil];
    [[HealthKit sharedHealthStore] requestAuthorizationToShareTypes:nil readTypes:types completion:^(BOOL success, NSError *error) {
        __block HealthKit *bSelf = self;
        if (!success) {
            dispatch_sync(dispatch_get_main_queue(), ^{
                [HealthKit triggerErrorCallbackWithMessage:error.localizedDescription command:command delegate:bSelf.commandDelegate];
            });
        } else {

            HKSampleQuery *query = [[HKSampleQuery alloc] initWithSampleType:[HKWorkoutType workoutType] predicate:workoutPredicate limit:HKObjectQueryNoLimit sortDescriptors:nil resultsHandler:^(HKSampleQuery *sampleQuery, NSArray *results, NSError *innerError) {
                if (innerError) {
                    dispatch_sync(dispatch_get_main_queue(), ^{
                        [HealthKit triggerErrorCallbackWithMessage:innerError.localizedDescription command:command delegate:bSelf.commandDelegate];
                    });
                } else {
                    NSMutableArray *finalResults = [[NSMutableArray alloc] initWithCapacity:results.count];

                    for (HKWorkout *workout in results) {
                        NSString *workoutActivity = [WorkoutActivityConversion convertHKWorkoutActivityTypeToString:workout.workoutActivityType];

                        // iOS 9 moves the source property to a collection of revisions
                        HKSource *source = nil;
                        if ([workout respondsToSelector:@selector(sourceRevision)]) {
                            source = [[workout valueForKey:@"sourceRevision"] valueForKey:@"source"];
                        } else {
                            //@TODO Update deprecated API call
                            source = workout.source;
                        }

                        // Parse totalEnergyBurned in kilocalories
                        double cals = [workout.totalEnergyBurned doubleValueForUnit:[HKUnit kilocalorieUnit]];
                        NSString *calories = [[NSNumber numberWithDouble:cals] stringValue];
                        
                        double totalDistance = [workout.totalDistance doubleValueForUnit:[HKUnit meterUnit]];
                        NSString *totalDistanceString = [NSString stringWithFormat:@"%f", (double) totalDistance];
                        
                        double totalFlightsClimbed = [workout.totalFlightsClimbed doubleValueForUnit:[HKUnit countUnit]];
                        NSString *totalFlightsClimbedString = [[NSNumber numberWithDouble:totalFlightsClimbed] stringValue];
                        
                        double totalSwimmingStrokeCount = [workout.totalSwimmingStrokeCount doubleValueForUnit:[HKUnit countUnit]];
                        NSString *totalSwimmingStrokeCountString = [[NSNumber numberWithDouble:totalSwimmingStrokeCount] stringValue];

                        NSMutableDictionary *entry = [
                                @{
                                        @"duration": @(workout.duration),
                                        @"durationUnit": @"seconds",
                                        HKPluginKeyStartDate: [HealthKit stringFromDate:workout.startDate],
                                        HKPluginKeyEndDate: [HealthKit stringFromDate:workout.endDate],
                                        @"distance": totalDistanceString,
                                        @"distanceUnit": @"meters",
                                        @"energy": calories,
                                        @"energyUnit": @"kcal",
                                        HKPluginKeySourceBundleId: source.bundleIdentifier,
                                        HKPluginKeySourceName: source.name,
                                        @"activityType": workoutActivity,
                                        @"measureName": HKWorkoutTypeIdentifier,
                                        @"UUID": [workout.UUID UUIDString],
                                        @"swimStrokeValue": totalSwimmingStrokeCountString,
                                        @"swimStrokeUnit": @"count",
                                        @"flightsClimbedValue": totalFlightsClimbedString,
                                        @"flightsClimbedUnit": @"count",
                                } mutableCopy
                        ];
                        
                        
                        entry[HKPluginKeySourceName] = workout.sourceRevision.source.name;
                        entry[HKPluginKeySourceBundleId] = workout.sourceRevision.source.bundleIdentifier;
                        entry[@"sourceProductType"] = workout.sourceRevision.productType;
                        entry[@"sourceVersion"] = workout.sourceRevision.version;
                        entry[@"sourceOSVersionMajor"] = [NSNumber numberWithInteger:workout.sourceRevision.operatingSystemVersion.majorVersion];
                        entry[@"sourceOSVersionMinor"] = [NSNumber numberWithInteger:workout.sourceRevision.operatingSystemVersion.minorVersion];
                        entry[@"sourceOSVersionPatch"] = [NSNumber numberWithInteger:workout.sourceRevision.operatingSystemVersion.patchVersion];
                        
                        entry[@"deviceName"] = workout.device.name;
                        entry[@"deviceModel"] = workout.device.model;
                        entry[@"deviceManufacturer"] = workout.device.manufacturer;
                        entry[@"deviceLocalIdentifier"] = workout.device.localIdentifier;
                        entry[@"deviceHardwareVersion"] = workout.device.hardwareVersion;
                        entry[@"deviceSoftwareVersion"] = workout.device.softwareVersion;
                        entry[@"deviceFirmwareVersion"] = workout.device.firmwareVersion;
                        entry[@"FDA_UDI"] = workout.device.UDIDeviceIdentifier;
                        entry[HKPluginKeyMetadata] = [@{} mutableCopy];
                        
                        if (workout.metadata != nil && [workout.metadata isKindOfClass:[NSDictionary class]]) {
                            for (id key in workout.metadata) {
                                NSMutableDictionary *metadata = [@{} mutableCopy];
                                [metadata setObject:[workout.metadata objectForKey:key] forKey:key];
                                
                                if ([NSJSONSerialization isValidJSONObject:metadata]) {
                                    [entry[HKPluginKeyMetadata] setObject:[workout.metadata objectForKey:key] forKey:key];
                                } else {
                                    if ([key isEqual: HKMetadataKeyWeatherTemperature]) {
                                        double temperatureCelsius = [[workout.metadata objectForKey:key] doubleValueForUnit: [HKUnit degreeCelsiusUnit]];
                                        NSString *temperatureCelsiusString = [[NSNumber numberWithDouble:temperatureCelsius] stringValue];
                                        [entry[HKPluginKeyMetadata] setObject:temperatureCelsiusString forKey:@"HKWeatherTemperatureCelsius"];
                                        
                                        double temperatureFahrenheit = [[workout.metadata objectForKey:key] doubleValueForUnit: [HKUnit degreeFahrenheitUnit]];
                                        NSString *temperatureFahrenheitString = [[NSNumber numberWithDouble:temperatureFahrenheit] stringValue];
                                        [entry[HKPluginKeyMetadata] setObject:temperatureFahrenheitString forKey:@"HKWeatherTemperatureFahrenheit"];
                                        
                                        double temperature = [[workout.metadata objectForKey:key] doubleValueForUnit: [HKUnit degreeCelsiusUnit]];
                                        NSString *temperatureString = [[NSNumber numberWithDouble:temperature] stringValue];
                                        [entry[HKPluginKeyMetadata] setObject:temperatureString forKey:@"HKWeatherTemperature"];
                                        [entry[HKPluginKeyMetadata] setObject:[[HKUnit degreeCelsiusUnit] unitString] forKey:@"HKWeatherTemperatureUnit"];
                                    }
                                    if ([key isEqual: HKMetadataKeyWeatherHumidity]) {
                                        double humidity = [[workout.metadata objectForKey:key] doubleValueForUnit: [HKUnit percentUnit]];
                                        NSString *humidityString = [[NSNumber numberWithDouble:humidity] stringValue];
                                        [entry[HKPluginKeyMetadata] setObject:humidityString forKey:HKMetadataKeyWeatherHumidity];
                                        [entry[HKPluginKeyMetadata] setObject:[[HKUnit percentUnit] unitString] forKey:@"HKWeatherHumidityUnit"];
                                    }
                                    if (@available(iOS 11.2, *)) {
                                        if ([key isEqual: HKMetadataKeyElevationAscended]) {
                                            double ascended = [[workout.metadata objectForKey:key] doubleValueForUnit: [HKUnit meterUnit]];
                                            NSString *ascendedString = [[NSNumber numberWithDouble:ascended] stringValue];
                                            [entry[HKPluginKeyMetadata] setObject:ascendedString forKey:HKMetadataKeyElevationAscended];
                                            [entry[HKPluginKeyMetadata] setObject:[[HKUnit meterUnit] unitString] forKey:@"HKElevationAscendedUnit"];
                                        }
                                        if ([key isEqual: HKMetadataKeyElevationDescended]) {
                                            double descended = [[workout.metadata objectForKey:key] doubleValueForUnit: [HKUnit meterUnit]];
                                            NSString *descendedString = [[NSNumber numberWithDouble:descended] stringValue];
                                            [entry[HKPluginKeyMetadata] setObject:descendedString forKey:HKMetadataKeyElevationDescended];
                                            [entry[HKPluginKeyMetadata] setObject:[[HKUnit meterUnit] unitString] forKey:@"HKElevationDescendedUnit"];
                                        }
                                    }
                                    if (@available(iOS 13.0, *)) {
                                        if ([key isEqual: HKMetadataKeyAverageMETs]) {
                                            double mets = [[workout.metadata objectForKey:key] doubleValueForUnit: [HKUnit unitFromString:@"kcal/(kg*hr)"]];
                                            NSString *metsString = [[NSNumber numberWithDouble:mets] stringValue];
                                            [entry[HKPluginKeyMetadata] setObject:metsString forKey:HKMetadataKeyAverageMETs];
                                            [entry[HKPluginKeyMetadata] setObject:[[HKUnit unitFromString:@"kcal/(kg*hr)"] unitString] forKey:@"HKAverageMETsUnit"];
                                        }
                                    }
                                }
                            }
                        }
                        
                        NSMutableArray *events = [[NSMutableArray alloc] initWithCapacity:workout.workoutEvents.count];
                        for (HKWorkoutEvent *event in workout.workoutEvents) {
                            NSString *eventType = [[NSNumber numberWithDouble:event.type] stringValue];
                            NSString *duration = [[NSNumber numberWithDouble:event.dateInterval.duration] stringValue];
                            NSString *startDate = [[NSNumber numberWithDouble:[event.dateInterval.startDate timeIntervalSince1970]] stringValue];
                            NSString *endDate = [[NSNumber numberWithDouble:[event.dateInterval.endDate timeIntervalSince1970]] stringValue];
                            NSMutableDictionary *evententry = [@{
                                @"startDate": startDate,
                                @"endDate": endDate,
                                @"duration": duration,
                                @"type": eventType,
                            } mutableCopy];
                            evententry[HKPluginKeyMetadata] = [@{} mutableCopy];

                            if (event.metadata != nil && [event.metadata isKindOfClass:[NSDictionary class]]) {
                                for (id key in event.metadata) {
                                    NSMutableDictionary *eventmetadata = [@{} mutableCopy];
                                    [eventmetadata setObject:[event.metadata objectForKey:key] forKey:key];
                                    
                                    if ([NSJSONSerialization isValidJSONObject:eventmetadata]) {
                                        [evententry[HKPluginKeyMetadata] setObject:[event.metadata objectForKey:key] forKey:key];
                                    }
                                }
                            }
                            
                            [events addObject:evententry];
                        }
                        entry[@"workoutEvents"] = events;
                        
                        [finalResults addObject:entry];
                    }

                    dispatch_sync(dispatch_get_main_queue(), ^{
                        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:finalResults];
                        [bSelf.commandDelegate sendPluginResult:result callbackId:command.callbackId];
                    });
                }
            }];
            [[HealthKit sharedHealthStore] executeQuery:query];
        }
    }];
}

/**
 * Save weight data
 *
 * @param command *CDVInvokedUrlCommand
 */
- (void)saveWeight:(CDVInvokedUrlCommand *)command {
    NSMutableDictionary *args = command.arguments[0];
    NSString *unit = args[HKPluginKeyUnit];
    NSNumber *amount = args[HKPluginKeyAmount];
    NSDate *date = [NSDate dateWithTimeIntervalSince1970:[args[@"date"] doubleValue]];
    BOOL requestReadPermission = (args[@"requestReadPermission"] == nil || [args[@"requestReadPermission"] boolValue]);

    if (amount == nil) {
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"no amount was set"];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        return;
    }

    HKUnit *preferredUnit = [HealthKit getUnit:unit expected:@"HKMassUnit"];
    if (preferredUnit == nil) {
        [HealthKit triggerErrorCallbackWithMessage:@"invalid unit is passed" command:command delegate:self.commandDelegate];
        return;
    }

    HKQuantityType *weightType = [HKQuantityType quantityTypeForIdentifier:HKQuantityTypeIdentifierBodyMass];
    NSSet *requestTypes = [NSSet setWithObjects:weightType, nil];
    __block HealthKit *bSelf = self;
    [[HealthKit sharedHealthStore] requestAuthorizationToShareTypes:requestTypes readTypes:(requestReadPermission ? requestTypes : nil) completion:^(BOOL success, NSError *error) {
        if (success) {
            HKQuantity *weightQuantity = [HKQuantity quantityWithUnit:preferredUnit doubleValue:[amount doubleValue]];
            HKQuantitySample *weightSample = [HKQuantitySample quantitySampleWithType:weightType quantity:weightQuantity startDate:date endDate:date];
            [[HealthKit sharedHealthStore] saveObject:weightSample withCompletion:^(BOOL success_save, NSError *errorInner) {
                if (success_save) {
                    dispatch_sync(dispatch_get_main_queue(), ^{
                        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
                        [bSelf.commandDelegate sendPluginResult:result callbackId:command.callbackId];
                    });
                } else {
                    dispatch_sync(dispatch_get_main_queue(), ^{
                        [HealthKit triggerErrorCallbackWithMessage:errorInner.localizedDescription command:command delegate:bSelf.commandDelegate];
                    });
                }
            }];
        } else {
            dispatch_sync(dispatch_get_main_queue(), ^{
                [HealthKit triggerErrorCallbackWithMessage:error.localizedDescription command:command delegate:bSelf.commandDelegate];
            });
        }
    }];
}

/**
 * Read weight data
 *
 * @param command *CDVInvokedUrlCommand
 */
- (void)readWeight:(CDVInvokedUrlCommand *)command {
    NSDictionary *args = command.arguments[0];
    NSString *unit = args[HKPluginKeyUnit];
    BOOL requestWritePermission = (args[@"requestWritePermission"] == nil || [args[@"requestWritePermission"] boolValue]);

    HKUnit *preferredUnit = [HealthKit getUnit:unit expected:@"HKMassUnit"];
    if (preferredUnit == nil) {
        [HealthKit triggerErrorCallbackWithMessage:@"invalid unit is passed" command:command delegate:self.commandDelegate];
        return;
    }

    // Query to get the user's latest weight, if it exists.
    HKQuantityType *weightType = [HKQuantityType quantityTypeForIdentifier:HKQuantityTypeIdentifierBodyMass];
    NSSet *requestTypes = [NSSet setWithObjects:weightType, nil];
    // always ask for read and write permission if the app uses both, because granting read will remove write for the same type :(
    [[HealthKit sharedHealthStore] requestAuthorizationToShareTypes:(requestWritePermission ? requestTypes : nil) readTypes:requestTypes completion:^(BOOL success, NSError *error) {
        __block HealthKit *bSelf = self;
        if (success) {
            [[HealthKit sharedHealthStore] aapl_mostRecentQuantitySampleOfType:weightType predicate:nil completion:^(HKQuantity *mostRecentQuantity, NSDate *mostRecentDate, NSError *errorInner) {
                if (mostRecentQuantity) {
                    double usersWeight = [mostRecentQuantity doubleValueForUnit:preferredUnit];
                    NSMutableDictionary *entry = [
                            @{
                                    HKPluginKeyValue: @(usersWeight),
                                    HKPluginKeyUnit: unit,
                                    @"date": [HealthKit stringFromDate:mostRecentDate]
                            } mutableCopy
                    ];

                    //@TODO formerly dispatch_async
                    dispatch_sync(dispatch_get_main_queue(), ^{
                        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:entry];
                        [bSelf.commandDelegate sendPluginResult:result callbackId:command.callbackId];
                    });
                } else {
                    //@TODO formerly dispatch_async
                    dispatch_sync(dispatch_get_main_queue(), ^{
                        NSString *errorDescription = ((errorInner.localizedDescription == nil) ? @"no data" : errorInner.localizedDescription);
                        [HealthKit triggerErrorCallbackWithMessage:errorDescription command:command delegate:bSelf.commandDelegate];
                    });
                }
            }];
        } else {
            dispatch_sync(dispatch_get_main_queue(), ^{
                [HealthKit triggerErrorCallbackWithMessage:error.localizedDescription command:command delegate:bSelf.commandDelegate];
            });
        }
    }];
}

/**
 * Save height data
 *
 * @param command *CDVInvokedUrlCommand
 */
- (void)saveHeight:(CDVInvokedUrlCommand *)command {
    NSDictionary *args = command.arguments[0];
    NSString *unit = args[HKPluginKeyUnit];
    NSNumber *amount = args[HKPluginKeyAmount];
    NSDate *date = [NSDate dateWithTimeIntervalSince1970:[args[@"date"] doubleValue]];
    BOOL requestReadPermission = (args[@"requestReadPermission"] == nil || [args[@"requestReadPermission"] boolValue]);

    if (amount == nil) {
        [HealthKit triggerErrorCallbackWithMessage:@"no amount is set" command:command delegate:self.commandDelegate];
        return;
    }

    HKUnit *preferredUnit = [HealthKit getUnit:unit expected:@"HKLengthUnit"];
    if (preferredUnit == nil) {
        [HealthKit triggerErrorCallbackWithMessage:@"invalid unit is passed" command:command delegate:self.commandDelegate];
        return;
    }

    HKQuantityType *heightType = [HKQuantityType quantityTypeForIdentifier:HKQuantityTypeIdentifierHeight];
    NSSet *requestTypes = [NSSet setWithObjects:heightType, nil];
    [[HealthKit sharedHealthStore] requestAuthorizationToShareTypes:requestTypes readTypes:(requestReadPermission ? requestTypes : nil) completion:^(BOOL success_requestAuth, NSError *error) {
        __block HealthKit *bSelf = self;
        if (success_requestAuth) {
            HKQuantity *heightQuantity = [HKQuantity quantityWithUnit:preferredUnit doubleValue:[amount doubleValue]];
            HKQuantitySample *heightSample = [HKQuantitySample quantitySampleWithType:heightType quantity:heightQuantity startDate:date endDate:date];
            [[HealthKit sharedHealthStore] saveObject:heightSample withCompletion:^(BOOL success_save, NSError *innerError) {
                if (success_save) {
                    dispatch_sync(dispatch_get_main_queue(), ^{
                        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
                        [bSelf.commandDelegate sendPluginResult:result callbackId:command.callbackId];
                    });
                } else {
                    dispatch_sync(dispatch_get_main_queue(), ^{
                        [HealthKit triggerErrorCallbackWithMessage:innerError.localizedDescription command:command delegate:bSelf.commandDelegate];
                    });
                }
            }];
        } else {
            dispatch_sync(dispatch_get_main_queue(), ^{
                [HealthKit triggerErrorCallbackWithMessage:error.localizedDescription command:command delegate:bSelf.commandDelegate];
            });
        }
    }];
}

/**
 * Read height data
 *
 * @param command *CDVInvokedUrlCommand
 */
- (void)readHeight:(CDVInvokedUrlCommand *)command {
    NSDictionary *args = command.arguments[0];
    NSString *unit = args[HKPluginKeyUnit];
    BOOL requestWritePermission = (args[@"requestWritePermission"] == nil || [args[@"requestWritePermission"] boolValue]);

    HKUnit *preferredUnit = [HealthKit getUnit:unit expected:@"HKLengthUnit"];
    if (preferredUnit == nil) {
        [HealthKit triggerErrorCallbackWithMessage:@"invalid unit is passed" command:command delegate:self.commandDelegate];
        return;
    }

    // Query to get the user's latest height, if it exists.
    HKQuantityType *heightType = [HKQuantityType quantityTypeForIdentifier:HKQuantityTypeIdentifierHeight];
    NSSet *requestTypes = [NSSet setWithObjects:heightType, nil];
    // always ask for read and write permission if the app uses both, because granting read will remove write for the same type :(
    [[HealthKit sharedHealthStore] requestAuthorizationToShareTypes:(requestWritePermission ? requestTypes : nil) readTypes:requestTypes completion:^(BOOL success, NSError *error) {
        __block HealthKit *bSelf = self;
        if (success) {
            [[HealthKit sharedHealthStore] aapl_mostRecentQuantitySampleOfType:heightType predicate:nil completion:^(HKQuantity *mostRecentQuantity, NSDate *mostRecentDate, NSError *errorInner) { // TODO use
                if (mostRecentQuantity) {
                    double usersHeight = [mostRecentQuantity doubleValueForUnit:preferredUnit];
                    NSMutableDictionary *entry = [
                            @{
                                    HKPluginKeyValue: @(usersHeight),
                                    HKPluginKeyUnit: unit,
                                    @"date": [HealthKit stringFromDate:mostRecentDate]
                            } mutableCopy
                    ];

                    //@TODO formerly dispatch_async
                    dispatch_sync(dispatch_get_main_queue(), ^{
                        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:entry];
                        [bSelf.commandDelegate sendPluginResult:result callbackId:command.callbackId];
                    });
                } else {
                    //@TODO formerly dispatch_async
                    dispatch_sync(dispatch_get_main_queue(), ^{
                        NSString *errorDescritption = ((errorInner.localizedDescription == nil) ? @"no data" : errorInner.localizedDescription);
                        [HealthKit triggerErrorCallbackWithMessage:errorDescritption command:command delegate:bSelf.commandDelegate];
                    });
                }
            }];
        } else {
            dispatch_sync(dispatch_get_main_queue(), ^{
                [HealthKit triggerErrorCallbackWithMessage:error.localizedDescription command:command delegate:bSelf.commandDelegate];
            });
        }
    }];
}

/**
 * Read gender data
 *
 * @param command *CDVInvokedUrlCommand
 */
- (void)readGender:(CDVInvokedUrlCommand *)command {
    HKCharacteristicType *genderType = [HKObjectType characteristicTypeForIdentifier:HKCharacteristicTypeIdentifierBiologicalSex];
    [[HealthKit sharedHealthStore] requestAuthorizationToShareTypes:nil readTypes:[NSSet setWithObjects:genderType, nil] completion:^(BOOL success, NSError *error) {
        __block HealthKit *bSelf = self;
        if (success) {
            HKBiologicalSexObject *sex = [[HealthKit sharedHealthStore] biologicalSexWithError:&error];
            if (sex != nil) {

                NSString *gender = nil;
                switch (sex.biologicalSex) {
                    case HKBiologicalSexMale:
                        gender = @"male";
                        break;
                    case HKBiologicalSexFemale:
                        gender = @"female";
                        break;
                    case HKBiologicalSexOther:
                        gender = @"other";
                        break;
                    default:
                        gender = @"unknown";
                }

                CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:gender];
                [bSelf.commandDelegate sendPluginResult:result callbackId:command.callbackId];
            } else {
                [HealthKit triggerErrorCallbackWithMessage:error.localizedDescription command:command delegate:bSelf.commandDelegate];
            }
        }
    }];
}

/**
 * Read Fitzpatrick Skin Type Data
 *
 * @param command *CDVInvokedUrlCommand
 */
- (void)readFitzpatrickSkinType:(CDVInvokedUrlCommand *)command {
    // fp skintype is available since iOS 9, so we need to check it
    if (![[HealthKit sharedHealthStore] respondsToSelector:@selector(fitzpatrickSkinTypeWithError:)]) {
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"not available on this device"];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        return;
    }

    HKCharacteristicType *type = [HKObjectType characteristicTypeForIdentifier:HKCharacteristicTypeIdentifierFitzpatrickSkinType];
    [[HealthKit sharedHealthStore] requestAuthorizationToShareTypes:nil readTypes:[NSSet setWithObjects:type, nil] completion:^(BOOL success, NSError *error) {
        __block HealthKit *bSelf = self;
        if (success) {
            HKFitzpatrickSkinTypeObject *skinType = [[HealthKit sharedHealthStore] fitzpatrickSkinTypeWithError:&error];
            if (skinType != nil) {

                NSString *skin = nil;
                switch (skinType.skinType) {
                    case HKFitzpatrickSkinTypeI:
                        skin = @"I";
                        break;
                    case HKFitzpatrickSkinTypeII:
                        skin = @"II";
                        break;
                    case HKFitzpatrickSkinTypeIII:
                        skin = @"III";
                        break;
                    case HKFitzpatrickSkinTypeIV:
                        skin = @"IV";
                        break;
                    case HKFitzpatrickSkinTypeV:
                        skin = @"V";
                        break;
                    case HKFitzpatrickSkinTypeVI:
                        skin = @"VI";
                        break;
                    default:
                        skin = @"unknown";
                }

                CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:skin];
                [bSelf.commandDelegate sendPluginResult:result callbackId:command.callbackId];
            } else {
                [HealthKit triggerErrorCallbackWithMessage:error.localizedDescription command:command delegate:bSelf.commandDelegate];
            }
        }
    }];
}

/**
 * Read blood type data
 *
 * @param command *CDVInvokedUrlCommand
 */
- (void)readBloodType:(CDVInvokedUrlCommand *)command {
    HKCharacteristicType *bloodType = [HKObjectType characteristicTypeForIdentifier:HKCharacteristicTypeIdentifierBloodType];
    [[HealthKit sharedHealthStore] requestAuthorizationToShareTypes:nil readTypes:[NSSet setWithObjects:bloodType, nil] completion:^(BOOL success, NSError *error) {
        __block HealthKit *bSelf = self;
        if (success) {
            HKBloodTypeObject *innerBloodType = [[HealthKit sharedHealthStore] bloodTypeWithError:&error];
            if (innerBloodType != nil) {
                NSString *bt = nil;

                switch (innerBloodType.bloodType) {
                    case HKBloodTypeAPositive:
                        bt = @"A+";
                        break;
                    case HKBloodTypeANegative:
                        bt = @"A-";
                        break;
                    case HKBloodTypeBPositive:
                        bt = @"B+";
                        break;
                    case HKBloodTypeBNegative:
                        bt = @"B-";
                        break;
                    case HKBloodTypeABPositive:
                        bt = @"AB+";
                        break;
                    case HKBloodTypeABNegative:
                        bt = @"AB-";
                        break;
                    case HKBloodTypeOPositive:
                        bt = @"O+";
                        break;
                    case HKBloodTypeONegative:
                        bt = @"O-";
                        break;
                    default:
                        bt = @"unknown";
                }

                CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:bt];
                [bSelf.commandDelegate sendPluginResult:result callbackId:command.callbackId];
            } else {
                [HealthKit triggerErrorCallbackWithMessage:error.localizedDescription command:command delegate:bSelf.commandDelegate];
            }
        }
    }];
}

/**
 * Read date of birth data
 *
 * @param command *CDVInvokedUrlCommand
 */
- (void)readDateOfBirth:(CDVInvokedUrlCommand *)command {
    HKCharacteristicType *birthdayType = [HKObjectType characteristicTypeForIdentifier:HKCharacteristicTypeIdentifierDateOfBirth];
    [[HealthKit sharedHealthStore] requestAuthorizationToShareTypes:nil readTypes:[NSSet setWithObjects:birthdayType, nil] completion:^(BOOL success, NSError *error) {
        __block HealthKit *bSelf = self;
        if (success) {
            NSDate *dateOfBirth = [[HealthKit sharedHealthStore] dateOfBirthWithError:&error];
            if (dateOfBirth) {
                CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:[HealthKit stringFromDate:dateOfBirth]];
                [bSelf.commandDelegate sendPluginResult:result callbackId:command.callbackId];
            } else {
                [HealthKit triggerErrorCallbackWithMessage:error.localizedDescription command:command delegate:bSelf.commandDelegate];
            }
        }
    }];
}

/**
 * Monitor a specified sample type
 *
 * @param command *CDVInvokedUrlCommand
 */
- (void)monitorSampleType:(CDVInvokedUrlCommand *)command {
    NSDictionary *args = command.arguments[0];
    NSString *sampleTypeString = args[HKPluginKeySampleType];
    HKSampleType *type = [HealthKit getHKSampleType:sampleTypeString];
    HKUpdateFrequency updateFrequency = HKUpdateFrequencyImmediate;
    if (type == nil) {
        [HealthKit triggerErrorCallbackWithMessage:@"sampleType was invalid" command:command delegate:self.commandDelegate];
        return;
    }

    // TODO use this an an anchor for an achored query
    //__block int *anchor = 0;
#ifdef HKPLUGIN_DEBUG
    NSLog(@"Setting up ObserverQuery");
#endif

    HKObserverQuery *query;
    query = [[HKObserverQuery alloc] initWithSampleType:type
                                              predicate:nil
                                          updateHandler:^(HKObserverQuery *observerQuery,
                                                  HKObserverQueryCompletionHandler handler,
                                                  NSError *error) {
                                              __block HealthKit *bSelf = self;
                                              if (error) {
                                                  handler();
                                                  dispatch_sync(dispatch_get_main_queue(), ^{
                                                      [HealthKit triggerErrorCallbackWithMessage:error.localizedDescription command:command delegate:bSelf.commandDelegate];
                                                  });
                                              } else {
                                                  handler();
#ifdef HKPLUGIN_DEBUG
                                                  NSLog(@"HealthKit plugin received a monitorSampleType, passing it to JS.");
#endif
                                                  // TODO using a anchored qery to return the new and updated values.
                                                  // Until then use querySampleType({limit=1, ascending="T", endDate=new Date()}) to return the last result

                                                  // Issue #47: commented this block since it resulted in callbacks not being delivered while the app was in the background
                                                  //dispatch_sync(dispatch_get_main_queue(), ^{
                                                  CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:sampleTypeString];
                                                  [result setKeepCallbackAsBool:YES];
                                                  [bSelf.commandDelegate sendPluginResult:result callbackId:command.callbackId];
                                                  //});
                                              }
                                          }];

    // Make sure we get the updated immediately
    [[HealthKit sharedHealthStore] enableBackgroundDeliveryForType:type frequency:updateFrequency withCompletion:^(BOOL success, NSError *error) {
#ifdef HKPLUGIN_DEBUG
        if (success) {
            NSLog(@"Background devliery enabled %@", sampleTypeString);
        } else {
            NSLog(@"Background delivery not enabled for %@ because of %@", sampleTypeString, error);
        }
        NSLog(@"Executing ObserverQuery");
#endif
        [[HealthKit sharedHealthStore] executeQuery:query];
        // TODO provide some kind of callback to stop monitoring this value, store the query in some kind of WeakHashSet equilavent?
    }];
};

/**
 * Get the sum of a specified quantity type
 *
 * @param command *CDVInvokedUrlCommand
 */
- (void)sumQuantityType:(CDVInvokedUrlCommand *)command {
    NSDictionary *args = command.arguments[0];

    NSDate *startDate = [NSDate dateWithTimeIntervalSince1970:[args[HKPluginKeyStartDate] longValue]];
    NSDate *endDate = [NSDate dateWithTimeIntervalSince1970:[args[HKPluginKeyEndDate] longValue]];
    NSString *sampleTypeString = args[HKPluginKeySampleType];
    NSString *unitString = args[HKPluginKeyUnit];
    HKQuantityType *type = [HKObjectType quantityTypeForIdentifier:sampleTypeString];


    if (type == nil) {
        [HealthKit triggerErrorCallbackWithMessage:@"sampleType was invalid" command:command delegate:self.commandDelegate];
        return;
    }

    NSPredicate *predicate = [HKQuery predicateForSamplesWithStartDate:startDate endDate:endDate options:HKQueryOptionStrictStartDate];
    HKStatisticsOptions sumOptions = HKStatisticsOptionCumulativeSum;
    HKStatisticsQuery *query;
    HKUnit *unit = ((unitString != nil) ? [HKUnit unitFromString:unitString] : [HKUnit countUnit]);
    query = [[HKStatisticsQuery alloc] initWithQuantityType:type
                                    quantitySamplePredicate:predicate
                                                    options:sumOptions
                                          completionHandler:^(HKStatisticsQuery *statisticsQuery,
                                                  HKStatistics *result,
                                                  NSError *error) {
                                              HKQuantity *sum = [result sumQuantity];
                                              CDVPluginResult *response = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDouble:[sum doubleValueForUnit:unit]];
                                              [self.commandDelegate sendPluginResult:response callbackId:command.callbackId];
                                          }];

    [[HealthKit sharedHealthStore] executeQuery:query];
}


- (void)queryActivitySummary:(CDVInvokedUrlCommand *)command {
    NSMutableSet *readDataTypes = [[NSMutableSet alloc] init];
    [readDataTypes addObject:[HKObjectType activitySummaryType]];
    [[HealthKit sharedHealthStore] requestAuthorizationToShareTypes:nil readTypes:readDataTypes completion:^(BOOL success, NSError *error) {
        __block HealthKit *bSelf = self;
        if (success) {
            NSDictionary *args = command.arguments[0];
            NSDate *startDate = [NSDate dateWithTimeIntervalSince1970:[args[HKPluginKeyStartDate] longValue]];
            NSDate *endDate = [NSDate dateWithTimeIntervalSince1970:[args[HKPluginKeyEndDate] longValue]];
            NSPredicate *predicate = [HKQuery predicateForSamplesWithStartDate:startDate endDate:endDate options:HKQueryOptionStrictStartDate];
            HKActivitySummaryQuery *query = [[HKActivitySummaryQuery alloc] initWithPredicate:predicate resultsHandler:^(HKActivitySummaryQuery *sampleQuery, NSArray *results, NSError *innerError) {
                if (innerError != nil) {
                    dispatch_sync(dispatch_get_main_queue(), ^{
                        [HealthKit triggerErrorCallbackWithMessage:error.localizedDescription command:command delegate:bSelf.commandDelegate];
                    });
                } else {

                    NSMutableArray *finalResults = [[NSMutableArray alloc] initWithCapacity:results.count];
                    
                    for (HKActivitySummary *sample in results) {
                        
                        NSMutableDictionary *entry = [NSMutableDictionary dictionary];
                        NSDateComponents *startSample = [sample dateComponentsForCalendar:[NSCalendar currentCalendar]];
                        NSDate *date = startSample.date;
                        
                        entry[@"startDate"] = [HealthKit stringFromDate:date];
                        entry[@"activeEnergy"] = @([sample.activeEnergyBurned doubleValueForUnit:[HKUnit kilocalorieUnit]]);
                        entry[@"activeEnergyGoal"] = @([sample.activeEnergyBurnedGoal doubleValueForUnit:[HKUnit kilocalorieUnit]]);
                        
                        entry[@"appleStandHours"] = @([sample.appleStandHours doubleValueForUnit:[HKUnit countUnit]]);
                        entry[@"appleStandHoursGoal"] = @([sample.appleStandHoursGoal doubleValueForUnit:[HKUnit countUnit]]);
                        
                        entry[@"appleExerciseTime"] = @([sample.appleExerciseTime doubleValueForUnit:[HKUnit secondUnit]]);
                        entry[@"appleExerciseTimeGoal"] = @([sample.appleExerciseTimeGoal doubleValueForUnit:[HKUnit secondUnit]]);
                        
                        if (@available(iOS 14.0, watchOS 7.0, *)) {
                            entry[@"appleMoveTime"] = @([sample.appleMoveTime doubleValueForUnit:[HKUnit minuteUnit]]);
                            entry[@"appleMoveTimeGoal"] = @([sample.appleMoveTimeGoal doubleValueForUnit:[HKUnit minuteUnit]]);
                        }
                        
                        [finalResults addObject:entry];
                    }
                    
                    dispatch_sync(dispatch_get_main_queue(), ^{
                        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:finalResults];
                        [bSelf.commandDelegate sendPluginResult:result callbackId:command.callbackId];
                    });
                }
            }];
            [[HealthKit sharedHealthStore] executeQuery:query];
        } else {
            dispatch_sync(dispatch_get_main_queue(), ^{
                CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error.localizedDescription];
                [bSelf.commandDelegate sendPluginResult:result callbackId:command.callbackId];
            });
        }
    }];


}

/**
 * Query a specified sample type
 *
 * @param command *CDVInvokedUrlCommand
 */
- (void)querySampleType:(CDVInvokedUrlCommand *)command {
    NSDictionary *args = command.arguments[0];
    NSDate *startDate = [NSDate dateWithTimeIntervalSince1970:[args[HKPluginKeyStartDate] longValue]];
    NSDate *endDate = [NSDate dateWithTimeIntervalSince1970:[args[HKPluginKeyEndDate] longValue]];
    NSString *sampleTypeString = args[HKPluginKeySampleType];
    NSString *unitString = args[HKPluginKeyUnit];
    NSUInteger limit = ((args[@"limit"] != nil) ? [args[@"limit"] unsignedIntegerValue] : 1000);
    BOOL ascending = (args[@"ascending"] != nil && [args[@"ascending"] boolValue]);
    
    HKSampleType *type = [HealthKit getHKSampleType:sampleTypeString];
    if (type == nil) {
        [HealthKit triggerErrorCallbackWithMessage:@"sampleType was invalid" command:command delegate:self.commandDelegate];
        return;
    }
    HKUnit *unit = nil;
    if (unitString != nil) {
        if ([unitString isEqualToString:@"mmol/L"]) {
            // @see https://stackoverflow.com/a/30196642/1214598
            unit = [[HKUnit moleUnitWithMetricPrefix:HKMetricPrefixMilli molarMass:HKUnitMolarMassBloodGlucose] unitDividedByUnit:[HKUnit literUnit]];
        } else {
            // issue 51
            // @see https://github.com/Telerik-Verified-Plugins/HealthKit/issues/51
            if ([unitString isEqualToString:@"percent"]) {
                unitString = @"%";
            }
            unit = [HKUnit unitFromString:unitString];
        }
    }
    // TODO check that unit is compatible with sampleType if sample type of HKQuantityType
    NSPredicate *predicate = [HKQuery predicateForSamplesWithStartDate:startDate endDate:endDate options:HKQueryOptionStrictStartDate];

    NSSet *requestTypes = [NSSet setWithObjects:type, nil];
    [[HealthKit sharedHealthStore] requestAuthorizationToShareTypes:nil readTypes:requestTypes completion:^(BOOL success, NSError *error) {
        __block HealthKit *bSelf = self;
        if (success) {
            NSString *endKey = HKSampleSortIdentifierEndDate;
            NSSortDescriptor *endDateSort = [NSSortDescriptor sortDescriptorWithKey:endKey ascending:ascending];
            HKSampleQuery *query = [[HKSampleQuery alloc] initWithSampleType:type
                                                                   predicate:predicate
                                                                       limit:limit
                                                             sortDescriptors:@[endDateSort]
                                                              resultsHandler:^(HKSampleQuery *sampleQuery,
                                                                      NSArray *results,
                                                                      NSError *innerError) {
                                                                  if (innerError != nil) {
                                                                      dispatch_sync(dispatch_get_main_queue(), ^{
                                                                          [HealthKit triggerErrorCallbackWithMessage:innerError.localizedDescription command:command delegate:bSelf.commandDelegate];
                                                                      });
                                                                  } else {
                                                                      NSMutableArray *finalResults = [[NSMutableArray alloc] initWithCapacity:results.count];

                                                                      for (HKSample *sample in results) {

                                                                          NSDate *startSample = sample.startDate;
                                                                          NSDate *endSample = sample.endDate;
                                                                          NSMutableDictionary *entry = [NSMutableDictionary dictionary];

                                                                          // common indices
                                                                          entry[HKPluginKeyStartDate] =[HealthKit stringFromDate:startSample];
                                                                          entry[HKPluginKeyEndDate] = [HealthKit stringFromDate:endSample];
                                                                          entry[HKPluginKeyUUID] = sample.UUID.UUIDString;
                                                                          entry[HKPluginKeySourceName] = sample.sourceRevision.source.name;
                                                                          entry[HKPluginKeySourceBundleId] = sample.sourceRevision.source.bundleIdentifier;
                                                                          
                                                                          entry[@"sourceProductType"] = sample.sourceRevision.productType;
                                                                          entry[@"sourceVersion"] = sample.sourceRevision.version;
                                                                          entry[@"sourceOSVersionMajor"] = [NSNumber numberWithInteger:sample.sourceRevision.operatingSystemVersion.majorVersion];
                                                                          entry[@"sourceOSVersionMinor"] = [NSNumber numberWithInteger:sample.sourceRevision.operatingSystemVersion.minorVersion];
                                                                          entry[@"sourceOSVersionPatch"] = [NSNumber numberWithInteger:sample.sourceRevision.operatingSystemVersion.patchVersion];
                                                                          
                                                                          entry[@"deviceName"] = sample.device.name;
                                                                          entry[@"deviceModel"] = sample.device.model;
                                                                          entry[@"deviceManufacturer"] = sample.device.manufacturer;
                                                                          entry[@"deviceLocalIdentifier"] = sample.device.localIdentifier;
                                                                          entry[@"deviceHardwareVersion"] = sample.device.hardwareVersion;
                                                                          entry[@"deviceSoftwareVersion"] = sample.device.softwareVersion;
                                                                          entry[@"deviceFirmwareVersion"] = sample.device.firmwareVersion;
                                                                          entry[@"UDI"] = sample.device.UDIDeviceIdentifier;
                                                                          
                                                                          entry[HKPluginKeyMetadata] = [@{} mutableCopy];
                                                                          

                                                                          if (sample.metadata != nil && [sample.metadata isKindOfClass:[NSDictionary class]]) {
                                                                              for (id key in sample.metadata) {
                                                                                  NSMutableDictionary *metadata = [@{} mutableCopy];
                                                                                  [metadata setObject:[sample.metadata objectForKey:key] forKey:key];
                                                                                  
                                                                                  if ([NSJSONSerialization isValidJSONObject:metadata]) {
                                                                                      [entry[HKPluginKeyMetadata] setObject:[sample.metadata objectForKey:key] forKey:key];
                                                                                  }
                                                                                  
                                                                              }
                                                                          }

                                                                          // case-specific indices
                                                                          if ([sample isKindOfClass:[HKCategorySample class]]) {

                                                                              HKCategorySample *csample = (HKCategorySample *) sample;
                                                                              entry[HKPluginKeyValue] = @(csample.value);
                                                                              entry[@"categoryType.identifier"] = csample.categoryType.identifier;
                                                                              entry[@"categoryType.description"] = csample.categoryType.description;

                                                                          } else if ([sample isKindOfClass:[HKCorrelationType class]]) {

                                                                              HKCorrelation *correlation = (HKCorrelation *) sample;
                                                                              entry[HKPluginKeyCorrelationType] = correlation.correlationType.identifier;

                                                                          } else if ([sample isKindOfClass:[HKQuantitySample class]]) {
                                                                              if (unit == nil) {
                                                                                     [HealthKit triggerErrorCallbackWithMessage:@"no unit provided" command:command delegate:self.commandDelegate];
                                                                                     break;
                                                                              }
                                                                              @try {

                                                                                  HKQuantitySample *qsample = (HKQuantitySample *) sample;
                                                                                  [entry setValue:@([qsample.quantity doubleValueForUnit:unit]) forKey:@"quantity"];
                                                                                  
                                                                                  NSString *qtype = qsample.quantityType.identifier;
                                                                                  [entry setValue:qtype forKey:@"quantityType"];
                                                                                  
                                                                              } @catch (NSException *exception) {
                                                                                  dispatch_sync(dispatch_get_main_queue(), ^{
                                                                                      [HealthKit triggerErrorCallbackWithMessage:@"Error: Incompatable unit" command:command delegate:bSelf.commandDelegate];
                                                                                  });
                                                                                  break;
                                                                              }
                                                                              
                                                                          } else if ([sample isKindOfClass:[HKWorkout class]]) {

                                                                              HKWorkout *wsample = (HKWorkout *) sample;
                                                                              [entry setValue:@(wsample.duration) forKey:@"duration"];

                                                                          }

                                                                          [finalResults addObject:entry];
                                                                      }

                                                                      dispatch_sync(dispatch_get_main_queue(), ^{
                                                                          CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:finalResults];
                                                                          [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
                                                                      });
                                                                  }
                                                              }];

            [[HealthKit sharedHealthStore] executeQuery:query];
        } else {
            dispatch_sync(dispatch_get_main_queue(), ^{
                [HealthKit triggerErrorCallbackWithMessage:error.localizedDescription command:command delegate:bSelf.commandDelegate];
            });
        }
    }];
}

/**
 * Query a specified sample type using an aggregation
 *
 * @param command *CDVInvokedUrlCommand
 */
- (void)querySampleTypeAggregated:(CDVInvokedUrlCommand *)command {
    NSDictionary *args = command.arguments[0];
    NSDate *startDate = [NSDate dateWithTimeIntervalSince1970:[args[HKPluginKeyStartDate] longValue]];
    NSDate *endDate = [NSDate dateWithTimeIntervalSince1970:[args[HKPluginKeyEndDate] longValue]];

    NSString *sampleTypeString = args[HKPluginKeySampleType];
    NSString *unitString = args[HKPluginKeyUnit];

    NSCalendar *calendar = [NSCalendar currentCalendar];
    NSDateComponents *interval = [[NSDateComponents alloc] init];

    NSString *aggregation = args[HKPluginKeyAggregation];
    // TODO would be nice to also have the dev pass in the nr of hours/days/..
    if ([@"hour" isEqualToString:aggregation]) {
        interval.hour = 1;
    } else if ([@"week" isEqualToString:aggregation]) {
        interval.day = 7;
    } else if ([@"month" isEqualToString:aggregation]) {
        interval.month = 1;
    } else if ([@"year" isEqualToString:aggregation]) {
        interval.year = 1;
    } else {
        // default 'day'
        interval.day = 1;
    }

    NSDateComponents *anchorComponents = [calendar components:NSCalendarUnitDay | NSCalendarUnitMonth | NSCalendarUnitYear
                                                     fromDate:endDate]; //[NSDate date]];
    anchorComponents.hour = 0; //at 00:00 AM
    NSDate *anchorDate = [calendar dateFromComponents:anchorComponents];
    HKQuantityType *quantityType = [HKObjectType quantityTypeForIdentifier:sampleTypeString];

    HKStatisticsOptions statOpt = HKStatisticsOptionNone;

    if (quantityType == nil) {
        [HealthKit triggerErrorCallbackWithMessage:@"sampleType is invalid" command:command delegate:self.commandDelegate];
        return;
    } else if ([sampleTypeString isEqualToString:@"HKQuantityTypeIdentifierHeartRate"]) {
        statOpt = HKStatisticsOptionDiscreteAverage;

    } else { //HKQuantityTypeIdentifierStepCount, etc...
        statOpt = HKStatisticsOptionCumulativeSum;
    }

    HKUnit *unit = nil;
    if (unitString != nil) {
        // issue 51
        // @see https://github.com/Telerik-Verified-Plugins/HealthKit/issues/51
        if ([unitString isEqualToString:@"percent"]) {
            unitString = @"%";
        }
        unit = [HKUnit unitFromString:unitString];
    }

    HKSampleType *type = [HealthKit getHKSampleType:sampleTypeString];
    if (type == nil) {
        [HealthKit triggerErrorCallbackWithMessage:@"sampleType is invalid" command:command delegate:self.commandDelegate];
        return;
    }

    // NSPredicate *predicate = [HKQuery predicateForSamplesWithStartDate:startDate endDate:endDate options:HKQueryOptionStrictStartDate];
    NSPredicate *predicate = nil;

    BOOL filtered = (args[@"filtered"] != nil && [args[@"filtered"] boolValue]);
    if (filtered) {
        predicate = [NSPredicate predicateWithFormat:@"metadata.%K != YES", HKMetadataKeyWasUserEntered];
    }

    NSSet *requestTypes = [NSSet setWithObjects:type, nil];
    [[HealthKit sharedHealthStore] requestAuthorizationToShareTypes:nil readTypes:requestTypes completion:^(BOOL success, NSError *error) {
        __block HealthKit *bSelf = self;
        if (success) {
            HKStatisticsCollectionQuery *query = [[HKStatisticsCollectionQuery alloc] initWithQuantityType:quantityType
                                                                                   quantitySamplePredicate:predicate
                                                                                                   options:statOpt
                                                                                                anchorDate:anchorDate
                                                                                        intervalComponents:interval];

            // Set the results handler
            query.initialResultsHandler = ^(HKStatisticsCollectionQuery *statisticsCollectionQuery, HKStatisticsCollection *results, NSError *innerError) {
                if (innerError) {
                    // Perform proper error handling here
                    //                    NSLog(@"*** An error occurred while calculating the statistics: %@ ***",error.localizedDescription);
                    dispatch_sync(dispatch_get_main_queue(), ^{
                        [HealthKit triggerErrorCallbackWithMessage:innerError.localizedDescription command:command delegate:bSelf.commandDelegate];
                    });
                } else {
                    // Get the daily steps over the past n days
                    //            HKUnit *unit = unitString!=nil ? [HKUnit unitFromString:unitString] : [HKUnit countUnit];
                    NSMutableArray *finalResults = [[NSMutableArray alloc] initWithCapacity:[[results statistics] count]];

                    [results enumerateStatisticsFromDate:startDate
                                                  toDate:endDate
                                               withBlock:^(HKStatistics *result, BOOL *stop) {

                                                   NSDate *valueStartDate = result.startDate;
                                                   NSDate *valueEndDate = result.endDate;

                                                   NSMutableDictionary *entry = [NSMutableDictionary dictionary];
                                                   entry[HKPluginKeyStartDate] = [HealthKit stringFromDate:valueStartDate];
                                                   entry[HKPluginKeyEndDate] = [HealthKit stringFromDate:valueEndDate];

                                                   HKQuantity *quantity = nil;
                                                   switch (statOpt) {
                                                       case HKStatisticsOptionDiscreteAverage:
                                                           quantity = result.averageQuantity;
                                                           break;
                                                       case HKStatisticsOptionCumulativeSum:
                                                           quantity = result.sumQuantity;
                                                           break;
                                                       case HKStatisticsOptionDiscreteMin:
                                                           quantity = result.minimumQuantity;
                                                           break;
                                                       case HKStatisticsOptionDiscreteMax:
                                                           quantity = result.maximumQuantity;
                                                           break;

                                                           // @TODO return appropriate values here
                                                       case HKStatisticsOptionSeparateBySource:
                                                       case HKStatisticsOptionNone:
                                                       default:
                                                           break;
                                                   }

                                                   double value = [quantity doubleValueForUnit:unit];
                                                   entry[@"quantity"] = @(value);
                                                   [finalResults addObject:entry];
                                               }];

                    dispatch_sync(dispatch_get_main_queue(), ^{
                        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:finalResults];
                        [bSelf.commandDelegate sendPluginResult:result callbackId:command.callbackId];
                    });
                }
            };

            [[HealthKit sharedHealthStore] executeQuery:query];

        } else {
            dispatch_sync(dispatch_get_main_queue(), ^{
                [HealthKit triggerErrorCallbackWithMessage:error.localizedDescription command:command delegate:bSelf.commandDelegate];
            });
        }
    }];


}

/**
 * Query a specified correlation type
 *
 * @param command *CDVInvokedUrlCommand
 */
- (void)queryCorrelationType:(CDVInvokedUrlCommand *)command {
    NSDictionary *args = command.arguments[0];
    NSDate *startDate = [NSDate dateWithTimeIntervalSince1970:[args[HKPluginKeyStartDate] longValue]];
    NSDate *endDate = [NSDate dateWithTimeIntervalSince1970:[args[HKPluginKeyEndDate] longValue]];
    NSString *correlationTypeString = args[HKPluginKeyCorrelationType];
    NSArray<NSString *> *unitsString = args[HKPluginKeyUnits];

    HKCorrelationType *type = (HKCorrelationType *) [HealthKit getHKSampleType:correlationTypeString];
    if (type == nil) {
        [HealthKit triggerErrorCallbackWithMessage:@"sampleType is invalid" command:command delegate:self.commandDelegate];
        return;
    }
    NSMutableArray *units = [[NSMutableArray alloc] init];
    for (NSString *unitString in unitsString) {
        HKUnit *unit = ((unitString != nil) ? [HKUnit unitFromString:unitString] : nil);
        [units addObject:unit];
    }

    // TODO check that unit is compatible with sampleType if sample type of HKQuantityType
    NSPredicate *predicate = [HKQuery predicateForSamplesWithStartDate:startDate endDate:endDate options:HKQueryOptionStrictStartDate];

    HKCorrelationQuery *query = [[HKCorrelationQuery alloc] initWithType:type predicate:predicate samplePredicates:nil completion:^(HKCorrelationQuery *correlationQuery, NSArray *correlations, NSError *error) {
        __block HealthKit *bSelf = self;
        if (error) {
            dispatch_sync(dispatch_get_main_queue(), ^{
                [HealthKit triggerErrorCallbackWithMessage:error.localizedDescription command:command delegate:bSelf.commandDelegate];
            });
        } else {
            NSMutableArray *finalResults = [[NSMutableArray alloc] initWithCapacity:correlations.count];
            for (HKSample *sample in correlations) {
                NSDate *startSample = sample.startDate;
                NSDate *endSample = sample.endDate;

                NSMutableDictionary *entry = [NSMutableDictionary dictionary];
                entry[HKPluginKeyStartDate] = [HealthKit stringFromDate:startSample];
                entry[HKPluginKeyEndDate] = [HealthKit stringFromDate:endSample];

                // common indices
                entry[HKPluginKeyUUID] = sample.UUID.UUIDString;
                entry[HKPluginKeySourceName] = sample.sourceRevision.source.name;
                entry[HKPluginKeySourceBundleId] = sample.sourceRevision.source.bundleIdentifier;
                
                entry[@"sourceProductType"] = sample.sourceRevision.productType;
                entry[@"sourceVersion"] = sample.sourceRevision.version;
                entry[@"sourceOSVersionMajor"] = [NSNumber numberWithInteger:sample.sourceRevision.operatingSystemVersion.majorVersion];
                entry[@"sourceOSVersionMinor"] = [NSNumber numberWithInteger:sample.sourceRevision.operatingSystemVersion.minorVersion];
                entry[@"sourceOSVersionPatch"] = [NSNumber numberWithInteger:sample.sourceRevision.operatingSystemVersion.patchVersion];
                
                entry[@"deviceName"] = sample.device.name;
                entry[@"deviceModel"] = sample.device.model;
                entry[@"deviceManufacturer"] = sample.device.manufacturer;
                entry[@"deviceLocalIdentifier"] = sample.device.localIdentifier;
                entry[@"deviceHardwareVersion"] = sample.device.hardwareVersion;
                entry[@"deviceSoftwareVersion"] = sample.device.softwareVersion;
                entry[@"deviceFirmwareVersion"] = sample.device.firmwareVersion;
                entry[@"UDI"] = sample.device.UDIDeviceIdentifier;
                entry[HKPluginKeyMetadata] = [@{} mutableCopy];

                if (sample.metadata != nil && [sample.metadata isKindOfClass:[NSDictionary class]]) {
                    for (id key in sample.metadata) {
                        NSMutableDictionary *metadata = [@{} mutableCopy];
                        [metadata setObject:[sample.metadata objectForKey:key] forKey:key];
                        
                        if ([NSJSONSerialization isValidJSONObject:metadata]) {
                            [entry[HKPluginKeyMetadata] setObject:[sample.metadata objectForKey:key] forKey:key];
                        }
                        
                    }
                }

                if ([sample isKindOfClass:[HKCategorySample class]]) {

                    HKCategorySample *csample = (HKCategorySample *) sample;
                    entry[HKPluginKeyValue] = @(csample.value);
                    entry[@"categoryType.identifier"] = csample.categoryType.identifier;
                    entry[@"categoryType.description"] = csample.categoryType.description;

                } else if ([sample isKindOfClass:[HKCorrelation class]]) {

                    HKCorrelation *correlation = (HKCorrelation *) sample;
                    entry[HKPluginKeyCorrelationType] = correlation.correlationType.identifier;

                    NSMutableArray *samples = [NSMutableArray arrayWithCapacity:correlation.objects.count];
                    for (HKQuantitySample *quantitySample in correlation.objects) {
                        for (int i=0; i<[units count]; i++) {
                            HKUnit *unit = units[i];
                            NSString *unitS = unitsString[i];
                            if ([quantitySample.quantity isCompatibleWithUnit:unit]) {
                                [samples addObject:@{
                                                     HKPluginKeyStartDate: [HealthKit stringFromDate:quantitySample.startDate],
                                                     HKPluginKeyEndDate: [HealthKit stringFromDate:quantitySample.endDate],
                                                     HKPluginKeySampleType: quantitySample.sampleType.identifier,
                                                     HKPluginKeyValue: @([quantitySample.quantity doubleValueForUnit:unit]),
                                                     HKPluginKeyUnit: unitS,
                                                     HKPluginKeyMetadata: (quantitySample.metadata == nil || ![NSJSONSerialization isValidJSONObject:quantitySample.metadata]) ? @{} : quantitySample.metadata,
                                                     HKPluginKeyUUID: quantitySample.UUID.UUIDString
                                                     }
                                 ];
                                break;
                            }
                        }
                    }
                    entry[HKPluginKeyObjects] = samples;

                } else if ([sample isKindOfClass:[HKQuantitySample class]]) {

                    HKQuantitySample *qsample = (HKQuantitySample *) sample;
                    for (int i=0; i<[units count]; i++) {
                        HKUnit *unit = units[i];
                        if ([qsample.quantity isCompatibleWithUnit:unit]) {
                            double quantity = [qsample.quantity doubleValueForUnit:unit];
                            entry[@"quantity"] = [NSString stringWithFormat:@"%f", quantity];
                            break;
                        }
                    }

                } else if ([sample isKindOfClass:[HKWorkout class]]) {

                    HKWorkout *wsample = (HKWorkout *) sample;
                    entry[@"duration"] = @(wsample.duration);

                } else if ([sample isKindOfClass:[HKCorrelationType class]]) {
                    // TODO
                    // wat do?
                }

                [finalResults addObject:entry];
            }

            dispatch_sync(dispatch_get_main_queue(), ^{
                CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:finalResults];
                [bSelf.commandDelegate sendPluginResult:result callbackId:command.callbackId];
            });
        }
    }];
    [[HealthKit sharedHealthStore] executeQuery:query];
}

/**
 * Save sample data
 *
 * @param command *CDVInvokedUrlCommand
 */
- (void)saveSample:(CDVInvokedUrlCommand *)command {
    NSDictionary *args = command.arguments[0];

    //Use helper method to create quantity sample
    NSError *error = nil;
    HKSample *sample = [self loadHKSampleFromInputDictionary:args error:&error];

    //If error in creation, return plugin result
    if (error) {
        [HealthKit triggerErrorCallbackWithMessage:error.localizedDescription command:command delegate:self.commandDelegate];
        return;
    }

    //Otherwise save to health store
    [[HealthKit sharedHealthStore] saveObject:sample withCompletion:^(BOOL success, NSError *innerError) {
        __block HealthKit *bSelf = self;
        if (success) {
            dispatch_sync(dispatch_get_main_queue(), ^{
                CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
                [bSelf.commandDelegate sendPluginResult:result callbackId:command.callbackId];
            });
        } else {
            dispatch_sync(dispatch_get_main_queue(), ^{
                [HealthKit triggerErrorCallbackWithMessage:innerError.localizedDescription command:command delegate:bSelf.commandDelegate];
            });
        }
    }];

}

/**
 * Save correlation data
 *
 * @param command *CDVInvokedUrlCommand
 */
- (void)saveCorrelation:(CDVInvokedUrlCommand *)command {
    NSDictionary *args = command.arguments[0];
    NSError *error = nil;

    //Use helper method to create correlation
    HKCorrelation *correlation = [self loadHKCorrelationFromInputDictionary:args error:&error];

    //If error in creation, return plugin result
    if (error) {
        [HealthKit triggerErrorCallbackWithMessage:error.localizedDescription command:command delegate:self.commandDelegate];
        return;
    }

    //Otherwise save to health store
    [[HealthKit sharedHealthStore] saveObject:correlation withCompletion:^(BOOL success, NSError *saveError) {
        __block HealthKit *bSelf = self;
        if (success) {
            dispatch_sync(dispatch_get_main_queue(), ^{
                CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
                [bSelf.commandDelegate sendPluginResult:result callbackId:command.callbackId];
            });
        } else {
            dispatch_sync(dispatch_get_main_queue(), ^{
                [HealthKit triggerErrorCallbackWithMessage:saveError.localizedDescription command:command delegate:bSelf.commandDelegate];
            });
        }
    }];
}

/**
 * Delete matching samples from the HealthKit store.
 * See https://developer.apple.com/library/ios/documentation/HealthKit/Reference/HKHealthStore_Class/#//apple_ref/occ/instm/HKHealthStore/deleteObject:withCompletion:
 *
 * @param command *CDVInvokedUrlCommand
 */
- (void)deleteSamples:(CDVInvokedUrlCommand *)command {
  NSDictionary *args = command.arguments[0];
  NSDate *startDate = [NSDate dateWithTimeIntervalSince1970:[args[HKPluginKeyStartDate] longValue]];
  NSDate *endDate = [NSDate dateWithTimeIntervalSince1970:[args[HKPluginKeyEndDate] longValue]];
  NSString *sampleTypeString = args[HKPluginKeySampleType];

  HKSampleType *type = [HealthKit getHKSampleType:sampleTypeString];
  if (type == nil) {
    [HealthKit triggerErrorCallbackWithMessage:@"sampleType is invalid" command:command delegate:self.commandDelegate];
    return;
  }

  NSPredicate *predicate = [HKQuery predicateForSamplesWithStartDate:startDate endDate:endDate options:HKQueryOptionStrictStartDate];

  NSSet *requestTypes = [NSSet setWithObjects:type, nil];
  [[HealthKit sharedHealthStore] requestAuthorizationToShareTypes:nil readTypes:requestTypes completion:^(BOOL success, NSError *error) {
    __block HealthKit *bSelf = self;
    if (success) {
      [[HealthKit sharedHealthStore] deleteObjectsOfType:type predicate:predicate withCompletion:^(BOOL success, NSUInteger deletedObjectCount, NSError * _Nullable deletionError) {
        if (deletionError != nil) {
          dispatch_sync(dispatch_get_main_queue(), ^{
            [HealthKit triggerErrorCallbackWithMessage:deletionError.localizedDescription command:command delegate:bSelf.commandDelegate];
          });
        } else {
          dispatch_sync(dispatch_get_main_queue(), ^{
            CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:(int)deletedObjectCount];
            [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
          });
        }
      }];
    }
  }];
}

- (void)queryAudiogramSamples:(CDVInvokedUrlCommand *)command
 {
     if (@available(iOS 14.0, *)) {
         NSDictionary *args = command.arguments[0];
         NSDate *startDate = [NSDate dateWithTimeIntervalSince1970:[args[HKPluginKeyStartDate] longValue]];
         NSDate *endDate = [NSDate dateWithTimeIntervalSince1970:[args[HKPluginKeyEndDate] longValue]];
         NSUInteger limit = ((args[@"limit"] != nil) ? [args[@"limit"] unsignedIntegerValue] : 1000);
         BOOL ascending = (args[@"ascending"] != nil && [args[@"ascending"] boolValue]);
         
         NSPredicate * predicate = [HKQuery predicateForSamplesWithStartDate:startDate endDate:endDate options:HKQueryOptionStrictStartDate];;
         NSSortDescriptor *timeSortDescriptor = [[NSSortDescriptor alloc] initWithKey:HKSampleSortIdentifierEndDate ascending:ascending];

         // Define the results handler for the SampleQuery.
         void (^resultsHandler)(HKSampleQuery *query, NSArray *results, NSError *innerError);
         resultsHandler = ^(HKSampleQuery *query, NSArray *results, NSError *innerError) {
             if (innerError != nil) {
                 dispatch_sync(dispatch_get_main_queue(), ^{
                     [HealthKit triggerErrorCallbackWithMessage:innerError.localizedDescription command:command delegate:self.commandDelegate];
                 });
             }

             // explicity send back an empty array for no results
             if (results.count == 0) {
                 dispatch_sync(dispatch_get_main_queue(), ^{
                     CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:@[]];
                     [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
                 });
                 return;
             }

             NSMutableArray *data = [NSMutableArray arrayWithCapacity:results.count];

             for (HKAudiogramSample *sample in results) {
                 NSMutableDictionary *entry = [NSMutableDictionary dictionary];
                 
                 [entry setObject:[HealthKit stringFromDate:sample.startDate] forKey:@"startDate"];
                 [entry setObject:[HealthKit stringFromDate:sample.endDate] forKey:@"endDate"];
                 NSNumber *osMajor = [NSNumber numberWithInteger:sample.sourceRevision.operatingSystemVersion.majorVersion];
                 NSNumber *osMinor = [NSNumber numberWithInteger:sample.sourceRevision.operatingSystemVersion.minorVersion];
                 NSNumber *osPatch = [NSNumber numberWithInteger:sample.sourceRevision.operatingSystemVersion.patchVersion];

                 NSMutableArray *sensitivityPoints = [NSMutableArray arrayWithCapacity:sample.sensitivityPoints.count];
                 for (HKAudiogramSensitivityPoint *sensitivityPoint in sample.sensitivityPoints) {
                     NSMutableDictionary *sensitivityPointEntry = [NSMutableDictionary dictionary];
                     
                     double frequency = [sensitivityPoint.frequency doubleValueForUnit:[HKUnit hertzUnit]];
                     
                     [sensitivityPointEntry setValue:[NSString stringWithFormat:@"%f", frequency] forKey:@"frequency"];
                     [sensitivityPointEntry setValue:@"Hz" forKey:@"frequencyUnit"];
                     
                     [sensitivityPointEntry setValue:@([sensitivityPoint.rightEarSensitivity doubleValueForUnit:[HKUnit decibelHearingLevelUnit]]) forKey:@"rightEarSensitivity"];
                     [sensitivityPointEntry setValue:@([sensitivityPoint.leftEarSensitivity doubleValueForUnit:[HKUnit decibelHearingLevelUnit]]) forKey:@"leftEarSensitivity"];
                     [sensitivityPointEntry setValue:@"dBHL" forKey:@"sensitivityUnit"];

                     [sensitivityPoints addObject:sensitivityPointEntry];
                 }
                 
                 [entry setObject:sensitivityPoints forKey:@"sensitivityPoints"];
                 
                 [entry setObject:sample.sourceRevision.source.name ? sample.sourceRevision.source.name : @"" forKey:@"sourceName"];
                 [entry setObject:sample.sourceRevision.source.bundleIdentifier ? sample.sourceRevision.source.bundleIdentifier : @"" forKey:@"sourceBundleId"];
                 [entry setObject:sample.sourceRevision.productType ? sample.sourceRevision.productType : @"" forKey:@"sourceProductType"];
                 [entry setObject:sample.sourceRevision.version ? sample.sourceRevision.version : @"" forKey:@"sourceVersion"];

                 [entry setObject:osMajor ? osMajor : @"" forKey:@"sourceOSVersionMajor"];
                 [entry setObject:osMinor ? osMinor : @"" forKey:@"sourceOSVersionMinor"];
                 [entry setObject:osPatch ? osPatch : @"" forKey:@"sourceOSVersionPatch"];
                 
                 [entry setObject:sample.device.name ? sample.device.name : @"" forKey:@"deviceName"];
                 [entry setObject:sample.device.model ? sample.device.model : @"" forKey:@"deviceModel"];
                 [entry setObject:sample.device.manufacturer ? sample.device.manufacturer : @"" forKey:@"deviceManufacturer"];
                 [entry setObject:sample.device.localIdentifier ? sample.device.localIdentifier : @"" forKey:@"deviceLocalIdentifier"];
                 [entry setObject:sample.device.hardwareVersion ? sample.device.hardwareVersion : @"" forKey:@"deviceHardwareVersion"];
                 [entry setObject:sample.device.softwareVersion ? sample.device.softwareVersion : @"" forKey:@"deviceSoftwareVersion"];
                 [entry setObject:sample.device.firmwareVersion ? sample.device.firmwareVersion : @"" forKey:@"deviceFirmwareVersion"];

                 [data addObject:entry];
             }

             dispatch_sync(dispatch_get_main_queue(), ^{
                  CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:data];
                  [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
              });
         };
         
         NSMutableSet *readDataTypes = [[NSMutableSet alloc] init];
         [readDataTypes addObject:[HKObjectType audiogramSampleType]];
         [[HealthKit sharedHealthStore] requestAuthorizationToShareTypes:nil readTypes:readDataTypes completion:^(BOOL success, NSError *error) {
             __block HealthKit *bSelf = self;
             if (success) {
                 HKSampleQuery *query = [[HKSampleQuery alloc] initWithSampleType:HKObjectType.audiogramSampleType
                                                                           predicate:predicate
                                                                               limit:limit
                                                                     sortDescriptors:@[timeSortDescriptor]
                                                                      resultsHandler:resultsHandler];
                 [[HealthKit sharedHealthStore] executeQuery:query];
             } else {
                 dispatch_sync(dispatch_get_main_queue(), ^{
                     [HealthKit triggerErrorCallbackWithMessage:@"Permission denied" command:command delegate:bSelf.commandDelegate];
                 });
             }
         }];
     } else {
         dispatch_sync(dispatch_get_main_queue(), ^{
             [HealthKit triggerErrorCallbackWithMessage:@"Audiogram is not available for this iOS version" command:command delegate:self.commandDelegate];
         });
     }
 }

- (void)queryElectrocardiogramSamples:(CDVInvokedUrlCommand *)command
 {
     if (@available(iOS 14.0, *)) {
         NSDictionary *args = command.arguments[0];
         NSDate *startDate = [NSDate dateWithTimeIntervalSince1970:[args[HKPluginKeyStartDate] longValue]];
         NSDate *endDate = [NSDate dateWithTimeIntervalSince1970:[args[HKPluginKeyEndDate] longValue]];
         NSUInteger limit = ((args[@"limit"] != nil) ? [args[@"limit"] unsignedIntegerValue] : 1000);
         BOOL ascending = (args[@"ascending"] != nil && [args[@"ascending"] boolValue]);
         
         NSPredicate * predicate = [HKQuery predicateForSamplesWithStartDate:startDate endDate:endDate options:HKQueryOptionStrictStartDate];;
         NSSortDescriptor *timeSortDescriptor = [[NSSortDescriptor alloc] initWithKey:HKSampleSortIdentifierEndDate ascending:ascending];

         // Define the results handler for the SampleQuery.
         void (^resultsHandler)(HKSampleQuery *query, NSArray *results, NSError *innerError);
         resultsHandler = ^(HKSampleQuery *query, NSArray *results, NSError *innerError) {
             if (innerError != nil) {
                 dispatch_sync(dispatch_get_main_queue(), ^{
                     [HealthKit triggerErrorCallbackWithMessage:innerError.localizedDescription command:command delegate:self.commandDelegate];
                 });
             }

             // explicity send back an empty array for no results
             if (results.count == 0) {
                 dispatch_sync(dispatch_get_main_queue(), ^{
                     CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:@[]];
                     [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
                 });
                 return;
             }

             __block NSUInteger samplesProcessed = 0;
             NSMutableArray *data = [NSMutableArray arrayWithCapacity:1];

             // create a function that check the progress of processing the samples
             // and executes the callback with the data whan done
             void (^maybeFinish)(void);
             maybeFinish =  ^() {
                 // check to see if we've processed all of the returned samples, and return if so
                 if (samplesProcessed == results.count) {
                     dispatch_sync(dispatch_get_main_queue(), ^{
                         CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:data];
                         [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
                     });
                 }
             };

             for (HKElectrocardiogram *sample in results) {
                 NSDate *startSample = sample.startDate;
                 NSDate *endSample = sample.endDate;

                 NSString *classification;
                 switch(sample.classification) {
                     case(HKElectrocardiogramClassificationNotSet):
                         classification = @"NotSet";
                         break;
                     case(HKElectrocardiogramClassificationSinusRhythm):
                         classification = @"SinusRhythm";
                         break;
                     case(HKElectrocardiogramClassificationAtrialFibrillation):
                         classification = @"AtrialFibrillation";
                         break;
                     case(HKElectrocardiogramClassificationInconclusiveLowHeartRate):
                         classification = @"InconclusiveLowHeartRate";
                         break;
                     case(HKElectrocardiogramClassificationInconclusiveHighHeartRate):
                         classification = @"InconclusiveHighHeartRate";
                         break;
                     case(HKElectrocardiogramClassificationInconclusivePoorReading):
                         classification = @"InconclusivePoorReading";
                         break;
                     case(HKElectrocardiogramClassificationInconclusiveOther):
                         classification = @"InconclusiveOther";
                         break;
                     default:
                         classification = @"Unrecognized";
                 }
                 
                 HKUnit *count = [HKUnit countUnit];
                 HKUnit *minute = [HKUnit minuteUnit];
                 HKUnit *bpmUnit = [count unitDividedByUnit:minute];
                 double averageHeartRate = [sample.averageHeartRate doubleValueForUnit:bpmUnit];
                 
//                 NSMutableDictionary *sourceDeviceInfo = [NSMutableDictionary dictionary];
//                 sourceDeviceInfo[
                 
                 NSNumber *osMajor = [NSNumber numberWithInteger:sample.sourceRevision.operatingSystemVersion.majorVersion];
                 NSNumber *osMinor = [NSNumber numberWithInteger:sample.sourceRevision.operatingSystemVersion.minorVersion];
                 NSNumber *osPatch = [NSNumber numberWithInteger:sample.sourceRevision.operatingSystemVersion.patchVersion];

                 
                 NSDictionary *elem = @{
                      @"id" : [[sample UUID] UUIDString],
                      @"startDate" : [HealthKit stringFromDate:startSample],
                      @"endDate" : [HealthKit stringFromDate:endSample],
                      @"classification": classification,
                      @"averageHeartRate": @(averageHeartRate),
                      @"samplingFrequency": @([sample.samplingFrequency doubleValueForUnit:HKUnit.hertzUnit]),
                      @"algorithmVersion": @([[sample metadata][HKMetadataKeyAppleECGAlgorithmVersion] intValue]),
                      @"voltageMeasurements": @[],
                      @"sourceName": sample.sourceRevision.source.name ? sample.sourceRevision.source.name : @"",
                      @"sourceBundleId": sample.sourceRevision.source.bundleIdentifier ? sample.sourceRevision.source.bundleIdentifier : @"",
                      @"sourceProductType": sample.sourceRevision.productType ? sample.sourceRevision.productType : @"",
                      @"sourceVersion": sample.sourceRevision.version ? sample.sourceRevision.version : @"",
                      @"sourceOSVersionMajor": osMajor ? osMajor : @"",
                      @"sourceOSVersionMinor": osMinor ? osMinor : @"",
                      @"sourceOSVersionPatch": osPatch ? osPatch : @"",
//
                      @"deviceName": sample.device.name ? sample.device.name : @"",
                      @"deviceModel": sample.device.model ? sample.device.model : @"",
                      @"deviceManufacturer": sample.device.manufacturer ? sample.device.manufacturer : @"",
                      @"deviceLocalIdentifier": sample.device.localIdentifier ? sample.device.localIdentifier : @"",
                      @"deviceHardwareVersion": sample.device.hardwareVersion ? sample.device.hardwareVersion : @"",
                      @"deviceSoftwareVersion": sample.device.softwareVersion ? sample.device.softwareVersion : @"",
                      @"deviceFirmwareVersion": sample.device.firmwareVersion ? sample.device.firmwareVersion : @"",
                  };
                 
                 
                 NSMutableDictionary *mutableElem = [elem mutableCopy];
                 [data addObject:mutableElem];

                 // create an array to hold the ecg voltage data which will be fetched asynchronously from healthkit
                 NSMutableArray *voltageMeasurements = [NSMutableArray arrayWithCapacity:sample.numberOfVoltageMeasurements];

                 // now define the data handler for the HKElectrocardiogramQuery
                 void (^dataHandler)(HKElectrocardiogramQuery *voltageQuery, HKElectrocardiogramVoltageMeasurement *voltageMeasurement, BOOL done, NSError *error);

                 dataHandler = ^(HKElectrocardiogramQuery *voltageQuery, HKElectrocardiogramVoltageMeasurement *voltageMeasurement, BOOL done, NSError *error) {
                     if (error == nil) {
                         // If no error exists for this data point, add the voltage measurement to the array.
                         // I'm not sure if this technique of error handling is what we want. It could lead
                         // to holes in the data. The alternative is to not write any of the voltage data to
                         // the elem dictionary if an error occurs. I think holes are *probably* better?
                         HKQuantity *voltageQuantity = [voltageMeasurement quantityForLead:HKElectrocardiogramLeadAppleWatchSimilarToLeadI];
                         NSArray *measurement = @[
                             @(voltageMeasurement.timeSinceSampleStart),
                             @([voltageQuantity doubleValueForUnit:HKUnit.voltUnit])
                         ];
                         [voltageMeasurements addObject:measurement];
                     }

                     if (done) {
                         [mutableElem setObject:voltageMeasurements forKey:@"voltageMeasurements"];
                         samplesProcessed += 1;
                         maybeFinish();
                     }
                 };
                 HKElectrocardiogramQuery *voltageQuery = [[HKElectrocardiogramQuery alloc] initWithElectrocardiogram:sample
                                                                                                        dataHandler:dataHandler];
                 [[HealthKit sharedHealthStore] executeQuery:voltageQuery];
             }
         };
         
         NSMutableSet *readDataTypes = [[NSMutableSet alloc] init];
         [readDataTypes addObject:[HKObjectType electrocardiogramType]];
         [[HealthKit sharedHealthStore] requestAuthorizationToShareTypes:nil readTypes:readDataTypes completion:^(BOOL success, NSError *error) {
             __block HealthKit *bSelf = self;
             if (success) {
                 HKSampleQuery *ecgQuery = [[HKSampleQuery alloc] initWithSampleType:HKObjectType.electrocardiogramType
                                                                           predicate:predicate
                                                                               limit:limit
                                                                     sortDescriptors:@[timeSortDescriptor]
                                                                      resultsHandler:resultsHandler];
                 [[HealthKit sharedHealthStore] executeQuery:ecgQuery];
             } else {
                 dispatch_sync(dispatch_get_main_queue(), ^{
                     [HealthKit triggerErrorCallbackWithMessage:@"Permission denied" command:command delegate:bSelf.commandDelegate];
                 });
             }
         }];
     } else {
         dispatch_sync(dispatch_get_main_queue(), ^{
             [HealthKit triggerErrorCallbackWithMessage:@"Electrocardiogram is not available for this iOS version" command:command delegate:self.commandDelegate];
         });
     }
 }

@end

#pragma clang diagnostic pop

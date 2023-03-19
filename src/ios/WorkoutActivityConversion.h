#import <HealthKit/HealthKit.h>

@interface WorkoutActivityConversion : NSObject
+ (NSString*) convertHKWorkoutActivityTypeToString:(HKWorkoutActivityType) which;
+ (HKWorkoutActivityType) convertStringToHKWorkoutActivityType:(NSString*) which;
+ (NSString*) convertStringToHKWorkoutActivityTypeString:(NSString*) which;
@end

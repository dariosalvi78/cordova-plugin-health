#import "WorkoutActivityConversion.h"


// Note that code in here requires maintenance but I can't find a better way
@implementation WorkoutActivityConversion

+ (NSString*) convertHKWorkoutActivityTypeToString:(HKWorkoutActivityType) which {

  switch(which) {

    case HKWorkoutActivityTypeArchery:  return @"archery";

    case HKWorkoutActivityTypeBadminton:  return @"badminton";

    case HKWorkoutActivityTypeBaseball:  return @"baseball";

    case HKWorkoutActivityTypeBarre:  return @"barre";

    case HKWorkoutActivityTypeBasketball:  return @"basketball";

    case HKWorkoutActivityTypeCycling:  return @"biking";

    case HKWorkoutActivityTypeHandCycling:  return @"biking.hand";

    case HKWorkoutActivityTypeBowling:  return @"bowling";

    case HKWorkoutActivityTypeBoxing: return @"boxing";

    case HKWorkoutActivityTypeCricket:  return @"cricket";

    case HKWorkoutActivityTypeCooldown: return @"cooldown";

    case HKWorkoutActivityTypeCoreTraining:  return @"core_training";

    case HKWorkoutActivityTypeCrossTraining:  return @"crossfit";

    case HKWorkoutActivityTypeCurling:  return @"curling";

    case HKWorkoutActivityTypeDance:  return @"dancing";

    case HKWorkoutActivityTypeCardioDance:  return @"dancing";

    case HKWorkoutActivityTypeSocialDance: return @"dancing.social";

    case HKWorkoutActivityTypeDiscSports: return @"disc_sports";

    case HKWorkoutActivityTypeElliptical:  return @"elliptical";

    case HKWorkoutActivityTypeFencing:  return @"fencing";

    case HKWorkoutActivityTypeFishing:  return @"fishing";

    case HKWorkoutActivityTypeFitnessGaming: return @"fitness_gaming";

    case HKWorkoutActivityTypeFlexibility:  return @"flexibility";

    case HKWorkoutActivityTypeAmericanFootball:  return @"football.american";

    case HKWorkoutActivityTypeAustralianFootball:  return @"football.australian";

    case HKWorkoutActivityTypeSoccer:  return @"football.soccer";

    case HKWorkoutActivityTypeFunctionalStrengthTraining:  return @"functional_strength";

    case HKWorkoutActivityTypeGolf:  return @"golf";

    case HKWorkoutActivityTypeGymnastics:  return @"gymnastics";

    case HKWorkoutActivityTypeHandball:  return @"handball";

    case HKWorkoutActivityTypeHiking:  return @"hiking";

    case HKWorkoutActivityTypeHockey:  return @"hockey";

    case HKWorkoutActivityTypeEquestrianSports:  return @"horseback_riding";

    case HKWorkoutActivityTypeHunting:  return @"hunting";

    case HKWorkoutActivityTypeHighIntensityIntervalTraining:  return @"interval_training.high_intensity";

    case HKWorkoutActivityTypeJumpRope: return @"jump_rope";

    case HKWorkoutActivityTypeKickboxing:  return @"kickboxing";

    case HKWorkoutActivityTypeMartialArts:  return @"martial_arts";

    case HKWorkoutActivityTypeLacrosse:  return @"lacrosse";

    case HKWorkoutActivityTypeMindAndBody:  return @"meditation";

    case HKWorkoutActivityTypeMixedCardio:  return @"mixed_metabolic_cardio";

    case HKWorkoutActivityTypeOther:  return @"other";

    case HKWorkoutActivityTypePaddleSports:  return @"paddle_sports";

    case HKWorkoutActivityTypePlay:  return @"play";

    case HKWorkoutActivityTypePickleball: return @"pickleball";

    case HKWorkoutActivityTypePilates:  return @"pilates";

    case HKWorkoutActivityTypePreparationAndRecovery:  return @"preparation_and_recovery";

    case HKWorkoutActivityTypeRacquetball:  return @"racquetball";

    case HKWorkoutActivityTypeClimbing:  return @"rock_climbing";

    case HKWorkoutActivityTypeRowing:  return @"rowing";

    case HKWorkoutActivityTypeRugby:  return @"rugby";

    case HKWorkoutActivityTypeRunning:  return @"running";

    case HKWorkoutActivityTypeSailing:  return @"sailing";

    case HKWorkoutActivityTypeSkatingSports:  return @"skating";

    case HKWorkoutActivityTypeDownhillSkiing:  return @"skiing.downhill";

    case HKWorkoutActivityTypeSnowSports:  return @"snow_sports";

    case HKWorkoutActivityTypeCrossCountrySkiing:  return @"skiing.cross_country";

    case HKWorkoutActivityTypeSnowboarding:  return @"snowboarding";

    case HKWorkoutActivityTypeSoftball:  return @"softball";

    case HKWorkoutActivityTypeSquash:  return @"squash";

    case HKWorkoutActivityTypeStairClimbing:  return @"stair_climbing";

    case HKWorkoutActivityTypeStepTraining: return @"step_training";

    case HKWorkoutActivityTypeStairs:  return @"stairs";

    case HKWorkoutActivityTypeTraditionalStrengthTraining:  return @"strength_training";

    case HKWorkoutActivityTypeSurfingSports:  return @"surfing";

    case HKWorkoutActivityTypeSwimming:  return @"swimming";

    case HKWorkoutActivityTypeTableTennis:  return @"table_tennis";

    case HKWorkoutActivityTypeTaiChi: return @"tai_chi";

    case HKWorkoutActivityTypeTennis:  return @"tennis";

    case HKWorkoutActivityTypeTrackAndField:  return @"track_and_field";

    case HKWorkoutActivityTypeVolleyball:  return @"volleyball";

    case HKWorkoutActivityTypeWalking:  return @"walking";

    case HKWorkoutActivityTypeWaterFitness:  return @"water_fitness";

    case HKWorkoutActivityTypeWaterPolo:  return @"water_polo";

    case HKWorkoutActivityTypeWaterSports:  return @"water_sports";

    case HKWorkoutActivityTypeWheelchairWalkPace:  return @"wheelchair.walkpace";

    case HKWorkoutActivityTypeWheelchairRunPace:  return @"wheelchair.runpace";

    case HKWorkoutActivityTypeWrestling:  return @"wrestling";

    case HKWorkoutActivityTypeYoga:  return @"yoga";

    default:

    return @"other";

  }

}



+ (HKWorkoutActivityType) convertStringToHKWorkoutActivityType:(NSString*) which {

  if ([which isEqualToString:@"archery"]) { return HKWorkoutActivityTypeArchery;

  } else if ([which isEqualToString:@"badminton"]) { return HKWorkoutActivityTypeBadminton;

  } else if ([which isEqualToString:@"baseball"]) { return HKWorkoutActivityTypeBaseball;

  } else if ([which isEqualToString:@"basketball"]) { return HKWorkoutActivityTypeBasketball;

  } else if ([which isEqualToString:@"barre"]) { return HKWorkoutActivityTypeBarre;

  } else if ([which isEqualToString:@"biking"]) { return HKWorkoutActivityTypeCycling;

  } else if ([which isEqualToString:@"biking.hand"]) { return HKWorkoutActivityTypeHandCycling;

  } else if ([which isEqualToString:@"biking.mountain"]) { return HKWorkoutActivityTypeCycling;

  } else if ([which isEqualToString:@"biking.road"]) { return HKWorkoutActivityTypeCycling;

  } else if ([which isEqualToString:@"biking.spinning"]) { return HKWorkoutActivityTypeCycling;

  } else if ([which isEqualToString:@"biking.stationary"]) { return HKWorkoutActivityTypeCycling;

  } else if ([which isEqualToString:@"biking.utility"]) { return HKWorkoutActivityTypeCycling;

  } else if ([which isEqualToString:@"bowling"]) { return HKWorkoutActivityTypeBowling;

  } else if ([which isEqualToString:@"boxing"]) { return HKWorkoutActivityTypeBoxing;

  } else if ([which isEqualToString:@"cricket"]) { return HKWorkoutActivityTypeCricket;

  } else if ([which isEqualToString:@"cooldown"]) { if (@available(iOS 14.0, *)) {
      return HKWorkoutActivityTypeCooldown;
    }

  } else if ([which isEqualToString:@"core_training"]) { return HKWorkoutActivityTypeCoreTraining;

  } else if ([which isEqualToString:@"crossfit"]) { return HKWorkoutActivityTypeCrossTraining;

  } else if ([which isEqualToString:@"curling"]) { return HKWorkoutActivityTypeCurling;

  } else if ([which isEqualToString:@"dancing"]) {
    if (@available(iOS 14.0, *)) {
      return HKWorkoutActivityTypeCardioDance;
    }
    return HKWorkoutActivityTypeDance;

  } else if ([which isEqualToString:@"dancing.social"]) {
    if (@available(iOS 14.0, *)) {
      return HKWorkoutActivityTypeSocialDance;
    }
    return HKWorkoutActivityTypeDance;

  } else if ([which isEqualToString:@"disc_sports"]) { if (@available(iOS 13.0, *)) {
      return HKWorkoutActivityTypeDiscSports;
    }
  } else if ([which isEqualToString:@"diving"]) { return HKWorkoutActivityTypeWaterSports;

  } else if ([which isEqualToString:@"elliptical"]) { return HKWorkoutActivityTypeElliptical;

  } else if ([which isEqualToString:@"fencing"]) { return HKWorkoutActivityTypeFencing;

  } else if ([which isEqualToString:@"fitness_gaming"]) { if (@available(iOS 13.0, *)) {
      return HKWorkoutActivityTypeFitnessGaming;
  }

  } else if ([which isEqualToString:@"fishing"]) { return HKWorkoutActivityTypeFishing;

  } else if ([which isEqualToString:@"football.american"]) { return HKWorkoutActivityTypeAmericanFootball;

  } else if ([which isEqualToString:@"football.australian"]) { return HKWorkoutActivityTypeAustralianFootball;

  } else if ([which isEqualToString:@"football.soccer"]) { return HKWorkoutActivityTypeSoccer;

  } else if ([which isEqualToString:@"functional_strength"]) { return HKWorkoutActivityTypeFunctionalStrengthTraining;

  } else if ([which isEqualToString:@"golf"]) { return HKWorkoutActivityTypeGolf;

  } else if ([which isEqualToString:@"gymnastics"]) { return HKWorkoutActivityTypeGymnastics;

  } else if ([which isEqualToString:@"handball"]) { return HKWorkoutActivityTypeHandball;

  } else if ([which isEqualToString:@"hiking"]) { return HKWorkoutActivityTypeHiking;

  } else if ([which isEqualToString:@"hockey"]) { return HKWorkoutActivityTypeHockey;

  } else if ([which isEqualToString:@"horseback_riding"]) { return HKWorkoutActivityTypeEquestrianSports;

  } else if ([which isEqualToString:@"hunting"]) { return HKWorkoutActivityTypeHunting;

  } else if ([which isEqualToString:@"ice_skating"]) { return HKWorkoutActivityTypeSkatingSports;

  } else if ([which isEqualToString:@"interval_training"]) { return HKWorkoutActivityTypeHighIntensityIntervalTraining;

  } else if ([which isEqualToString:@"interval_training.high_intensity"]) { return HKWorkoutActivityTypeHighIntensityIntervalTraining;

  } else if ([which isEqualToString:@"jump_rope"]) { return HKWorkoutActivityTypeJumpRope;

  } else if ([which isEqualToString:@"kayaking"]) { return HKWorkoutActivityTypePaddleSports;

  } else if ([which isEqualToString:@"kick_scooter"]) { return HKWorkoutActivityTypeCycling;

  } else if ([which isEqualToString:@"kickboxing"]) { return HKWorkoutActivityTypeKickboxing;

  } else if ([which isEqualToString:@"lacrosse"]) { return HKWorkoutActivityTypeLacrosse;

  } else if ([which isEqualToString:@"martial_arts"]) { return HKWorkoutActivityTypeMartialArts;

  } else if ([which isEqualToString:@"meditation"]) { return HKWorkoutActivityTypeMindAndBody;

  } else if ([which isEqualToString:@"martial_arts.mixed"]) { return HKWorkoutActivityTypeMartialArts;

  } else if ([which isEqualToString:@"mixed_metabolic_cardio"]) { return HKWorkoutActivityTypeMixedCardio;

  } else if ([which isEqualToString:@"other"]) { return HKWorkoutActivityTypeOther;

  } else if ([which isEqualToString:@"paddle_sports"]) { return HKWorkoutActivityTypePaddleSports;

  } else if ([which isEqualToString:@"play"]) { return HKWorkoutActivityTypePlay;

  } else if ([which isEqualToString:@"pickleball"]) { if (@available(iOS 14.0, *)) {
      return HKWorkoutActivityTypePickleball;
  }

  } else if ([which isEqualToString:@"pilates"]) { return HKWorkoutActivityTypePilates;

  }else if ([which isEqualToString:@"preparation_and_recovery"]) { return HKWorkoutActivityTypePreparationAndRecovery;

  } else if ([which isEqualToString:@"racquetball"]) { return HKWorkoutActivityTypeRacquetball;

  } else if ([which isEqualToString:@"rock_climbing"]) { return HKWorkoutActivityTypeClimbing;

  } else if ([which isEqualToString:@"rowing"]) { return HKWorkoutActivityTypeRowing;

  } else if ([which isEqualToString:@"rowing.machine"]) { return HKWorkoutActivityTypeRowing;

  } else if ([which isEqualToString:@"rugby"]) { return HKWorkoutActivityTypeRugby;

  } else if ([which isEqualToString:@"running"]) { return HKWorkoutActivityTypeRunning;

  } else if ([which isEqualToString:@"running.jogging"]) { return HKWorkoutActivityTypeRunning;

  } else if ([which isEqualToString:@"running.sand"]) { return HKWorkoutActivityTypeRunning;

  } else if ([which isEqualToString:@"running.treadmill"]) { return HKWorkoutActivityTypeRunning;

  } else if ([which isEqualToString:@"sailing"]) { return HKWorkoutActivityTypeSailing;

  } else if ([which isEqualToString:@"scuba_diving"]) { return HKWorkoutActivityTypeWaterSports;

  } else if ([which isEqualToString:@"skateboarding"]) { return HKWorkoutActivityTypeSkatingSports;

  } else if ([which isEqualToString:@"skating"]) { return HKWorkoutActivityTypeSkatingSports;

  } else if ([which isEqualToString:@"skating.cross"]) { return HKWorkoutActivityTypeSkatingSports;

  } else if ([which isEqualToString:@"skating.indoor"]) { return HKWorkoutActivityTypeSkatingSports;

  } else if ([which isEqualToString:@"skating.inline"]) { return HKWorkoutActivityTypeSkatingSports;

  } else if ([which isEqualToString:@"skiing"]) { return HKWorkoutActivityTypeSnowSports;

  } else if ([which isEqualToString:@"skiing.back_country"]) { return HKWorkoutActivityTypeSnowSports;

  } else if ([which isEqualToString:@"skiing.cross_country"]) { return HKWorkoutActivityTypeCrossCountrySkiing;

  } else if ([which isEqualToString:@"skiing.downhill"]) { return HKWorkoutActivityTypeDownhillSkiing;

  } else if ([which isEqualToString:@"skiing.kite"]) { return HKWorkoutActivityTypeSnowSports;

  } else if ([which isEqualToString:@"skiing.roller"]) { return HKWorkoutActivityTypeSnowSports;

  } else if ([which isEqualToString:@"sledding"]) { return HKWorkoutActivityTypeSnowSports;

  } else if ([which isEqualToString:@"snowboarding"]) { return HKWorkoutActivityTypeSnowSports;

  } else if ([which isEqualToString:@"snowmobile"]) { return HKWorkoutActivityTypeSnowSports;

  } else if ([which isEqualToString:@"snowshoeing"]) { return HKWorkoutActivityTypeSnowSports;

  } else if ([which isEqualToString:@"snow_sports"]) { return HKWorkoutActivityTypeSnowSports;

  } else if ([which isEqualToString:@"softball"]) { return HKWorkoutActivityTypeSoftball;

  } else if ([which isEqualToString:@"squash"]) { return HKWorkoutActivityTypeSquash;

  } else if ([which isEqualToString:@"stairs"]) { return HKWorkoutActivityTypeStairs;

  } else if ([which isEqualToString:@"stair_climbing"]) { return HKWorkoutActivityTypeStairClimbing;

  } else if ([which isEqualToString:@"stair_climbing.machine"]) { return HKWorkoutActivityTypeStepTraining;

  } else if ([which isEqualToString:@"standup_paddleboarding"]) { return HKWorkoutActivityTypePaddleSports;

  } else if ([which isEqualToString:@"strength_training"]) { return HKWorkoutActivityTypeTraditionalStrengthTraining;

  } else if ([which isEqualToString:@"surfing"]) { return HKWorkoutActivityTypeSurfingSports;

  } else if ([which isEqualToString:@"swimming"]) { return HKWorkoutActivityTypeSwimming;

  } else if ([which isEqualToString:@"swimming.pool"]) { return HKWorkoutActivityTypeSwimming;

  } else if ([which isEqualToString:@"swimming.open_water"]) { return HKWorkoutActivityTypeSwimming;

  } else if ([which isEqualToString:@"table_tennis"]) { return HKWorkoutActivityTypeTableTennis;

  } else if ([which isEqualToString:@"tai_chi"]) { return HKWorkoutActivityTypeTaiChi;

  } else if ([which isEqualToString:@"tennis"]) { return HKWorkoutActivityTypeTennis;

  } else if ([which isEqualToString:@"track_and_field"]) { return HKWorkoutActivityTypeTrackAndField;

  } else if ([which isEqualToString:@"unknown"]) { return HKWorkoutActivityTypeOther;

  } else if ([which isEqualToString:@"volleyball"]) { return HKWorkoutActivityTypeVolleyball;

  } else if ([which isEqualToString:@"volleyball.beach"]) { return HKWorkoutActivityTypeVolleyball;

  } else if ([which isEqualToString:@"volleyball.indoor"]) { return HKWorkoutActivityTypeVolleyball;

  } else if ([which isEqualToString:@"wakeboarding"]) { return HKWorkoutActivityTypeWaterSports;

  } else if ([which isEqualToString:@"walking"]) { return HKWorkoutActivityTypeWalking;

  } else if ([which isEqualToString:@"walking.fitness"]) { return HKWorkoutActivityTypeWalking;

  } else if ([which isEqualToString:@"walking.nordic"]) { return HKWorkoutActivityTypeWalking;

  } else if ([which isEqualToString:@"walking.treadmill"]) { return HKWorkoutActivityTypeWalking;

  } else if ([which isEqualToString:@"walking.stroller"]) { return HKWorkoutActivityTypeWalking;

  } else if ([which isEqualToString:@"water_fitness"]) { return HKWorkoutActivityTypeWaterFitness;

  } else if ([which isEqualToString:@"water_polo"]) { return HKWorkoutActivityTypeWaterPolo;

  } else if ([which isEqualToString:@"water_sports"]) { return HKWorkoutActivityTypeWaterSports;

  } else if ([which isEqualToString:@"weightlifting"]) { return HKWorkoutActivityTypeTraditionalStrengthTraining;

  } else if ([which isEqualToString:@"wheelchair"]) { return HKWorkoutActivityTypeWheelchairRunPace;

  } else if ([which isEqualToString:@"wheelchair.walkpace"]) { return HKWorkoutActivityTypeWheelchairWalkPace;

  } else if ([which isEqualToString:@"wheelchair.runpace"]) { return HKWorkoutActivityTypeWheelchairRunPace;

  } else if ([which isEqualToString:@"windsurfing"]) { return HKWorkoutActivityTypeWaterSports;

  } else if ([which isEqualToString:@"wrestling"]) { return HKWorkoutActivityTypeWrestling;

  } else if ([which isEqualToString:@"yoga"]) { return HKWorkoutActivityTypeYoga;
  }

  // When no match is available, return other as the result
  return HKWorkoutActivityTypeOther;
}

+ (NSString*) convertStringToHKWorkoutActivityTypeString:(NSString*) which {

  if ([which isEqualToString:@"archery"]) { return @"HKWorkoutActivityTypeArchery";

  } else if ([which isEqualToString:@"badminton"]) { return @"HKWorkoutActivityTypeBadminton";

  } else if ([which isEqualToString:@"baseball"]) { return @"HKWorkoutActivityTypeBaseball";

  } else if ([which isEqualToString:@"basketball"]) { return @"HKWorkoutActivityTypeBasketball";

  } else if ([which isEqualToString:@"barre"]) { return @"HKWorkoutActivityTypeBarre";

  } else if ([which isEqualToString:@"biking"]) { return @"HKWorkoutActivityTypeCycling";

  } else if ([which isEqualToString:@"biking.hand"]) { return @"HKWorkoutActivityTypeHandCycling";

  } else if ([which isEqualToString:@"biking.mountain"]) { return @"HKWorkoutActivityTypeCycling";

  } else if ([which isEqualToString:@"biking.road"]) { return @"HKWorkoutActivityTypeCycling";

  } else if ([which isEqualToString:@"biking.spinning"]) { return @"HKWorkoutActivityTypeCycling";

  } else if ([which isEqualToString:@"biking.stationary"]) { return @"HKWorkoutActivityTypeCycling";

  } else if ([which isEqualToString:@"biking.utility"]) { return @"HKWorkoutActivityTypeCycling";

  } else if ([which isEqualToString:@"bowling"]) { return @"HKWorkoutActivityTypeBowling";

  } else if ([which isEqualToString:@"boxing"]) { return @"HKWorkoutActivityTypeBoxing";

  } else if ([which isEqualToString:@"cricket"]) { return @"HKWorkoutActivityTypeCricket";

  } else if ([which isEqualToString:@"cooldown"]) { if (@available(iOS 14.0, *)) {
      return @"HKWorkoutActivityTypeCooldown";
    }

  } else if ([which isEqualToString:@"core_training"]) { return @"HKWorkoutActivityTypeCoreTraining";

  } else if ([which isEqualToString:@"crossfit"]) { return @"HKWorkoutActivityTypeCrossTraining";

  } else if ([which isEqualToString:@"curling"]) { return @"HKWorkoutActivityTypeCurling";

  } else if ([which isEqualToString:@"dancing"]) {
    if (@available(iOS 14.0, *)) {
      return @"HKWorkoutActivityTypeCardioDance";
    }
    return @"HKWorkoutActivityTypeDance";

  } else if ([which isEqualToString:@"dancing.social"]) {
    if (@available(iOS 14.0, *)) {
      return @"HKWorkoutActivityTypeSocialDance";
    }
    return @"HKWorkoutActivityTypeDance";

  } else if ([which isEqualToString:@"disc_sports"]) { if (@available(iOS 13.0, *)) {
      return @"HKWorkoutActivityTypeDiscSports";
    }
  } else if ([which isEqualToString:@"diving"]) { return @"HKWorkoutActivityTypeWaterSports";

  } else if ([which isEqualToString:@"elliptical"]) { return @"HKWorkoutActivityTypeElliptical";

  } else if ([which isEqualToString:@"fencing"]) { return @"HKWorkoutActivityTypeFencing";

  } else if ([which isEqualToString:@"fitness_gaming"]) { if (@available(iOS 13.0, *)) {
      return @"HKWorkoutActivityTypeFitnessGaming";
  }

  } else if ([which isEqualToString:@"fishing"]) { return @"HKWorkoutActivityTypeFishing";

  } else if ([which isEqualToString:@"football.american"]) { return @"HKWorkoutActivityTypeAmericanFootball";

  } else if ([which isEqualToString:@"football.australian"]) { return @"HKWorkoutActivityTypeAustralianFootball";

  } else if ([which isEqualToString:@"football.soccer"]) { return @"HKWorkoutActivityTypeSoccer";

  } else if ([which isEqualToString:@"functional_strength"]) { return @"HKWorkoutActivityTypeFunctionalStrengthTraining";

  } else if ([which isEqualToString:@"golf"]) { return @"HKWorkoutActivityTypeGolf";

  } else if ([which isEqualToString:@"gymnastics"]) { return @"HKWorkoutActivityTypeGymnastics";

  } else if ([which isEqualToString:@"handball"]) { return @"HKWorkoutActivityTypeHandball";

  } else if ([which isEqualToString:@"hiking"]) { return @"HKWorkoutActivityTypeHiking";

  } else if ([which isEqualToString:@"hockey"]) { return @"HKWorkoutActivityTypeHockey";

  } else if ([which isEqualToString:@"horseback_riding"]) { return @"HKWorkoutActivityTypeEquestrianSports";

  } else if ([which isEqualToString:@"hunting"]) { return @"HKWorkoutActivityTypeHunting";

  } else if ([which isEqualToString:@"ice_skating"]) { return @"HKWorkoutActivityTypeSkatingSports";

  } else if ([which isEqualToString:@"interval_training"]) { return @"HKWorkoutActivityTypeHighIntensityIntervalTraining";

  } else if ([which isEqualToString:@"interval_training.high_intensity"]) { return @"HKWorkoutActivityTypeHighIntensityIntervalTraining";

  } else if ([which isEqualToString:@"jump_rope"]) { return @"HKWorkoutActivityTypeJumpRope";

  } else if ([which isEqualToString:@"kayaking"]) { return @"HKWorkoutActivityTypePaddleSports";

  } else if ([which isEqualToString:@"kick_scooter"]) { return @"HKWorkoutActivityTypeCycling";

  } else if ([which isEqualToString:@"kickboxing"]) { return @"HKWorkoutActivityTypeKickboxing";

  } else if ([which isEqualToString:@"lacrosse"]) { return @"HKWorkoutActivityTypeLacrosse";

  } else if ([which isEqualToString:@"martial_arts"]) { return @"HKWorkoutActivityTypeMartialArts";

  } else if ([which isEqualToString:@"meditation"]) { return @"HKWorkoutActivityTypeMindAndBody";

  } else if ([which isEqualToString:@"martial_arts.mixed"]) { return @"HKWorkoutActivityTypeMartialArts";

  } else if ([which isEqualToString:@"mixed_metabolic_cardio"]) { return @"HKWorkoutActivityTypeMixedCardio";

  } else if ([which isEqualToString:@"other"]) { return @"HKWorkoutActivityTypeOther";

  } else if ([which isEqualToString:@"paddle_sports"]) { return @"HKWorkoutActivityTypePaddleSports";

  } else if ([which isEqualToString:@"play"]) { return @"HKWorkoutActivityTypePlay";

  } else if ([which isEqualToString:@"pickleball"]) { if (@available(iOS 14.0, *)) {
      return @"HKWorkoutActivityTypePickleball";
  }

  } else if ([which isEqualToString:@"pilates"]) { return @"HKWorkoutActivityTypePilates";

  }else if ([which isEqualToString:@"preparation_and_recovery"]) { return @"HKWorkoutActivityTypePreparationAndRecovery";

  } else if ([which isEqualToString:@"racquetball"]) { return @"HKWorkoutActivityTypeRacquetball";

  } else if ([which isEqualToString:@"rock_climbing"]) { return @"HKWorkoutActivityTypeClimbing";

  } else if ([which isEqualToString:@"rowing"]) { return @"HKWorkoutActivityTypeRowing";

  } else if ([which isEqualToString:@"rowing.machine"]) { return @"HKWorkoutActivityTypeRowing";

  } else if ([which isEqualToString:@"rugby"]) { return @"HKWorkoutActivityTypeRugby";

  } else if ([which isEqualToString:@"running"]) { return @"HKWorkoutActivityTypeRunning";

  } else if ([which isEqualToString:@"running.jogging"]) { return @"HKWorkoutActivityTypeRunning";

  } else if ([which isEqualToString:@"running.sand"]) { return @"HKWorkoutActivityTypeRunning";

  } else if ([which isEqualToString:@"running.treadmill"]) { return @"HKWorkoutActivityTypeRunning";

  } else if ([which isEqualToString:@"sailing"]) { return @"HKWorkoutActivityTypeSailing";

  } else if ([which isEqualToString:@"scuba_diving"]) { return @"HKWorkoutActivityTypeWaterSports";

  } else if ([which isEqualToString:@"skateboarding"]) { return @"HKWorkoutActivityTypeSkatingSports";

  } else if ([which isEqualToString:@"skating"]) { return @"HKWorkoutActivityTypeSkatingSports";

  } else if ([which isEqualToString:@"skating.cross"]) { return @"HKWorkoutActivityTypeSkatingSports";

  } else if ([which isEqualToString:@"skating.indoor"]) { return @"HKWorkoutActivityTypeSkatingSports";

  } else if ([which isEqualToString:@"skating.inline"]) { return @"HKWorkoutActivityTypeSkatingSports";

  } else if ([which isEqualToString:@"skiing"]) { return @"HKWorkoutActivityTypeSnowSports";

  } else if ([which isEqualToString:@"skiing.back_country"]) { return @"HKWorkoutActivityTypeSnowSports";

  } else if ([which isEqualToString:@"skiing.cross_country"]) { return @"HKWorkoutActivityTypeCrossCountrySkiing";

  } else if ([which isEqualToString:@"skiing.downhill"]) { return @"HKWorkoutActivityTypeDownhillSkiing";

  } else if ([which isEqualToString:@"skiing.kite"]) { return @"HKWorkoutActivityTypeSnowSports";

  } else if ([which isEqualToString:@"skiing.roller"]) { return @"HKWorkoutActivityTypeSnowSports";

  } else if ([which isEqualToString:@"sledding"]) { return @"HKWorkoutActivityTypeSnowSports";

  } else if ([which isEqualToString:@"snowboarding"]) { return @"HKWorkoutActivityTypeSnowSports";

  } else if ([which isEqualToString:@"snowmobile"]) { return @"HKWorkoutActivityTypeSnowSports";

  } else if ([which isEqualToString:@"snowshoeing"]) { return @"HKWorkoutActivityTypeSnowSports";

  } else if ([which isEqualToString:@"snow_sports"]) { return @"HKWorkoutActivityTypeSnowSports";

  } else if ([which isEqualToString:@"softball"]) { return @"HKWorkoutActivityTypeSoftball";

  } else if ([which isEqualToString:@"squash"]) { return @"HKWorkoutActivityTypeSquash";

  } else if ([which isEqualToString:@"stairs"]) { return @"HKWorkoutActivityTypeStairs";

  } else if ([which isEqualToString:@"stair_climbing"]) { return @"HKWorkoutActivityTypeStairClimbing";

  } else if ([which isEqualToString:@"stair_climbing.machine"]) { return @"HKWorkoutActivityTypeStepTraining";

  } else if ([which isEqualToString:@"standup_paddleboarding"]) { return @"HKWorkoutActivityTypePaddleSports";

  } else if ([which isEqualToString:@"strength_training"]) { return @"HKWorkoutActivityTypeTraditionalStrengthTraining";

  } else if ([which isEqualToString:@"surfing"]) { return @"HKWorkoutActivityTypeSurfingSports";

  } else if ([which isEqualToString:@"swimming"]) { return @"HKWorkoutActivityTypeSwimming";

  } else if ([which isEqualToString:@"swimming.pool"]) { return @"HKWorkoutActivityTypeSwimming";

  } else if ([which isEqualToString:@"swimming.open_water"]) { return @"HKWorkoutActivityTypeSwimming";

  } else if ([which isEqualToString:@"table_tennis"]) { return @"HKWorkoutActivityTypeTableTennis";

  } else if ([which isEqualToString:@"tai_chi"]) { return @"HKWorkoutActivityTypeTaiChi";

  } else if ([which isEqualToString:@"tennis"]) { return @"HKWorkoutActivityTypeTennis";

  } else if ([which isEqualToString:@"track_and_field"]) { return @"HKWorkoutActivityTypeTrackAndField";

  } else if ([which isEqualToString:@"unknown"]) { return @"HKWorkoutActivityTypeOther";

  } else if ([which isEqualToString:@"volleyball"]) { return @"HKWorkoutActivityTypeVolleyball";

  } else if ([which isEqualToString:@"volleyball.beach"]) { return @"HKWorkoutActivityTypeVolleyball";

  } else if ([which isEqualToString:@"volleyball.indoor"]) { return @"HKWorkoutActivityTypeVolleyball";

  } else if ([which isEqualToString:@"wakeboarding"]) { return @"HKWorkoutActivityTypeWaterSports";

  } else if ([which isEqualToString:@"walking"]) { return @"HKWorkoutActivityTypeWalking";

  } else if ([which isEqualToString:@"walking.fitness"]) { return @"HKWorkoutActivityTypeWalking";

  } else if ([which isEqualToString:@"walking.nordic"]) { return @"HKWorkoutActivityTypeWalking";

  } else if ([which isEqualToString:@"walking.treadmill"]) { return @"HKWorkoutActivityTypeWalking";

  } else if ([which isEqualToString:@"walking.stroller"]) { return @"HKWorkoutActivityTypeWalking";

  } else if ([which isEqualToString:@"water_fitness"]) { return @"HKWorkoutActivityTypeWaterFitness";

  } else if ([which isEqualToString:@"water_polo"]) { return @"HKWorkoutActivityTypeWaterPolo";

  } else if ([which isEqualToString:@"water_sports"]) { return @"HKWorkoutActivityTypeWaterSports";

  } else if ([which isEqualToString:@"weightlifting"]) { return @"HKWorkoutActivityTypeTraditionalStrengthTraining";

  } else if ([which isEqualToString:@"wheelchair"]) { return @"HKWorkoutActivityTypeWheelchairRunPace";

  } else if ([which isEqualToString:@"wheelchair.walkpace"]) { return @"HKWorkoutActivityTypeWheelchairWalkPace";

  } else if ([which isEqualToString:@"wheelchair.runpace"]) { return @"HKWorkoutActivityTypeWheelchairRunPace";

  } else if ([which isEqualToString:@"windsurfing"]) { return @"HKWorkoutActivityTypeWaterSports";

  } else if ([which isEqualToString:@"wrestling"]) { return @"HKWorkoutActivityTypeWrestling";

  } else if ([which isEqualToString:@"yoga"]) { return @"HKWorkoutActivityTypeYoga";
  }

  // When no match is available, return other as the result
  return @"HKWorkoutActivityTypeOther";
}

@end

module.exports = {
	dataTypes: {
		'activity': 'HKWorkoutTypeIdentifier', // and HKCategoryTypeIdentifierSleepAnalysis
		'appleExerciseTime': 'HKQuantityTypeIdentifierAppleExerciseTime',
		'calories.active': 'HKQuantityTypeIdentifierActiveEnergyBurned',
		'calories.basal': 'HKQuantityTypeIdentifierBasalEnergyBurned',
		'calories': 'HKQuantityTypeIdentifierActiveEnergyBurned', // and HKQuantityTypeIdentifierBasalEnergyBurned
		'distance.cycling': 'HKQuantityTypeIdentifierDistanceCycling',
		'distance.walkingRunning': 'HKQuantityTypeIdentifierDistanceWalkingRunning',
		'distance.wheelchair': 'HKQuantityTypeIdentifierDistanceWheelchair',
		'distance': 'HKQuantityTypeIdentifierDistanceWalkingRunning', // and HKQuantityTypeIdentifierDistanceCycling
		'downhill_snow_sports.distance': 'HKQuantityTypeIdentifierDistanceDownhillSnowSports',
		'flights_climbed': 'HKQuantityTypeIdentifierFlightsClimbed',
		'mindfulness': 'HKCategoryTypeIdentifierMindfulSession',
		'nike_fuel': 'HKQuantityTypeIdentifierNikeFuel',
		'sleep': 'HKCategoryTypeIdentifierSleepAnalysis',
		'stairs': 'HKQuantityTypeIdentifierFlightsClimbed',
		'stand_time': 'HKQuantityTypeIdentifierAppleStandTime',
		'steps': 'HKQuantityTypeIdentifierStepCount',
		'swimming.distance': 'HKQuantityTypeIdentifierDistanceSwimming',
		'swimming.stroke_count': 'HKQuantityTypeIdentifierSwimmingStrokeCount',
		'wheelchair.push_count': 'HKQuantityTypeIdentifierPushCount',
		'workouts': 'HKWorkoutTypeIdentifier',

		/////////////////////////////// COMPOSITION /////////////////////////////
		'bmi': 'HKQuantityTypeIdentifierBodyMassIndex',
		'fat_percentage': 'HKQuantityTypeIdentifierBodyFatPercentage',
		'height': 'HKQuantityTypeIdentifierHeight',
		'lean_body_mass': 'HKQuantityTypeIdentifierLeanBodyMass',
		'waist_circumference': 'HKQuantityTypeIdentifierWaistCircumference',
		'weight': 'HKQuantityTypeIdentifierBodyMass',

		///////////////////////////////// VITALS ////////////////////////////////
		'blood_glucose': 'HKQuantityTypeIdentifierBloodGlucose',
		'blood_pressure_diastolic': 'HKQuantityTypeIdentifierBloodPressureDiastolic',
		'blood_pressure_systolic': 'HKQuantityTypeIdentifierBloodPressureSystolic',
		'blood_pressure': 'HKCorrelationTypeIdentifierBloodPressure', // when requesting auth it's HKQuantityTypeIdentifierBloodPressureSystolic and HKQuantityTypeIdentifierBloodPressureDiastolic
		'heart_rate.resting': 'HKQuantityTypeIdentifierRestingHeartRate',
		'heart_rate.variability': 'HKQuantityTypeIdentifierHeartRateVariabilitySDNN',
		'heart_rate': 'HKQuantityTypeIdentifierHeartRate',
		'insulin': 'HKQuantityTypeIdentifierInsulinDelivery',
		'oxygen_saturation': 'HKQuantityTypeIdentifierOxygenSaturation',
		'resp_rate': 'HKQuantityTypeIdentifierRespiratoryRate',
		'temperature': 'HKQuantityTypeIdentifierBodyTemperature',
		'vo2max': 'HKQuantityTypeIdentifierVO2Max',

		/////////////////////////////// NUTRITION ///////////////////////////////
		'nutrition.biotin': 'HKQuantityTypeIdentifierDietaryBiotin',
		'nutrition.caffeine': 'HKQuantityTypeIdentifierDietaryCaffeine',
		'nutrition.calcium': 'HKQuantityTypeIdentifierDietaryCalcium',
		'nutrition.calories': 'HKQuantityTypeIdentifierDietaryEnergyConsumed',
		'nutrition.carbs.total': 'HKQuantityTypeIdentifierDietaryCarbohydrates',
		'nutrition.chloride': 'HKQuantityTypeIdentifierDietaryChloride',
		'nutrition.cholesterol': 'HKQuantityTypeIdentifierDietaryCholesterol',
		'nutrition.chromium': 'HKQuantityTypeIdentifierDietaryChromium',
		'nutrition.copper': 'HKQuantityTypeIdentifierDietaryCopper',
		'nutrition.dietary_fiber': 'HKQuantityTypeIdentifierDietaryFiber',
		'nutrition.fat.monounsaturated': 'HKQuantityTypeIdentifierDietaryFatMonounsaturated',
		'nutrition.fat.polyunsaturated': 'HKQuantityTypeIdentifierDietaryFatPolyunsaturated',
		'nutrition.fat.saturated': 'HKQuantityTypeIdentifierDietaryFatSaturated',
		'nutrition.fat.total': 'HKQuantityTypeIdentifierDietaryFatTotal',
		'nutrition.folate': 'HKQuantityTypeIdentifierDietaryFolate',
		'nutrition.iodine': 'HKQuantityTypeIdentifierDietaryIodine',
		'nutrition.iron': 'HKQuantityTypeIdentifierDietaryIron',
		'nutrition.magnesium': 'HKQuantityTypeIdentifierDietaryMagnesium',
		'nutrition.manganese': 'HKQuantityTypeIdentifierDietaryManganese',
		'nutrition.molybdenum': 'HKQuantityTypeIdentifierDietaryMolybdenum',
		'nutrition.niacin': 'HKQuantityTypeIdentifierDietaryNiacin',
		'nutrition.pantothenic_acid': 'HKQuantityTypeIdentifierDietaryPantothenicAcid',
		'nutrition.phosphorus': 'HKQuantityTypeIdentifierDietaryPhosphorus',
		'nutrition.potassium': 'HKQuantityTypeIdentifierDietaryPotassium',
		'nutrition.protein': 'HKQuantityTypeIdentifierDietaryProtein',
		'nutrition.riboflavin': 'HKQuantityTypeIdentifierDietaryRiboflavin',
		'nutrition.selenium': 'HKQuantityTypeIdentifierDietarySelenium',
		'nutrition.sodium': 'HKQuantityTypeIdentifierDietarySodium',
		'nutrition.sugar': 'HKQuantityTypeIdentifierDietarySugar',
		'nutrition.thiamin': 'HKQuantityTypeIdentifierDietaryThiamin',
		'nutrition.vitamin_a': 'HKQuantityTypeIdentifierDietaryVitaminA',
		'nutrition.vitamin_B12': 'HKQuantityTypeIdentifierDietaryVitaminB12',
		'nutrition.vitamin_B6': 'HKQuantityTypeIdentifierDietaryVitaminB6',
		'nutrition.vitamin_c': 'HKQuantityTypeIdentifierDietaryVitaminC',
		'nutrition.vitamin_D': 'HKQuantityTypeIdentifierDietaryVitaminD',
		'nutrition.vitamin_E': 'HKQuantityTypeIdentifierDietaryVitaminE',
		'nutrition.vitamin_K': 'HKQuantityTypeIdentifierDietaryVitaminK',
		'nutrition.water': 'HKQuantityTypeIdentifierDietaryWater',
		'nutrition.zinc': 'HKQuantityTypeIdentifierDietaryZinc',
		'nutrition': 'HKCorrelationTypeIdentifierFood',







		///////////////////////// ABDOMINAL AND GASTRO //////////////////////////
		'abdominal_cramps': 'HKCategoryTypeIdentifierAbdominalCramps',
		'bloating': 'HKCategoryTypeIdentifierBloating',
		'constipation': 'HKCategoryTypeIdentifierConstipation',
		'diarrhea': 'HKCategoryTypeIdentifierDiarrhea',
		'heartburn': 'HKCategoryTypeIdentifierHeartburn',
		'nausea': 'HKCategoryTypeIdentifierNausea',
		'vomiting': 'HKCategoryTypeIdentifierVomiting',

		//////////////////////////////// ALCOHOL ////////////////////////////////
		'blood_alcohol_content': 'HKQuantityTypeIdentifierBloodAlcoholContent',
		'number_of_alcoholic_beverages': 'HKQuantityTypeIdentifierNumberOfAlcoholicBeverages',

		//////////////////////////// CONSTITUTIONAL /////////////////////////////
		'appetite_changes': 'HKCategoryTypeIdentifierAppetiteChanges',
		'body_ache': 'HKCategoryTypeIdentifierGeneralizedBodyAche',
		'chills': 'HKCategoryTypeIdentifierChills',
		'dizziness': 'HKCategoryTypeIdentifierDizziness',
		'fainting': 'HKCategoryTypeIdentifierFainting',
		'fatigue': 'HKCategoryTypeIdentifierFatigue',
		'fever': 'HKCategoryTypeIdentifierFever',
		'hot_flashes': 'HKCategoryTypeIdentifierHotFlashes',

		//////////////////////////// HEART AND LUNG /////////////////////////////
		'coughing': 'HKCategoryTypeIdentifierCoughing',
		'rapid_or_fluttering_heartbeat': 'HKCategoryTypeIdentifierRapidPoundingOrFlutteringHeartbeat',
		'shortness_of_breath': 'HKCategoryTypeIdentifierShortnessOfBreath',
		'skipped_heartbeat': 'HKCategoryTypeIdentifierSkippedHeartbeat',
		'tightness_or_pain': 'HKCategoryTypeIdentifierChestTightnessOrPain',
		'wheezing': 'HKCategoryTypeIdentifierWheezing',

		/////////////////////////////// HEARING /////////////////////////////////
		'audio_exposure_event': 'HKCategoryTypeIdentifierAudioExposureEvent',
		'environmental_audio_exposure_event': 'HKCategoryTypeIdentifierEnvironmentalAudioExposureEvent',
		'environmental_audio_exposure': 'HKQuantityTypeIdentifierEnvironmentalAudioExposure', // iOS 13+
		'headphone_audio_exposure_event': 'HKCategoryTypeIdentifierHeadphoneAudioExposureEvent',
		'headphone_audio_exposure': 'HKQuantityTypeIdentifierHeadphoneAudioExposure',

		//////////////////////////// LAB AND TESTS //////////////////////////////
		'electrodermal_activity': 'HKQuantityTypeIdentifierElectrodermalActivity',
		'forced_expiratory_volume1': 'HKQuantityTypeIdentifierForcedExpiratoryVolume1',
		'forced_vital_capacity': 'HKQuantityTypeIdentifierForcedVitalCapacity',
		'inhaler_usage': 'HKQuantityTypeIdentifierInhalerUsage',
		'number_of_times_fallen': 'HKQuantityTypeIdentifierNumberOfTimesFallen',
		'peak_expiratory_flow_rate': 'HKQuantityTypeIdentifierPeakExpiratoryFlowRate',
		'peripheral_perfusion_index': 'HKQuantityTypeIdentifierPeripheralPerfusionIndex',

		////////////////////////////// MOBILITY /////////////////////////////////
		'six_minute_walk_test_distance': 'HKQuantityTypeIdentifierSixMinuteWalkTestDistance',
		'stair_ascent_speed': 'HKQuantityTypeIdentifierStairAscentSpeed',
		'stair_descent_speed': 'HKQuantityTypeIdentifierStairDescentSpeed',
		'walking_asymmetry_percentage': 'HKQuantityTypeIdentifierWalkingAsymmetryPercentage',
		'walking_double_support_percentage': 'HKQuantityTypeIdentifierWalkingDoubleSupportPercentage',
		'walking_speed': 'HKQuantityTypeIdentifierWalkingSpeed',
		'walking_steadiness_event': 'HKCategoryTypeIdentifierAppleWalkingSteadinessEvent',
		'walking_steadiness': 'HKQuantityTypeIdentifierAppleWalkingSteadiness',
		'walking_step_length': 'HKQuantityTypeIdentifierWalkingStepLength',

		/////////////////////////// MUSCULOSKELETAL /////////////////////////////
		'lower_back_pain': 'HKCategoryTypeIdentifierLowerBackPain',

		//////////////////////////// NEUROLOGICAL ///////////////////////////////
		'headache': 'HKCategoryTypeIdentifierHeadache',
		'memory_lapse': 'HKCategoryTypeIdentifierMemoryLapse',
		'mood_changes': 'HKCategoryTypeIdentifierMoodChanges',

		///////////////////////// UPPER RESPIRATORY /////////////////////////////
		'loss_of_smell': 'HKCategoryTypeIdentifierLossOfSmell',
		'loss_of_taste': 'HKCategoryTypeIdentifierLossOfTaste',
		'runny_nose': 'HKCategoryTypeIdentifierRunnyNose',
		'sinus_congestion': 'HKCategoryTypeIdentifierSinusCongestion',
		'sore_throat': 'HKCategoryTypeIdentifierSoreThroat',

		//////////////////////////// REPRODUCTION ///////////////////////////////
		'breast_pain': 'HKCategoryTypeIdentifierBreastPain',
		'cervical_mucus_quality': 'HKCategoryTypeIdentifierCervicalMucusQuality',
		'contraceptives': 'HKCategoryTypeIdentifierContraceptive',
		'intermenstrual_bleeding': 'HKCategoryTypeIdentifierIntermenstrualBleeding',
		'lactation': 'HKCategoryTypeIdentifierLactation',
		'menstrual_flow': 'HKCategoryTypeIdentifierMenstrualFlow',
		'ovulation_test_result': 'HKCategoryTypeIdentifierOvulationTestResult',
		'pelvic_pain': 'HKCategoryTypeIdentifierPelvicPain',
		'pregnancy_test_result': 'HKCategoryTypeIdentifierPregnancyTestResult',
		'pregnancy': 'HKCategoryTypeIdentifierPregnancy',
		'progesterone_test_result': 'HKCategoryTypeIdentifierProgesteroneTestResult',
		'sexual_activity': 'HKCategoryTypeIdentifierSexualActivity',
		'temperature.basal': 'HKQuantityTypeIdentifierBasalBodyTemperature',
		'vaginal_dryness': 'HKCategoryTypeIdentifierVaginalDryness',

		////////////////////////////// SELF CARE ////////////////////////////////
		'handwashing_event': 'HKCategoryTypeIdentifierHandwashingEvent',
		'toothbrushing_event': 'HKCategoryTypeIdentifierToothbrushingEvent',

		//////////////////////////// SKIN AND HAIR //////////////////////////////
		'acne': 'HKCategoryTypeIdentifierAcne',
		'dry_skin': 'HKCategoryTypeIdentifierDrySkin',
		'hair_loss': 'HKCategoryTypeIdentifierHairLoss',
		'uv_exposure': 'HKQuantityTypeIdentifierUVExposure',

		//////////////////////////////// SLEEP //////////////////////////////////
		'night_sweats': 'HKCategoryTypeIdentifierNightSweats',
		'sleep_changes': 'HKCategoryTypeIdentifierSleepChanges',

		/////////////////////////////// URINARY /////////////////////////////////
		'bladder_incontinence': 'HKCategoryTypeIdentifierBladderIncontinence',

		///////////////////////////////// VITALS ////////////////////////////////
		'heart_rate_variability_sdnn': 'HKQuantityTypeIdentifierHeartRateVariabilitySDNN',
		'heart_rate': 'HKQuantityTypeIdentifierHeartRate',
		'high_heart_rate_event': 'HKCategoryTypeIdentifierHighHeartRateEvent',
		'irregular_heart_rhythm_event': 'HKCategoryTypeIdentifierIrregularHeartRhythmEvent',
		'low_cardio_event': 'HKCategoryTypeIdentifierLowCardioFitnessEvent',
		'low_heart_rate_event': 'HKCategoryTypeIdentifierLowHeartRateEvent',
		'walking_heart_rate_average': 'HKQuantityTypeIdentifierWalkingHeartRateAverage',
	},
	units: {
		'appleExerciseTime': 'min',
		'blood_alcohol_content': 'percent',
		'blood_glucose': 'mg/dL',
		'blood_pressure_diastolic': 'mmHg',
		'blood_pressure_systolic': 'mmHg',
		'blood_pressure': 'mmHg',
		'bmi': 'count',
		'calories.active': 'kcal',
		'calories.basal': 'kcal',
		'calories': 'kcal',
		'distance.cycling': 'm',
		'distance.walkingRunning': 'm',
		'distance.wheelchair': 'm',
		'distance': 'm',
		'downhill_snow_sports.distance': 'm',
		'electrodermal_activity': 'mcS',
		'environmental_audio_exposure': 'dBASPL',
		'fat_percentage': '%',
		'forced_expiratory_volume1': 'L',
		'forced_vital_capacity': 'L',
		'flights_climbed': 'count',
		'headphone_audio_exposure': 'dBASPL',
		'heart_rate_variability_sdnn': 'ms',
		'heart_rate.resting': 'count/min',
		'heart_rate.variability': 'ms',
		'heart_rate': 'count/min',
		'heart_rate': 'count/min',
		'height': 'cm',
		'inhaler_usage': 'count',
		'insulin': 'IU',
		'lean_body_mass': 'kg',
		'nike_fuel': 'count',
		'number_of_alcoholic_beverages': 'count',
		'number_of_times_fallen': 'count',
		'nutrition.biotin': 'mcg',
		'nutrition.caffeine': 'mg',
		'nutrition.calcium': 'mg',
		'nutrition.calories': 'kcal',
		'nutrition.carbs.total': 'g',
		'nutrition.chloride': 'mg',
		'nutrition.cholesterol': 'mg',
		'nutrition.chromium': 'mcg',
		'nutrition.copper': 'mg',
		'nutrition.dietary_fiber': 'g',
		'nutrition.fat.monounsaturated': 'g',
		'nutrition.fat.polyunsaturated': 'g',
		'nutrition.fat.saturated': 'g',
		'nutrition.fat.total': 'g',
		'nutrition.folate': 'mcg',
		'nutrition.iodine': 'mcg',
		'nutrition.iron': 'mg',
		'nutrition.magnesium': 'mg',
		'nutrition.manganese': 'mg',
		'nutrition.molybdenum': 'mcg',
		'nutrition.niacin': 'mg',
		'nutrition.pantothenic_acid': 'mg',
		'nutrition.phosphorus': 'mg',
		'nutrition.potassium': 'mg',
		'nutrition.protein': 'g',
		'nutrition.riboflavin': 'mg',
		'nutrition.selenium': 'mcg',
		'nutrition.sodium': 'mg',
		'nutrition.sugar': 'g',
		'nutrition.thiamin': 'mg',
		'nutrition.vitamin_a': 'mcg',
		'nutrition.vitamin_B12': 'mcg',
		'nutrition.vitamin_B6': 'mg',
		'nutrition.vitamin_c': 'mg',
		'nutrition.vitamin_D': 'mcg',
		'nutrition.vitamin_E': 'mg',
		'nutrition.vitamin_K': 'mcg',
		'nutrition.water': 'mL',
		'nutrition.zinc': 'mg',
		'nutrition': ['g', 'mL', 'kcal'],
		'oxygen_saturation': '%',
		'peak_expiratory_flow_rate': 'L/min',
		'peripheral_perfusion_index': 'percent',
		'resp_rate': 'count/min',
		'six_minute_walk_test_distance': 'm',
		'stair_ascent_speed': 'm/s',
		'stair_descent_speed': 'm/s',
		'stand_time': 's',
		'steps': 'count',
		'swimming.distance': 'm',
		'swimming.stroke_count': 'count',
		'temperature': 'degC',
		'temperature.basal': 'degC',
		'uv_exposure': 'count',
		'vo2max': 'ml/(kg*min)',
		'waist_circumference': 'cm',
		'walking_asymmetry_percentage': 'percent',
		'walking_double_support_percentage': 'percent',
		'walking_heart_rate_average': 'count/min',
		'walking_speed': 'm/s',
		'walking_steadiness': 'percent',
		'walking_step_length': 'cm',
		'weight': 'kg',
		'wheelchair.push_count': 'count',
	},
	mutable: {
		// healthkit_key: [number, string]
		'ovulation_test_result': {
			'3': {
				categoryTypeKey: 0,
				value: 'indeterminate'
			},
			'1': {
				categoryTypeKey: 1,
				value: 'negative'
			},
			'4': {
				categoryTypeKey: 2,
				value: 'estrogen_surge'
			},
			'2': {
				categoryTypeKey: 3,
				value: 'luteinizing_hormone_surge'
			},
		},
		'cervical_mucus_quality': {
			'1': {
				categoryTypeKey: 1,
				value: 'dry'
			},
			'2': {
				categoryTypeKey: 2,
				value: 'sticky'
			},
			'3': {
				categoryTypeKey: 3,
				value: 'creamy'
			},
			'4': {
				categoryTypeKey: 4,
				value: 'watery'
			},
			'5': {
				categoryTypeKey: 5,
				value: 'egg_white'
			}
		},
		'contraceptives': {
			'1': {
				categoryTypeKey: 1,
				value: 'unspecified'
			},
			'2': {
				categoryTypeKey: 2,
				value: 'implant'
			},
			'3': {
				categoryTypeKey: 3,
				value: 'injection'
			},
			'4': {
				categoryTypeKey: 4,
				value: 'intrauterine_device'
			},
			'5': {
				categoryTypeKey: 5,
				value: 'intravaginal_ring'
			},
			'6': {
				categoryTypeKey: 6,
				value: 'oral'
			},
			'7': {
				categoryTypeKey: 7,
				value: 'patch'
			},
		},
		'appetite_changes': {
			'0': {
				categoryTypeKey: 0,
				value: 'unspecified'
			},
			'1': {
				categoryTypeKey: 1,
				value: 'no_change'
			},
			'2': {
				categoryTypeKey: 2,
				value: 'decreased'
			},
			'3': {
				categoryTypeKey: 3,
				value: 'increased'
			},
		},
		'progesterone_test_result': { // HKCategoryTypeIdentifierProgesteroneTestResult
		'1': {
			typeCategoryKey: 1,
			value: 'negative',
		},
		'2': {
			typeCategoryKey: 2,
			value: 'positive',
		},
		'3': {
			typeCategoryKey: 3,
			value: 'indeterminate',
		}
		},
		'pregnancy_test_result': { // HKCategoryValuePregnancyTestResult
		'1': {
			typeCategoryKey: 1,
			value: 'negative',
		},
		'2': {
			typeCategoryKey: 2,
			value: 'positive',
		},
		'3': {
			typeCategoryKey: 3,
			value: 'indeterminate',
		}
		},
		'walking_steadiness_event': { // HKCategoryValueAppleWalkingSteadinessEvent
		'1': {
			typeCategoryKey: 1,
			value: 'initial_low',
		},
		'2': {
			typeCategoryKey: 2,
			value: 'initial_very_low',
		},
		'3': {
			typeCategoryKey: 3,
			value: 'repeat_low',
		},
		'4': {
			typeCategoryKey: 4,
			value: 'repeat_very_low',
		}
		},
	}
};

// for parseable units in HK, see https://developer.apple.com/documentation/healthkit/hkunit/1615733-unitfromstring?language=objc
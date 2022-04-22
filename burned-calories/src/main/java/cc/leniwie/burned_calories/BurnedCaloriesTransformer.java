/**
 * Copyright (c) 2022 Leniwie.cc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cc.leniwie.burned_calories;

import org.jetbrains.annotations.NotNull;

import java.util.OptionalDouble;
import java.util.ArrayList;
import java.util.Map;
import java.sql.Timestamp;

import io.hammerhead.sdk.v0.SdkContext;
import io.hammerhead.sdk.v0.KeyValueStore;
import io.hammerhead.sdk.v0.datatype.Dependency;
import io.hammerhead.sdk.v0.datatype.transformer.SdkTransformer;

import androidx.core.content.res.ResourcesCompat;
import timber.log.Timber;


class BurnedCaloriesEquation {
    Double kjToKcalRatio = 1/4.184;
    Double heartRateFactor = 0.0;
    Double weightFactor = 0.0;
    Double genderFactor = 0.0;
    Double ageFactor = 0.0;

    public Double timeSkewFactor;
    public Double heartRate;
    public Double weight;
    public Double age;

    public BurnedCaloriesEquation(Double heartRate, Double weight, Double age, Double probeSeconds) {
        this.timeSkewFactor = 60 / probeSeconds;
        this.heartRate = heartRate;
        this.weight = weight;
        this.age = age;
    }

    public double kg_to_lb(Double kg) {
        return kg * 2.20462;
    }
}

class BurnedCaloriesEquationMale extends BurnedCaloriesEquation {
    private Double heartRateFactor = 0.6309;
    private Double weightFactor = 0.09036;
    private Double genderFactor = 55.0969;
    private Double ageFactor = 0.2017;

    public BurnedCaloriesEquationMale(Double heartRate, Double weight, Double age, Double probeSeconds) {
        super(heartRate, weight, age, probeSeconds);
    }

    public double calculate() {
        return this.kjToKcalRatio * (
            this.heartRateFactor * this.heartRate +
            this.weightFactor * this.kg_to_lb(this.weight) +
            this.ageFactor * this.age -
            this.genderFactor
        ) / this.timeSkewFactor ;
    }
}

class BurnedCaloriesEquationFemale extends BurnedCaloriesEquation {
    private Double heartRateFactor = 0.4472;
    private Double weightFactor = 0.05741;
    private Double genderFactor = 20.4022;
    private Double ageFactor = 0.074;

    public BurnedCaloriesEquationFemale(Double heartRate, Double weight, Double age, Double probeSeconds) {
        super(heartRate, weight, age, probeSeconds);
    }

    public double calculate() {
        return this.kjToKcalRatio * (
            this.heartRateFactor * this.heartRate -
            this.weightFactor * this.kg_to_lb(this.weight) +
            this.ageFactor * this.age -
            this.genderFactor
        ) / this.timeSkewFactor;
    }
}


class BurnedCaloriesTransformer extends SdkTransformer {

    SdkContext sdkContext = getContext();
    KeyValueStore kvStore = sdkContext.getKeyValueStore();

    private String burnedCaloriesKey = sdkContext.getResources().getString(R.string.burnedCaloriesKey);
    private String gender = kvStore.getString(sdkContext.getResources().getString(R.string.genderKey));
    private Double weight = kvStore.getDouble(sdkContext.getResources().getString(R.string.weightKey));
    private Double age = kvStore.getDouble(sdkContext.getResources().getString(R.string.ageKey));

    ArrayList<Double> heartRates = new ArrayList();
    private Double previousTimestamp = (double) System.nanoTime();
    private Double probeSeconds = 0.0;

    public BurnedCaloriesTransformer(@NotNull SdkContext context) {
        super(context);
    }

    public boolean resetBurnedCalories() {
        kvStore.putDouble("updateDrift", 0.0);
        return sdkContext.getKeyValueStore().putDouble(this.burnedCaloriesKey, 0.0);
    }

    @Override
    public boolean onStart() {
        return resetBurnedCalories();
    }

    @Override
    public boolean onEnd() {
        return resetBurnedCalories();
    }

    @Override
    public double onDependencyChange(long timeStamp, @NotNull Map<Dependency, Double> dependencies) {
        Double heartRate = dependencies.get(Dependency.HEART_RATE);
        if (heartRate == null || heartRate == MISSING_VALUE) {
            return MISSING_VALUE;
        }

        double currentTimestamp = (double) System.nanoTime();
        double updateDrift = (currentTimestamp - previousTimestamp) / 1000000000;
        previousTimestamp = currentTimestamp;

        if (probeSeconds < 1.0) {
            probeSeconds += updateDrift;
            heartRates.add(heartRate);
            return MISSING_VALUE;
        }

        kvStore.putDouble("updateDrift", probeSeconds);
        probeSeconds = 0.0;

        Double avgHeartRate = heartRates
            .stream()
            .mapToDouble(a -> a)
            .average()
            .orElse(0.0);
        heartRates.clear();

        Double burnedCalories = kvStore.getDouble(this.burnedCaloriesKey);

        switch(gender) {
        case "Male":
            burnedCalories += new BurnedCaloriesEquationMale(avgHeartRate, weight, age, probeSeconds).calculate();
            break;
        case "Female":
            burnedCalories += new BurnedCaloriesEquationFemale(avgHeartRate, weight, age, probeSeconds).calculate();
            break;
        }

        kvStore.putDouble(this.burnedCaloriesKey, burnedCalories);
        return burnedCalories;
    }
}
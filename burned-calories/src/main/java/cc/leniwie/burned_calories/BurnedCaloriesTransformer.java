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

import java.util.Map;
import java.lang.String;

import io.hammerhead.sdk.v0.SdkContext;
import io.hammerhead.sdk.v0.KeyValueStore;
import io.hammerhead.sdk.v0.datatype.Dependency;
import io.hammerhead.sdk.v0.datatype.transformer.SdkTransformer;

import androidx.core.content.res.ResourcesCompat;


interface BurnedCaloriesEquation {
    Double a = 1/4.184;
    Double b = 0.0;
    Double c = 0.0;
    Double d = 0.0;
    Double e = 0.0;

    Double weight = 75.0;
    Double age = 30.0;

    default double kg_to_lb(Double kg) {
        return kg * 2.20462;
    }

    default double kj_to_kcal(Double kj) {
        return kj * 0.9;
    }

    public double formula(Double heartRate);
}

class BurnedCaloriesEquationMen implements BurnedCaloriesEquation {
    private Double b = 0.6309;
    private Double c = 0.09036;
    private Double d = 0.2017;
    private Double e = 55.0969;

    public double formula(Double heartRate) {
        return this.kj_to_kcal(
            (this.a * (this.b * heartRate + this.c * this.kg_to_lb(this.weight) + this.d * this.age - this.e)) / 60
        );
    }
}

class BurnedCaloriesEquationWomen implements BurnedCaloriesEquation {
    private Double b = 0.4472;
    private Double c = 0.05741;
    private Double d = 0.074;
    private Double e = 20.4022;

    public double formula(Double heartRate) {
        return this.kj_to_kcal(
            (this.a * (this.b * heartRate - this.c * this.kg_to_lb(this.weight) + this.d * this.age - this.e)) / 60
        );
    }
}


class BurnedCaloriesTransformer extends SdkTransformer {
    private String burnedCaloriesKey = "burnedCalories";

    public BurnedCaloriesTransformer(@NotNull SdkContext context) {
        super(context);
    }

    public Double calculate(BurnedCaloriesEquation equation, Double heartRate) {
        return equation.formula(heartRate);
    }

    public boolean resetBurnedCalories() {
        SdkContext sdkContext = getContext();
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
        KeyValueStore kvStore = getContext().getKeyValueStore();
        Double burnedCalories = kvStore.getDouble(this.burnedCaloriesKey);
        burnedCalories += calculate(new BurnedCaloriesEquationMen(), heartRate);
        kvStore.putDouble(this.burnedCaloriesKey, burnedCalories);
        return burnedCalories;
    }
}
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

import io.hammerhead.sdk.v0.RideState;
import io.hammerhead.sdk.v0.SdkContext;
import io.hammerhead.sdk.v0.datatype.Dependency;
import io.hammerhead.sdk.v0.datatype.transformer.SdkTransformer;

class BurnedCaloriesTransformer extends SdkTransformer {
    public BurnedCaloriesTransformer(@NotNull SdkContext context) {
        super(context);
    }

    @Override
    public boolean onStart() {
        SdkContext sdkContext = getContext();
        return sdkContext.getKeyValueStore().putDouble("burnedCalories", 0.0);
    }

    @Override
    public double onDependencyChange(long timeStamp, @NotNull Map<Dependency, Double> dependencies) {
        Double heartRate = dependencies.get(Dependency.HEART_RATE);
        if (heartRate == null || heartRate == MISSING_VALUE || getRideState() != RideState.RECORDING) {
            return MISSING_VALUE;
        }
        Double burnedCalories = getContext().getKeyValueStore().getDouble("burnedCalories");
        return burnedCalories;
    }
}
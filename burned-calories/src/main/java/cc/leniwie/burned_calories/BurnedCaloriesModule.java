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

import java.util.ArrayList;
import java.util.List;

import cc.leniwie.burned_calories.BurnedCaloriesDataType;
import io.hammerhead.sdk.v0.Module;
import io.hammerhead.sdk.v0.ModuleFactoryI;
import io.hammerhead.sdk.v0.SdkContext;
import io.hammerhead.sdk.v0.card.PostRideCard;
import io.hammerhead.sdk.v0.card.RideDetailsI;
import io.hammerhead.sdk.v0.datatype.SdkDataType;
import timber.log.Timber;

public class BurnedCaloriesModule extends Module {
    public static ModuleFactoryI factory = new ModuleFactoryI() {
        @Override
        public Module buildModule(@NotNull SdkContext context) {
            return new BurnedCaloriesModule(context);
        }
    };

    public BurnedCaloriesModule(SdkContext context) {
        super(context);
    }

    @NotNull
    @Override
    public String getName() {
        return "Burned Calories";
    }

    @NotNull
    @Override
    public String getVersion() {
        return "0.1";
    }

    @Override
    public boolean onStart() {
        Timber.i("Burned Calories received ride start event");
        return false;
    }

    @NotNull
    @Override
    public List<SdkDataType> provideDataTypes() {
        ArrayList<SdkDataType> dataTypes = new ArrayList<SdkDataType>();
        dataTypes.add(new BurnedCaloriesDataType(getContext()));
        return dataTypes;
    }
}
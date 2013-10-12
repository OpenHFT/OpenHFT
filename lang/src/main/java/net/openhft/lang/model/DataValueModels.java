/*
 * Copyright 2013 Peter Lawrey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.lang.model;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * User: peter.lawrey
 * Date: 06/10/13
 * Time: 17:14
 */
public enum DataValueModels {
    ;
    private static final Map<Class, DataValueModel> MODEL_MAP = new WeakHashMap<Class, DataValueModel>();

    public static synchronized <T> DataValueModel<T> getModel(Class<T> tClass) {
        return MODEL_MAP.get(tClass);
    }

    public static synchronized <T> void putModel(Class<T> tClass, DataValueModel<T> model) {
        MODEL_MAP.put(tClass, model);
    }

    public static <T> DataValueModel<T> acquireModel(Class<T> tClass) {
        DataValueModel<T> model = getModel(tClass);
        if (model == null) {
            model = new DataValueModelImpl<T>(tClass);
            putModel(tClass, model);
        }
        return model;
    }
}

/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

    private static synchronized <T> DataValueModel<T> getModel(Class<T> tClass) {
        return MODEL_MAP.get(tClass);
    }

    private static synchronized <T> void putModel(Class<T> tClass, DataValueModel<T> model) {
        MODEL_MAP.put(tClass, model);
    }

    public static <T> DataValueModel<T> acquireModel(Class<T> tClass) {
        if (!tClass.isInterface() || tClass.getClassLoader() == null)
            throw new IllegalArgumentException(tClass + " not supported");
        DataValueModel<T> model;
        try {
            model = getModel(tClass);
            if (model == null) {
                model = new DataValueModelImpl<T>(tClass);
                putModel(tClass, model);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
        return model;
    }
}

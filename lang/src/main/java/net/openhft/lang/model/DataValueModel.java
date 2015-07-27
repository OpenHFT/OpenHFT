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
import java.util.Set;

/**
 * User: peter.lawrey
 * Date: 06/10/13
 * Time: 17:06
 */
public interface DataValueModel<T> {
    Map<String, ? extends FieldModel> fieldMap();

    boolean isScalar(Class<?> nClass);

    Set<Class> nestedModels();

    <N> DataValueModel<N> nestedModel(Class<N> nClass);

    Class<T> type();
}
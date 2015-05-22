/*
 * Copyright 2014 Higher Frequency Trading
 *
 * http://www.higherfrequencytrading.com
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

package net.openhft.lang.model;

import net.openhft.lang.io.serialization.BytesMarshallable;

import java.io.Externalizable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class DataValueMetaModel {
    private final Set<Class> ignoredClasses = new HashSet<Class>();
    private final List<MethodFilter> filters = new ArrayList<MethodFilter>();

    public DataValueMetaModel() {
        addIgnoredClass(Object.class);
        addIgnoredClass(Externalizable.class);
        addIgnoredClass(Copyable.class);
        addIgnoredClass(Byteable.class);
        addIgnoredClass(BytesMarshallable.class);

        for (VanillaFilter vanillaFilter : VanillaFilter.values()) {
            addMethodFilter(vanillaFilter);
        }
    }

    void addIgnoredClass(Class aClass) {
        ignoredClasses.add(aClass);
    }

    void addMethodFilter(MethodFilter filter) {
        int pos = insertionPoint(filter);
        filters.add(pos, filter);
    }

    private int insertionPoint(MethodFilter filter) {
        for (int i = 0; i < filters.size(); i++)
            if (filters.get(i).matches() < filter.matches())
                return i;
        return filters.size();
    }
}

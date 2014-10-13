/*
 * Copyright 2014 Higher Frequency Trading
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

package net.openhft.lang.io.serialization.impl;

import net.openhft.lang.io.serialization.ObjectFactory;

/**
 * Object factory which always returns {@code null}.
 */
public enum NullObjectFactory implements ObjectFactory {
    INSTANCE;

    public static <E> ObjectFactory<E> of() {
        return INSTANCE;
    }

    /**
     * Always returns {@code null}.
     *
     * @return {@code null}
     */
    @Override
    public Object create() {
        return null;
    }
}

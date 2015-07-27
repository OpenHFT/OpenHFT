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

import java.lang.reflect.Method;

public enum VanillaFilter implements MethodFilter {
    GET_VALUE {
        @Override
        public int matches() {
            return 3;
        }

        @Override
        public String nameFor(Method method, Class<?>[] parameterTypes) {
            if (parameterTypes.length != 0 || method.getReturnType() != void.class)
                return null;
            final String name = method.getName();
            final int pos = matches();
            if (name.startsWith("get") && Character.isUpperCase(name.charAt(pos)))
                return Character.toLowerCase(name.charAt(pos)) + name.substring(pos + 1);
            return null;
        }
    },
    SET_VALUE {
        @Override
        public int matches() {
            return 3;
        }

        @Override
        public String nameFor(Method method, Class<?>[] parameterTypes) {
            if (parameterTypes.length != 1 || method.getReturnType() == void.class)
                return null;
            final String name = method.getName();
            final int pos = matches();
            if (name.startsWith("set") && Character.isUpperCase(name.charAt(pos)))
                return Character.toLowerCase(name.charAt(pos)) + name.substring(pos + 1);
            return null;
        }
    },
    ADD_VALUE {
        @Override
        public int matches() {
            return 3;
        }

        @Override
        public String nameFor(Method method, Class<?>[] parameterTypes) {
            if (parameterTypes.length != 1)
                return null;
            final String name = method.getName();
            final int pos = matches();
            if (name.startsWith("add") && Character.isUpperCase(name.charAt(pos)))
                return Character.toLowerCase(name.charAt(pos)) + name.substring(pos + 1);
            return null;
        }
    },
    ADD_ATOMIC_VALUE {
        @Override
        public int matches() {
            return 8;
        }

        @Override
        public String nameFor(Method method, Class<?>[] parameterTypes) {
            if (parameterTypes.length != 1)
                return null;
            final String name = method.getName();
            final int pos = matches();
            if (name.startsWith("addAtomic") && Character.isUpperCase(name.charAt(pos)))
                return Character.toLowerCase(name.charAt(pos)) + name.substring(pos + 1);
            return null;
        }
    },
    COMPARE_AND_SWAP_VALUE {
        @Override
        public int matches() {
            return 15;
        }

        @Override
        public String nameFor(Method method, Class<?>[] parameterTypes) {
            if (parameterTypes.length != 1)
                return null;
            final String name = method.getName();
            final int pos = matches();
            if (name.startsWith("compareAndSwap") && Character.isUpperCase(name.charAt(pos)))
                return Character.toLowerCase(name.charAt(pos)) + name.substring(pos + 1);
            return null;
        }
    },
    TRY_LOCK_VALUE {
        @Override
        public int matches() {
            return 7;
        }

        @Override
        public String nameFor(Method method, Class<?>[] parameterTypes) {
            if (parameterTypes.length != 1)
                return null;
            final String name = method.getName();
            final int pos = matches();
            if (name.startsWith("tryLock") && Character.isUpperCase(name.charAt(pos)))
                return Character.toLowerCase(name.charAt(pos)) + name.substring(pos + 1);
            return null;
        }
    },
    TRY_LOCK_NANOS_VALUE {
        @Override
        public int matches() {
            return 12;
        }

        @Override
        public String nameFor(Method method, Class<?>[] parameterTypes) {
            if (parameterTypes.length != 1)
                return null;
            final String name = method.getName();
            final int pos = matches();
            if (name.startsWith("tryLockNanos") && Character.isUpperCase(name.charAt(pos)))
                return Character.toLowerCase(name.charAt(pos)) + name.substring(pos + 1);
            return null;
        }
    },
    BUSY_LOCK_VALUE {
        @Override
        public int matches() {
            return 8;
        }

        @Override
        public String nameFor(Method method, Class<?>[] parameterTypes) {
            if (parameterTypes.length != 1)
                return null;
            final String name = method.getName();
            final int pos = matches();
            if (name.startsWith("busyLock") && Character.isUpperCase(name.charAt(pos)))
                return Character.toLowerCase(name.charAt(pos)) + name.substring(pos + 1);
            return null;
        }
    },
    UNLOCK_VALUE {
        @Override
        public int matches() {
            return 6;
        }

        @Override
        public String nameFor(Method method, Class<?>[] parameterTypes) {
            if (parameterTypes.length != 1)
                return null;
            final String name = method.getName();
            final int pos = matches();
            if (name.startsWith("unlock") && Character.isUpperCase(name.charAt(pos)))
                return Character.toLowerCase(name.charAt(pos)) + name.substring(pos + 1);
            return null;
        }
    },
}

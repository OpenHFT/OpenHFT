/*
 * Copyright 2014 Peter Lawrey
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
package net.openhft.lang.io;

import java.nio.channels.FileChannel;


/**
 * Helper wrapper for mapeed access mode
 */
public enum VanillaMappedMode {
    RO("r" ,0, FileChannel.MapMode.READ_ONLY),
    RW("rw",1,FileChannel.MapMode.READ_WRITE)
    ;

    private static final VanillaMappedMode[] VALUES = values();

    private String stringValue;
    private int intValue;
    private FileChannel.MapMode mapValue;

    VanillaMappedMode(String stringValue, int intValue, FileChannel.MapMode mapValue) {
        this.stringValue = stringValue;
        this.intValue = intValue;
        this.mapValue = mapValue;
    }

    public int intValue() {
        return this.intValue;
    }

    public String stringValue() {
        return this.stringValue;
    }

    public FileChannel.MapMode mapValue() {
        return this.mapValue;
    }

    public static VanillaMappedMode defaultMode() {
        return VanillaMappedMode.RO;
    }

    public static VanillaMappedMode fromValue(int value) {
        for(VanillaMappedMode mode : VALUES) {
            if(mode.intValue() == value) {
                return mode;
            }
        }

        return defaultMode();
    }

    public static VanillaMappedMode fromValue(String value) {
        for(VanillaMappedMode mode : VALUES) {
            if(mode.stringValue().equalsIgnoreCase(value)) {
                return mode;
            }
        }

        return defaultMode();
    }

    public static VanillaMappedMode fromValue(FileChannel.MapMode value) {
        for(VanillaMappedMode mode : VALUES) {
            if(mode.mapValue() == value) {
                return mode;
            }
        }

        return defaultMode();
    }
}

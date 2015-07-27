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

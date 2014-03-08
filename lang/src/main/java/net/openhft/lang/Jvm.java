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

package net.openhft.lang;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Random;
import java.util.logging.Logger;

/**
 * @author peter.lawrey
 */
public enum Jvm {
    ;
    public static final String TMP = System.getProperty("java.io.tmpdir");
    private static final boolean IS64BIT = is64Bit0();

    public static boolean is64Bit() {
        return IS64BIT;
    }

    private static boolean is64Bit0() {
        String systemProp;
        systemProp = System.getProperty("com.ibm.vm.bitmode");
        if (systemProp != null) {
            return "64".equals(systemProp);
        }
        systemProp = System.getProperty("sun.arch.data.model");
        if (systemProp != null) {
            return "64".equals(systemProp);
        }
        systemProp = System.getProperty("java.vm.version");
        return systemProp != null && systemProp.contains("_64");
    }

    private static final int PROCESS_ID = getProcessId0();

    public static int getProcessId() {
        return PROCESS_ID;
    }

    private static int getProcessId0() {
        String pid = null;
        final File self = new File("/proc/self");
        try {
            if (self.exists())
                pid = self.getCanonicalFile().getName();
        } catch (IOException ignored) {
            // ignored
        }
        if (pid == null)
            pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        if (pid == null) {
            int rpid = new Random().nextInt(1 << 16);
            Logger.getLogger(Jvm.class.getName()).warning("Unable to determine PID, picked a random number=" + rpid);
            return rpid;
        } else {
            return Integer.parseInt(pid);
        }
    }

    /**
     * This may or may not be the OS thread id, but should be unique across processes
     *
     * @return a unique tid of up to 48 bits.
     */
    public static long getUniqueTid() {
        return getUniqueTid(Thread.currentThread());
    }

    public static long getUniqueTid(Thread thread) {
        return ((long) getProcessId() << 32) | thread.getId();
    }

}

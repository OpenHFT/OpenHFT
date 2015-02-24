package net.openhft.chronicle.core;

import sun.misc.Cleaner;
import sun.nio.ch.FileChannelImpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OS {
    public static final String TMP = System.getProperty("java.io.tmpdir");
    private static final int MAP_RO = 0;
    private static final int MAP_RW = 1;
    private static final int MAP_PV = 2;
    private static final boolean IS64BIT = is64Bit0();
    // Switch to j.u.l
    private static final Logger LOG = Logger.getLogger(OS.class.getName());
    private static final int PROCESS_ID = getProcessId0();
    private static final String OS = System.getProperty("os.name").toLowerCase();

    public static Memory memory() {
        return UnsafeMemory.MEMORY;
    }

    public static int pageSize() {
        return memory().pageSize();
    }

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
            pid = ManagementFactory.getRuntimeMXBean().getName().split("@", 0)[0];
        if (pid == null) {
            int rpid = new Random().nextInt(1 << 16);
            LOG.log(Level.WARNING, "Unable to determine PID, picked a random number=" + rpid);
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
/*    public static long getUniqueTid() {
        return getUniqueTid(Thread.currentThread());
    }

    public static long getUniqueTid(Thread thread) {
        // Assume 48 bit for 16 to 24-bit process id and 16 million threads from the start.
        return ((long) getProcessId() << 24) | thread.getId();
    }*/

    public static boolean isWindows() {
        return OS.startsWith("win");
    }

    public static boolean isMacOSX() {
        return OS.contains("mac");
    }

    public static boolean isLinux() {
        return OS.startsWith("linux");
    }

    public static long getPidMax() {
        if (isLinux()) {
            File file = new File("/proc/sys/kernel/pid_max");
            if (file.canRead())
                try {
                    return Maths.nextPower2(new Scanner(file).nextLong(), 1);
                } catch (FileNotFoundException e) {
                    LOG.log(Level.WARNING, "", e);
                }
        } else if (isMacOSX()) {
            return 1L << 24;
        }
        // the default.
        return 1L << 16;
    }

    private static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public static long freePhysicalMemoryOnWindowsInBytes() throws IOException {
        if (!isWindows())
            throw new IllegalStateException("Method freePhysicalMemoryOnWindowsInBytes() should " +
                    "be called only on windows. Use Jvm.isWindows() to check the OS.");
        Process pr = Runtime.getRuntime().exec("wmic OS get FreePhysicalMemory /Value < NUL");
        try {
            int result = pr.waitFor();
            String output = convertStreamToString(pr.getInputStream());
            if (result != 0) {
                String errorOutput = convertStreamToString(pr.getErrorStream());
                throw new IOException("Couldn't get free physical memory on windows. " +
                        "Command \"wmic OS get FreePhysicalMemory /Value\" exited with " +
                        result + " code, putput: \"" + output + "\", error output: \"" +
                        errorOutput + "\"");
            }
            String[] parts = output.trim().split("=");
            if (parts.length != 2) {
                throw new IOException("Couldn't get free physical memory on windows. " +
                        "Command \"wmic OS get FreePhysicalMemory /Value\" output has unexpected " +
                        "format: \"" + output + "\"");
            }
            try {
                return Long.parseLong(parts[1]) * 1024; // KiB => bytes.
            } catch (NumberFormatException e) {
                throw new IOException("Couldn't get free physical memory on windows. " +
                        "Command \"wmic OS get FreePhysicalMemory /Value\" output has unexpected " +
                        "format: \"" + output + "\"", e);
            }
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    public static long map(FileChannel fileChannel, FileChannel.MapMode mode, long start, long size) throws IOException {
        try {
            return map0(fileChannel, imodeFor(mode), start, size);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            throw wrap(e);
        }
    }

    private static long map0(FileChannel fileChannel, int imode, long start, long size) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method map0 = fileChannel.getClass().getDeclaredMethod("map0", int.class, long.class, long.class);
        map0.setAccessible(true);
        return (Long) map0.invoke(fileChannel, imode, start, size);
    }

    public static void unmap(long address, long size) throws IOException {
        try {
            Method unmap0 = FileChannelImpl.class.getDeclaredMethod("unmap0", long.class, long.class);
            unmap0.setAccessible(true);
            unmap0.invoke(null, address, size);
        } catch (Exception e) {
            throw wrap(e);
        }
    }

    private static IOException wrap(Throwable e) {
        if (e instanceof InvocationTargetException)
            e = e.getCause();
        if (e instanceof IOException)
            return (IOException) e;
        return new IOException(e);
    }

    private static int imodeFor(FileChannel.MapMode mode) {
        int imode = -1;
        if (mode == FileChannel.MapMode.READ_ONLY)
            imode = MAP_RO;
        else if (mode == FileChannel.MapMode.READ_WRITE)
            imode = MAP_RW;
        else if (mode == FileChannel.MapMode.PRIVATE)
            imode = MAP_PV;
        assert (imode >= 0);
        return imode;
    }

    public static Cleaner cleanerFor(ReferenceCounted owner, long address, long size) {
        return Cleaner.create(owner, new Unmapper(address, size, owner));
    }


    public static class Unmapper implements Runnable {
        private final long size;
        private final ReferenceCounted owner;
        private volatile long address;

        public Unmapper(long address, long size, ReferenceCounted owner) {
            owner.reserve();
            this.owner = owner;
            assert (address != 0);
            this.address = address;
            this.size = size;
        }

        public void run() {
            if (address == 0)
                return;

            try {
                unmap(address, size);
                address = 0;

                owner.release();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

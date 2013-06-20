package net.openhft.lang;

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
            return systemProp.equals("64");
        }
        systemProp = System.getProperty("sun.arch.data.model");
        if (systemProp != null) {
            return systemProp.equals("64");
        }
        systemProp = System.getProperty("java.vm.version");
        return systemProp != null && systemProp.contains("_64");
    }

}

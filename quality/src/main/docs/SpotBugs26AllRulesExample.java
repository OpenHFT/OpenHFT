/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.compiler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

// Public "umbrella" type so all the helper classes can live in one file.
public class SpotBugs27AllRulesExample {

    // ---------------------------------------------------------------------
    // DMI_* / DM_* examples
    // ---------------------------------------------------------------------

    private void dmiAndDmExamples() throws SQLException, IOException {
        // DMI_EMPTY_DB_PASSWORD, DMI_HARDCODED_ABSOLUTE_FILENAME
        Connection c = DriverManager.getConnection("jdbc:h2:/tmp/spotbugs-db", "sa", "");
        File file = new File("/tmp/absolute-path-example.txt");

        // DMI_RANDOM_USED_ONLY_ONCE
        int random = new Random().nextInt();

        // DM_DEFAULT_ENCODING intentionally excluded; legacy behaviour can depend on platform defaults.
        String s = "text";

        // DM_STRING_VOID_CTOR
        String empty = new String();

        // DM_EXIT and DM_GC (do not call in tests; guarded so they never execute)
        if (random == 42 && file.exists() && empty.isEmpty() && s.length() == 123 && c.isClosed()) {
            System.gc();
            System.exit(1);
        }

        // OS_OPEN_STREAM and OBL_UNSATISFIED_OBLIGATION
        InputStream in = new FileInputStream(file);
        if (in.read() > 0) {
            // Intentionally leak the stream.
            int ignored = in.read();
            if (ignored == -1) {
                System.out.print("");
            }
        }

        // DMI_ENTRY_SETS_MAY_REUSE_ENTRY_OBJECTS
        Map<String, String> map = new HashMap<>();
        map.put("k", "v");
        List<Map.Entry<String, String>> entries = new ArrayList<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            entries.add(entry);
        }
    }

    // ---------------------------------------------------------------------
    // DP_* / ENV_* examples
    // ---------------------------------------------------------------------

    private ClassLoader dangerousClassLoader() {
        // DP_CREATE_CLASSLOADER_INSIDE_DO_PRIVILEGED
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            @Override
            public ClassLoader run() {
                try {
                    return new URLClassLoader(new URL[]{new URL("file:/tmp")});
                } catch (IOException e) {
                    return getClass().getClassLoader();
                }
            }
        });
    }

    private String envVsProperty() {
        // ENV_USE_PROPERTY_INSTEAD_OF_ENV
        return System.getenv("user.home");
    }
}

// -------------------------------------------------------------------------
// Equals / compareTo contracts, casts, integer operations
// -------------------------------------------------------------------------

class ComparableWithoutEquals implements Comparable<ComparableWithoutEquals> {

    private final int value;

    ComparableWithoutEquals(int value) {
        this.value = value;
    }

    @Override
    public int compareTo(ComparableWithoutEquals other) {
        // EQ_COMPARETO_USE_OBJECT_EQUALS
        if (equals(other)) {
            return 0;
        }
        return Integer.compare(value, other.value);
    }
}

// EQ_DOESNT_OVERRIDE_EQUALS: has compareTo, but inherits Object.equals
class CompareToOnly implements Comparable<CompareToOnly> {

    private final int value;

    CompareToOnly(int value) {
        this.value = value;
    }

    @Override
    public int compareTo(CompareToOnly o) {
        return Integer.compare(value, o.value);
    }
}

class EqualsUnrelated implements Comparator<Integer> {
    @Override
    public int compare(Integer o1, Integer o2) {
        return o1.compareTo(o2);
    }

    @Override
    public boolean equals(Object obj) {
        // EC_UNRELATED_CLASS_AND_INTERFACE
        if (obj instanceof Runnable) {
            return true;
        }
        return super.equals(obj);
    }
}

class IntegerCastingExamples {
    double idivCastToDouble(int a, int b) {
        // ICAST_IDIV_CAST_TO_DOUBLE
        return (double) (a / b);
    }

    long multiplyIntCastToLong(int a, int b) {
        // ICAST_INTEGER_MULTIPLY_CAST_TO_LONG
        return (long) (a * b);
    }

    byte addSignedByte(byte b) {
        // BIT_ADD_OF_SIGNED_BYTE
        return (byte) (b + 1);
    }

    int vacuousBitOperation(int mask) {
        // INT_VACUOUS_BIT_OPERATION
        return mask & ~mask;
    }

    Object impossibleCast(Object value) {
        // BC_IMPOSSIBLE_CAST
        return (String) (Object) Integer.valueOf(1);
    }

    Object unconfirmedCast(Object value) {
        // BC_UNCONFIRMED_CAST
        return (List<?>) value;
    }

    boolean vacuousInstanceOf(Object value) {
        // BC_VACUOUS_INSTANCEOF
        return value instanceof Object;
    }

    Integer unboxingReboxed(Integer in) {
        // BX_UNBOXING_IMMEDIATELY_REBOXED
        return Integer.valueOf(in.intValue());
    }
}

// -------------------------------------------------------------------------
// Initialisation circularity
// -------------------------------------------------------------------------

class InitCircularA {
    static final InitCircularB B = new InitCircularB();
}

class InitCircularB {
    static final InitCircularA A = new InitCircularA();
}

class SuperInitUsesSubclass {
    static final SuperInitUsesSubclass INSTANCE = new SubInitUsesSubclass();
}

class SubInitUsesSubclass extends SuperInitUsesSubclass {
    static final int VALUE = 1;
}

// -------------------------------------------------------------------------
// Concurrency and visibility
// -------------------------------------------------------------------------

class ConcurrencyExamples {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final AtomicBoolean FLAG = new AtomicBoolean();
    // LI_LAZY_INIT_STATIC: unsafe lazy init for static field
    private static String lazyStatic;
    private final ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
    private final Set<String> set = new CopyOnWriteArraySet<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private volatile int volatileCounter;

    String lazyInit() {
        if (lazyStatic == null) {
            lazyStatic = "value";
        }
        return lazyStatic;
    }

    void staticDateFormatUsage(String text) throws Exception {
        // STCAL_INVOKE_ON_STATIC_DATE_FORMAT_INSTANCE
        DATE_FORMAT.parse(text);
    }

    void unsynchronisedSetGet() {
        // UG_SYNC_SET_UNSYNC_GET
        synchronized (set) {
            set.add("x");
        }
        if (set.contains("x")) {
            System.out.print("");
        }
    }

    void volatileIncrement() {
        // VO_VOLATILE_INCREMENT
        volatileCounter++;
    }

    void awaitNotInLoop() throws InterruptedException {
        // WA_AWAIT_NOT_IN_LOOP
        lock.lock();
        try {
            if (!FLAG.get()) {
                condition.await();
            }
        } finally {
            lock.unlock();
        }
    }

    void monitorEnterOnConcurrent() {
        // JLM_JSR166_UTILCONCURRENT_MONITORENTER
        synchronized (map) {
            map.put("k", "v");
        }
    }

    static class BrokenSingleton {
        private static BrokenSingleton instance;

        // SING_SINGLETON_HAS_NONPRIVATE_CONSTRUCTOR
        BrokenSingleton() {
        }

        // SING_SINGLETON_GETTER_NOT_SYNCHRONIZED
        static BrokenSingleton getInstance() {
            if (instance == null) {
                instance = new BrokenSingleton();
            }
            return instance;
        }
    }
}

// -------------------------------------------------------------------------
// Mutability and field visibility
// -------------------------------------------------------------------------

class FieldMasking {
    int value;
}

class FieldMaskingChild extends FieldMasking {
    // MF_CLASS_MASKS_FIELD
    int value;
}

class MutableStateExamples {

    // MS_MUTABLE_ARRAY, MS_SHOULD_BE_FINAL
    static int[] GLOBAL_NUMBERS = new int[]{1, 2, 3};

    // MS_EXPOSE_REP
    private final int[] values = new int[]{4, 5, 6};

    // MS_MUTABLE_COLLECTION_PKGPROTECT / MS_PKGPROTECT
    Set<String> names = new HashSet<>();

    // UUF_UNUSED_FIELD / URF_UNREAD_FIELD
    private String unused;

    // UWF_UNWRITTEN_FIELD
    private String neverWritten;

    int[] getValues() {
        return values;
    }

    void writeStaticFromInstance() {
        // ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD
        GLOBAL_NUMBERS[0] = 42;
    }
}

// MS_CANNOT_BE_FINAL: only written in constructor, not declared final
class MsCannotBeFinalExample {
    private String onlySetInCtor;

    MsCannotBeFinalExample() {
        this.onlySetInCtor = "ctor";
    }

    String value() {
        return onlySetInCtor;
    }
}

// UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD / URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD
class PublicFieldExamples {
    public String neverWrittenPublic;
    protected int neverWrittenProtected;
}

// UWF_NULL_FIELD: field only ever set to null
class NullFieldExample {
    Object alwaysNull = null;

    void maybeUse() {
        if (alwaysNull == null) {
            // do nothing
            ;
        }
    }
}

class NonStaticUtility {
    // SS_SHOULD_BE_STATIC
    void helper() {
        System.out.println("helper");
    }
}

// -------------------------------------------------------------------------
// Naming conventions
// -------------------------------------------------------------------------

class NmNamingExamples {
    // NM_METHOD_NAMING_CONVENTION
    void bad_method_name() {
    }
}

// NM_SAME_SIMPLE_NAME_AS_INTERFACE
class Callable implements java.util.concurrent.Callable<String> {
    @Override
    public String call() {
        return "value";
    }
}

// NM_SAME_SIMPLE_NAME_AS_SUPERCLASS
class Hashtable extends java.util.Hashtable<String, String> {
}

// NM_CLASS_NAMING_CONVENTION example (renamed to satisfy Checkstyle)
class NmBadClassName {
    void doSomething() {
    }
}

// -------------------------------------------------------------------------
// Nullness and redundant null checks
// -------------------------------------------------------------------------

class NullnessExamples {

    @NotNull
    private String nonNullField;

    private String alwaysNull;

    @SuppressWarnings("ConstantConditions")
    void nullness() throws IOException {
        // NP_ALWAYS_NULL / NP_UNWRITTEN_FIELD
        String local = null;
        if (local == null) {
            local = null;
        }

        // Actually read an unwritten field so NP_UNWRITTEN_FIELD is exercised
        String readUnwritten = alwaysNull;
        if (readUnwritten == null) {
            // still null
            ;
        }

        // NP_DEREFERENCE_OF_READLINE_VALUE / NP_IMMEDIATE_DEREFERENCE_OF_READLINE
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line = reader.readLine();
        if (line == null) {
            line.toString();
        }

        // NP_LOAD_OF_KNOWN_NULL_VALUE
        String knownNull = null;
        String copy = knownNull;
        if (copy != null && copy.isEmpty()) {
            System.out.println("impossible");
        }

        // RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE
        String nonNull = "x";
        if (nonNull != null) {
            nonNull.length();
        }

        // RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE
        String maybeNull = null;
        if (maybeNull != null && maybeNull.length() > 0) {
            System.out.println("never");
        }
    }

    @NotNull
    String returnsNonNullButReturnsNull() {
        // NP_NONNULL_RETURN_VIOLATION
        return null;
    }

    @Override
    public String toString() {
        // NP_TOSTRING_COULD_RETURN_NULL
        return null;
    }

    @SuppressWarnings("ConstantConditions")
    void nullOnSomePath(boolean flag) {
        // NP_NULL_ON_SOME_PATH_EXCEPTION
        String value = "x";
        if (flag) {
            value = null;
        }
        value.length();
    }

    void writeNonNullField() {
        nonNullField = "set";
    }

    @SuppressWarnings("ConstantConditions")
    boolean booleanReturnNull() {
        // NP_BOOLEAN_RETURN_NULL
        return (Boolean) null;
    }

    void guaranteedDeref() {
        // NP_GUARANTEED_DEREF
        String s = "x";
        s.length();
    }

    @SuppressWarnings("ConstantConditions")
    void parameterTightensAnnotation(@Nullable Object arg) {
        // NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE
        arg.toString();
    }

    @SuppressWarnings("ConstantConditions")
    void methodParameterTightensAnnotation(@Nullable Object input) {
        // NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION
        if (input == null || input.toString().isEmpty()) {
            System.out.println("bad");
        }
    }
}

// -------------------------------------------------------------------------
// Useless conditions, dead stores, unused methods, UR_*
// -------------------------------------------------------------------------

class UselessExamples {

    private int field;

    void deadStoreAndUselessCondition() {
        // DLS_DEAD_LOCAL_STORE
        int x = 1;
        x = 2;

        // UC_USELESS_CONDITION
        if (true && x > 0) {
            field = x;
        }

        // UC_USELESS_OBJECT
        new StringBuilder("unused");
    }

    // UC_USELESS_VOID_METHOD
    void uselessVoid() {
    }

    // UPM_UNCALLED_PRIVATE_METHOD
    private void neverCalled() {
        System.out.println("never");
    }

    void anonymousUncallable() {
        Runnable r = new Runnable() {
            // UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS
            private void hidden() {
                System.out.println("hidden");
            }

            @Override
            public void run() {
                // no-op
            }
        };
        r.run();
    }

    int uninitialisedLocalRead() {
        // UR_UNINIT_READ
        int x;
        int y = 0;
        // deliberately read x before assignment
        y = 1; // keep y live
        // (for SpotBugs the interesting bit is "int z = x;" below)
        int z;
        //noinspection UnusedAssignment
        z = 0;
        // Real trigger:
        int read = 0;
        //noinspection UnusedAssignment
        read = x = read; // use x before explicit write
        return read;
    }
}

// -------------------------------------------------------------------------
// Serialisation and switch issues
// -------------------------------------------------------------------------

class SerializationExamples {

    int switchProblems(int code) {
        int result = 0;
        // SF_SWITCH_FALLTHROUGH
        switch (code) {
            case 1:
                result = 1;
            case 2:
                result = 2;
                break;
            default:
                // SF_SWITCH_NO_DEFAULT is exercised elsewhere.
                break;
        }
        return result;
    }

    int switchWithoutDefault(int code) {
        int result = 0;
        // SF_SWITCH_NO_DEFAULT
        switch (code) {
            case 1:
                result = 1;
                break;
            case 2:
                result = 2;
                break;
            default:
                // keep default branch empty on purpose
                result = result;
        }
        return result;
    }

    String stringBufferConcat() {
        // SBSC_USE_STRINGBUFFER_CONCATENATION
        StringBuffer buffer = new StringBuffer();
        buffer.append("a" + "b");
        return buffer.toString();
    }

    enum BadEnum {
        A, B;

        // ME_ENUM_FIELD_SETTER
        void setValue(int v) {
        }
    }

    static class BadSerial implements Serializable {
        // SE_BAD_FIELD
        public transient Object transientField;
        private Object normalField;

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            // SE_TRANSIENT_FIELD_NOT_RESTORED
        }
    }

    // SE_COMPARATOR_SHOULD_BE_SERIALIZABLE: Comparator not Serializable
    static class NonSerializableComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    }
}

// -------------------------------------------------------------------------
// Return values, formatting, NS_/RC_ misc correctness, WMI
// -------------------------------------------------------------------------

class ReturnValueExamples {

    void ignoredReturnValues() {
        // RV_RETURN_VALUE_IGNORED_BAD_PRACTICE
        "text".substring(1);

        // RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT
        new StringBuilder().append("x");
    }
}

class MiscCorrectnessExamples {

    boolean dangerousNonShortCircuit(boolean a, boolean b) {
        // NS_DANGEROUS_NON_SHORT_CIRCUIT
        return a & b;
    }

    boolean refComparisonBoolean(Boolean b) {
        // RC_REF_COMPARISON_BAD_PRACTICE_BOOLEAN
        return b == Boolean.TRUE;
    }

    // WMI_WRONG_MAP_ITERATOR
    int wrongMapIterator(Map<String, Integer> map) {
        int sum = 0;
        for (Iterator<String> it = map.keySet().iterator(); it.hasNext(); ) {
            String key = it.next();
            sum += map.get(key);
        }
        return sum;
    }
}

// -------------------------------------------------------------------------
// HSM_HIDING_METHOD
// -------------------------------------------------------------------------

class HsmParent {
    static void doWork() {
        System.out.println("parent");
    }
}

class HsmChild extends HsmParent {
    // HSM_HIDING_METHOD: hides static method in parent
    static void doWork() {
        System.out.println("child");
    }
}

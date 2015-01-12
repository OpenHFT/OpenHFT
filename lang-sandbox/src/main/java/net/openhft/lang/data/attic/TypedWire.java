package net.openhft.lang.data.attic;

/**
 * Created by peter on 1/10/15.
 */
public class TypedWire {
    public enum TypeGroup {
        // group 00 should be invalid.
        SPECIAL(0),
        INTEGER(1),
        FIELD_NUMBER(2),
        FIXED_PRECISION(3),
        STRING(4),
        FIELD_NAME(5),
        CLASS_NAME(6),
        COMMENT(7);

        private final int code;

        TypeGroup(int code) {

            this.code = code;
        }
    }

    public enum SpecialCodes {
        INVALID(0),
        GROUP_START(2),
        GROUP_END(3);

        private final int code;

        SpecialCodes(int code) {

            this.code = code;
        }
        }
}

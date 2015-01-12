package net.openhft.lang.data;

/**
 * Created by peter on 12/01/15.
 */
public enum YamlCodes {
    byte_order_mark('\uFEFF', "\u00FE\u00FF"),
    directive('%'),
    directive_end('=', "---"),
    document_end('.', "..."),
    sequence_entry('-'),
    mapping_key('?'),
    mapping_value(':'),
    collect_entry(','),
    sequence_start('['),
    sequence_end(']'),
    mapping_start('{'),
    mapping_end('}'),
    comment('#'),
    anchor('&'),
    alias('*'),
    tag('!'),
    literal('|'),
    folded('>'),
    single_quote('\''),
    double_quote('\"');

    private final char code;
    private final String str;

    static {
    }

    YamlCodes(char code) {
        this(code, Character.toString(code));
    }

    YamlCodes(char code, String str) {
        this.code = code;
        this.str = str;
    }
}

package net.openhft.lang.data;

/**
 * Created by peter on 12/01/15.
 *
 * @see <a href="YAML Types">http://yaml.org/type/</a>
 */
public enum YamlTypes {
    MAP,
    OMAP,
    PAIRS,
    SET,
    SEQ,

    BOOL,
    FLOAT,
    INT,
    MERGE,
    NULL,
    STR,
    TIMESTAMP, /* date, date-time, date-time-zone */
    VALUE,
    YAML;

}

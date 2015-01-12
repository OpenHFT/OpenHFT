package net.openhft.lang.data;

/**
 * Created by peter on 12/01/15.
 */
public enum BinaryTypes {
    // special handling
    INVALID,
    DIRECTIVE,
    DIRECTIVE_END,
    DOCUMENT_END,
    SEQUENCE_START,
    SEQUENCE_END,
    MAPPING_START,
    MAPPING_KEY,
    MAPPING_END,
    ANCHOR,
    ALIAS,
    TYPE,
    HINT,

    // scalar
    COMMENT,
    COUNT,
    LEN8,
    LEN16,
    LEN32,
    INT8,
    INT16,
    INT24,
    INT32,
    INT64,
    UINT8,
    UINT16,
    UINT32,
    UINT64,
    FLOAT32,
    FLOAT64,
    BOOLEAN_TRUE,
    BOOLEAN_FALSE,
    EMPTY_STRING,
    /**
     * {size} {UTF8 bytes}
     */
    STRING,
    /**
     * {max-size} {size} {UTF8 bytes}
     */
    FIXED_STRING,
    /**
     * {}
     */
    NULL,
    /**
     * {INT24 for the date}.
     */
    DATE,
    /**
     * {INT64 for the date time to 0.1 micro-seconds}.
     */
    DATETIME,
    /**
     * {INT64} {INT8 timezone}
     */
    DATETIMEZONE,
    TIME,
    UUID,
}

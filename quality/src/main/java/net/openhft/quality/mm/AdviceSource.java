/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

/**
 * User-visible advice source categories.
 */
public enum AdviceSource {
    /**
     * Code construct sources.
     */
    ASSERTION,
    PRECONDITION,
    THROW,
    LOG,
    COMMENT,
    JAVADOC_CLASS,
    JAVADOC_MEMBER,
    /**
     * Annotation subtypes where advice differs.
     */
    ANNOTATION_DISPLAY_NAME,
    ANNOTATION_DISABLED,
    ANNOTATION_TEST_ORDER,
    ANNOTATION_JUNIT4
    ;

    /**
     * Return the AdviceId prefix component for this source.
     *
     * @return AdviceId source prefix.
     */
    public String idPrefix() {
        switch (this) {
            case ASSERTION:
                return "Assertion";
            case PRECONDITION:
                return "Precondition";
            case THROW:
                return "Throw";
            case LOG:
                return "Log";
            case COMMENT:
                return "Comment";
            case JAVADOC_CLASS:
                return "JavadocClass";
            case JAVADOC_MEMBER:
                return "JavadocMember";
            case ANNOTATION_DISPLAY_NAME:
                return "AnnotationDisplayName";
            case ANNOTATION_DISABLED:
                return "AnnotationDisabled";
            case ANNOTATION_TEST_ORDER:
                return "AnnotationTestOrder";
            case ANNOTATION_JUNIT4:
                return "AnnotationJUnit4";
            default:
                throw new IllegalStateException("Unhandled advice source: " + this);
        }
    }

    /**
     * Map a message source to the corresponding advice source.
     * Annotation sources must be supplied explicitly.
     *
     * @param source message source to map.
     * @return matching advice source, or {@code null} for annotation sources.
     */
    public static AdviceSource fromMessageSource(MessageSource source) {
        if (source == null) {
            return null;
        }
        switch (source) {
            case ASSERTION:
                return ASSERTION;
            case PRECONDITION:
                return PRECONDITION;
            case THROW:
                return THROW;
            case LOG:
                return LOG;
            case COMMENT:
                return COMMENT;
            case JAVADOC_CLASS:
                return JAVADOC_CLASS;
            case JAVADOC_MEMBER:
                return JAVADOC_MEMBER;
            case ANNOTATION:
                return null;
            default:
                throw new IllegalStateException("Unhandled MessageSource: " + source);
        }
    }
}

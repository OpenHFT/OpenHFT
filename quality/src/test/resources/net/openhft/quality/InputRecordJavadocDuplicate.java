/*
 * Test input for record Javadoc duplication on components.
 */
package net.openhft.quality;

/**
 * Captures configuration fields, limits, and identifiers for workflow validation snapshots.
 */
public record InputRecordJavadocDuplicate(String name, int count) {
    /**
     * Computes a checksum summary so audit pipelines can verify record payload integrity.
     *
     * @return deterministic checksum for the current record contents
     */
    public int checksum() {
        return (name == null ? 0 : name.hashCode()) ^ count;
    }
}

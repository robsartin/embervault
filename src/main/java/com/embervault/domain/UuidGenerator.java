package com.embervault.domain;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates UUID v7 (time-ordered) identifiers per RFC 9562.
 *
 * <p>UUID v7 layout (128 bits):
 * <pre>
 *  0                   1                   2                   3
 *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                          unix_ts_ms (48 bits)                 |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |  ver (4) |      rand_a (12 bits)                              |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |var(2)|              rand_b (62 bits)                           |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * </pre>
 * The 48-bit timestamp ensures chronological ordering. Random
 * bits in rand_a and rand_b ensure uniqueness.</p>
 */
public final class UuidGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final AtomicLong LAST_TIMESTAMP =
            new AtomicLong(0);

    private UuidGenerator() { }

    /**
     * Generates a new UUID v7.
     *
     * @return a time-ordered UUID
     */
    public static UUID generate() {
        long now = System.currentTimeMillis();

        // Ensure monotonic ordering
        long ts = LAST_TIMESTAMP.updateAndGet(
                prev -> Math.max(prev + 1, now));

        // High 64 bits: 48-bit timestamp + 4-bit version
        // + 12-bit random
        long randA = RANDOM.nextLong() & 0x0FFFL;
        long msb = (ts << 16)
                | (0x7000L)       // version 7
                | randA;

        // Low 64 bits: 2-bit variant + 62-bit random
        long randB = RANDOM.nextLong();
        long lsb = (randB & 0x3FFFFFFFFFFFFFFFL)
                | 0x8000000000000000L; // variant 10

        return new UUID(msb, lsb);
    }
}

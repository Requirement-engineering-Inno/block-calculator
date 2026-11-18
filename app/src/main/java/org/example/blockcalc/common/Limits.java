package org.example.blockcalc.common;

/** Upper bounds for user input, so nobody enters 100000000000. */
public final class Limits {
    public static final long MAX_PRICE = 1_000_000;
    public static final long MAX_BLOCKS = 10_000_000;
    public static final long MAX_WS = 10_000;
    public static final long MAX_DENSITY = 10_000;
    public static final long MAX_RATE = 1_000_000;

    private Limits() {
    }
}

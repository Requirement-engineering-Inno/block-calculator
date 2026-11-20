package org.example.blockcalc.common;

import java.math.BigDecimal;

/** Physical constants used to convert prices into "per kg". They are fixed on purpose and shown in the UI. */
public final class Materials {
    /** Bulk density of sand, kg/m³. */
    public static final BigDecimal SAND_DENSITY = new BigDecimal("1600");
    /** Bulk density of crushed stone, kg/m³. */
    public static final BigDecimal STONE_DENSITY = new BigDecimal("1450");
    /** Weight of one bag of cement, kg. */
    public static final BigDecimal BAG_KG = new BigDecimal("50");
    public static final BigDecimal KG_PER_TONNE = new BigDecimal("1000");

    private Materials() {
    }
}

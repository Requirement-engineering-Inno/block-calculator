package org.example.blockcalc.project;

import java.util.List;

public enum PriceUnit {
    PER_KG, PER_M3, PER_TONNE, PER_BAG;

    /** Units a manager can choose for sand and crushed stone. */
    public static final List<PriceUnit> BULK = List.of(PER_KG, PER_M3, PER_TONNE);
    /** Units a manager can choose for cement. */
    public static final List<PriceUnit> CEMENT = List.of(PER_KG, PER_BAG, PER_TONNE);
}

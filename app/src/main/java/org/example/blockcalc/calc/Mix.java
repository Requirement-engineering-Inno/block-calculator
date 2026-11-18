package org.example.blockcalc.calc;

import org.example.blockcalc.common.BusinessException;

import java.math.BigDecimal;

/** The four fixed mixes from the customer's PDF. One measure = one wheelbarrow, cement = one 50 kg bag. */
public enum Mix {
    BATCH_1(1, "3", "0", 22, "4.8"),
    BATCH_2(2, "3", "2", 35, "5.2"),
    BATCH_3(3, "2.5", "2.5", 35, "6.2"),
    BATCH_4(4, "2", "2", 28, "7.6");

    public final int number;
    public final BigDecimal sandBarrows;
    public final BigDecimal stoneBarrows;
    /** Blocks produced by one batch. */
    public final int yield;
    public final BigDecimal mpa;

    Mix(int number, String sand, String stone, int yield, String mpa) {
        this.number = number;
        this.sandBarrows = new BigDecimal(sand);
        this.stoneBarrows = new BigDecimal(stone);
        this.yield = yield;
        this.mpa = new BigDecimal(mpa);
    }

    public static Mix of(int number) {
        for (Mix m : values()) {
            if (m.number == number) {
                return m;
            }
        }
        throw new BusinessException("error.calc.batch");
    }
}

package org.example.blockcalc.calc;

import java.math.BigDecimal;

/** Prices stored on a frozen calculation so later price changes do not affect it. */
public class PriceSnapshot {
    public String currency;
    public BigDecimal sandPerKg;
    public BigDecimal stonePerKg;
    public BigDecimal cementPerKg;

    public PriceSnapshot() {
    }

    public PriceSnapshot(PerKgPrices p) {
        this.currency = p.currency();
        this.sandPerKg = p.sand();
        this.stonePerKg = p.stone();
        this.cementPerKg = p.cement();
    }

    public PerKgPrices toPrices() {
        return new PerKgPrices(currency, sandPerKg, stonePerKg, cementPerKg);
    }
}

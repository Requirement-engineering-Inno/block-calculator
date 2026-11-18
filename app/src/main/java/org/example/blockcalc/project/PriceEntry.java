package org.example.blockcalc.project;

import java.math.BigDecimal;

public class PriceEntry {
    public BigDecimal amount = BigDecimal.ZERO;
    public PriceUnit unit = PriceUnit.PER_KG;

    public PriceEntry() {
    }

    public PriceEntry(BigDecimal amount, PriceUnit unit) {
        this.amount = amount;
        this.unit = unit;
    }
}

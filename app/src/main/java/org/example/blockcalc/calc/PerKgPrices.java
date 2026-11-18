package org.example.blockcalc.calc;

import java.math.BigDecimal;

/** Material prices normalised to "per kg" in one currency. */
public record PerKgPrices(String currency, BigDecimal sand, BigDecimal stone, BigDecimal cement) {
}

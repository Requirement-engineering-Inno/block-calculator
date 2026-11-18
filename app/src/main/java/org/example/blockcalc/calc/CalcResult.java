package org.example.blockcalc.calc;

import java.math.BigDecimal;

public record CalcResult(
        int batch,
        BigDecimal ws,
        BigDecimal blocks,
        BigDecimal batches,
        BigDecimal sandKg,
        BigDecimal stoneKg,
        BigDecimal cementKg,
        BigDecimal sandBarrows,
        BigDecimal stoneBarrows,
        BigDecimal cementBags,
        BigDecimal sandCost,
        BigDecimal stoneCost,
        BigDecimal cementCost,
        BigDecimal total,
        BigDecimal costPerBlock,
        String currency) {
}

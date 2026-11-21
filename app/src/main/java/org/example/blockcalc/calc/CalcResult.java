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

    /**
     * The same quantities with every money value removed. Workers get this copy: they see proportions and
     * amounts of materials, never prices, so the figures are not even sent to their templates.
     */
    public CalcResult withoutMoney() {
        return new CalcResult(batch, ws, blocks, batches, sandKg, stoneKg, cementKg, sandBarrows, stoneBarrows, cementBags,
                null, null, null, null, null, null);
    }
}

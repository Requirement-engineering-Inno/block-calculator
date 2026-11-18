package org.example.blockcalc.calc;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Pure calculation logic, no I/O. */
public final class CalculationEngine {

    public static final BigDecimal BAG_KG = new BigDecimal("50");
    private static final BigDecimal HALF = new BigDecimal("0.5");

    private CalculationEngine() {
    }

    /**
     * @param ws      weight of one wheelbarrow, kg (used for sand and stone)
     * @param roundUp round the number of batches up to a whole number
     */
    public static CalcResult calculate(int full, int half, Mix mix, BigDecimal ws, boolean roundUp, PerKgPrices prices) {
        BigDecimal blocks = BigDecimal.valueOf(full).add(BigDecimal.valueOf(half).multiply(HALF));
        BigDecimal yield = BigDecimal.valueOf(mix.yield);
        BigDecimal batches = roundUp
                ? blocks.divide(yield, 0, RoundingMode.CEILING)
                : blocks.divide(yield, 10, RoundingMode.HALF_UP);

        BigDecimal sandKg = batches.multiply(mix.sandBarrows).multiply(ws);
        BigDecimal stoneKg = batches.multiply(mix.stoneBarrows).multiply(ws);
        BigDecimal cementKg = batches.multiply(BAG_KG);

        BigDecimal sandCost = money(sandKg.multiply(prices.sand()));
        BigDecimal stoneCost = money(stoneKg.multiply(prices.stone()));
        BigDecimal cementCost = money(cementKg.multiply(prices.cement()));
        BigDecimal total = sandCost.add(stoneCost).add(cementCost);

        BigDecimal costPerBlock = blocks.signum() == 0
                ? BigDecimal.ZERO.setScale(2)
                : total.divide(blocks, 2, RoundingMode.HALF_UP);

        return new CalcResult(mix.number, ws, blocks, batches.setScale(4, RoundingMode.HALF_UP),
                round2(sandKg), round2(stoneKg), round2(cementKg),
                round2(batches.multiply(mix.sandBarrows)), round2(batches.multiply(mix.stoneBarrows)),
                round2(batches),
                sandCost, stoneCost, cementCost, total, costPerBlock, prices.currency());
    }

    private static BigDecimal round2(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal money(BigDecimal v) {
        return round2(v);
    }
}

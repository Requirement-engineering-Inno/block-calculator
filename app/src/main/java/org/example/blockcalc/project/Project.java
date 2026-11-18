package org.example.blockcalc.project;

import org.example.blockcalc.calc.CalculationEngine;
import org.example.blockcalc.calc.PerKgPrices;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Document("projects")
public class Project {

    public static final String OTHER_NAME = "Other calculations";

    @Id
    public String id;
    public String name;
    /** The built-in project every manager can use (and every worker too). */
    public boolean system;
    public String currency = "RUB";
    /** Default weight of one wheelbarrow, kg. */
    public BigDecimal wsDefault = new BigDecimal("150");
    public BigDecimal sandDensity = new BigDecimal("1600");
    public BigDecimal stoneDensity = new BigDecimal("1450");
    public PriceEntry sandPrice = new PriceEntry(BigDecimal.ZERO, PriceUnit.PER_M3);
    public PriceEntry stonePrice = new PriceEntry(BigDecimal.ZERO, PriceUnit.PER_M3);
    public PriceEntry cementPrice = new PriceEntry(BigDecimal.ZERO, PriceUnit.PER_BAG);
    public Set<String> workerIds = new HashSet<>();
    public String createdBy;
    public Instant createdAt;

    /** Prices converted to "per kg" using the project's densities and the 50 kg bag. */
    public PerKgPrices perKg() {
        return new PerKgPrices(currency,
                toKg(sandPrice, sandDensity),
                toKg(stonePrice, stoneDensity),
                toKg(cementPrice, null));
    }

    private static BigDecimal toKg(PriceEntry p, BigDecimal density) {
        return switch (p.unit) {
            case PER_KG -> p.amount;
            case PER_M3 -> p.amount.divide(density, 10, RoundingMode.HALF_UP);
            case PER_BAG -> p.amount.divide(CalculationEngine.BAG_KG, 10, RoundingMode.HALF_UP);
        };
    }
}

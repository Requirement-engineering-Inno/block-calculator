package org.example.blockcalc.project;

import org.example.blockcalc.calc.PerKgPrices;
import org.example.blockcalc.common.Materials;
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
    public PriceEntry sandPrice = new PriceEntry(BigDecimal.ZERO, PriceUnit.PER_M3);
    public PriceEntry stonePrice = new PriceEntry(BigDecimal.ZERO, PriceUnit.PER_M3);
    public PriceEntry cementPrice = new PriceEntry(BigDecimal.ZERO, PriceUnit.PER_BAG);
    public Set<String> workerIds = new HashSet<>();
    public String createdBy;
    public Instant createdAt;

    /** Prices converted to "per kg" with the fixed densities, the 50 kg bag and 1000 kg per tonne. */
    public PerKgPrices perKg() {
        return new PerKgPrices(currency,
                toKg(sandPrice, Materials.SAND_DENSITY),
                toKg(stonePrice, Materials.STONE_DENSITY),
                toKg(cementPrice, null));
    }

    private static BigDecimal toKg(PriceEntry p, BigDecimal density) {
        return switch (p.unit) {
            case PER_KG -> p.amount;
            case PER_M3 -> p.amount.divide(density, 10, RoundingMode.HALF_UP);
            case PER_TONNE -> p.amount.divide(Materials.KG_PER_TONNE, 10, RoundingMode.HALF_UP);
            case PER_BAG -> p.amount.divide(Materials.BAG_KG, 10, RoundingMode.HALF_UP);
        };
    }
}

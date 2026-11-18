package org.example.blockcalc.project;

import java.math.BigDecimal;
import java.util.Set;

/** Everything a manager can edit in project settings. */
public record ProjectInput(
        String name,
        String currency,
        BigDecimal ws,
        BigDecimal sandDensity,
        BigDecimal stoneDensity,
        BigDecimal sandAmount,
        PriceUnit sandUnit,
        BigDecimal stoneAmount,
        PriceUnit stoneUnit,
        BigDecimal cementAmount,
        PriceUnit cementUnit,
        Set<String> workerIds) {
}

package org.example.blockcalc.project;

import java.math.BigDecimal;
import java.util.Set;

/** Everything a manager can edit in project settings (WS, densities and currencies are global). */
public record ProjectInput(
        String name,
        String currency,
        BigDecimal sandAmount,
        PriceUnit sandUnit,
        BigDecimal stoneAmount,
        PriceUnit stoneUnit,
        BigDecimal cementAmount,
        PriceUnit cementUnit,
        Set<String> workerIds) {
}

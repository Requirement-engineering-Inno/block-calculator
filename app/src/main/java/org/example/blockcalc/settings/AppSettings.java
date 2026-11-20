package org.example.blockcalc.settings;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Application level settings. There is exactly one document. */
@Document("settings")
public class AppSettings {
    public static final String ID = "global";

    @Id
    public String id = ID;
    /** Currencies projects can choose from, three-letter codes. */
    public List<String> currencies = new ArrayList<>();
    /** Weight of one wheelbarrow of sand or stone, kg. Used when a calculation has no value of its own. */
    public BigDecimal wheelbarrowKg = new BigDecimal("150");
}

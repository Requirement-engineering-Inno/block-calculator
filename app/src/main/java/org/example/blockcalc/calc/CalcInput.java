package org.example.blockcalc.calc;

import java.math.BigDecimal;

/** Form data of a calculation. Wrapper types because unchecked checkboxes and blank fields arrive as null. */
public record CalcInput(
        String projectId,
        String title,
        Integer fullBlocks,
        Integer halfBlocks,
        Integer batch,
        BigDecimal ws,
        Boolean roundUp) {
}

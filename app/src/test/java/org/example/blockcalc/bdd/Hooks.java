package org.example.blockcalc.bdd;

import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;

public class Hooks {

    @Autowired
    private TestData data;

    /** Every scenario starts from an empty database. */
    @Before
    public void resetDatabase() {
        data.reset();
    }
}

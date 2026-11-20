package org.example.blockcalc.bdd;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.blockcalc.settings.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Application level settings: wheelbarrow weight and the list of currencies. */
public class SettingsSteps {

    @Autowired
    private World world;
    @Autowired
    private SettingsService settings;

    @When("I set the wheelbarrow weight to {string}")
    public void setWheelbarrow(String kg) throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("kg", kg);
        world.post("/settings/wheelbarrow", params);
    }

    @When("I add the currency {string}")
    public void addCurrency(String code) throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        world.post("/settings/currencies", params);
    }

    @When("I remove the currency {string}")
    public void removeCurrency(String code) throws Exception {
        world.post("/settings/currencies/" + code + "/delete");
    }

    @Then("the wheelbarrow weight is {string}")
    public void wheelbarrowIs(String kg) {
        assertEquals(0, new BigDecimal(kg).compareTo(settings.wheelbarrowKg()), "was " + settings.wheelbarrowKg());
    }

    @Then("the currency list contains {string}")
    public void currencyListContains(String code) {
        assertTrue(settings.currencies().contains(code), "Currencies: " + settings.currencies());
    }

    @Then("the currency list does not contain {string}")
    public void currencyListDoesNotContain(String code) {
        assertFalse(settings.currencies().contains(code), "Currencies: " + settings.currencies());
    }

    @Then("the settings page lists the currency {string}")
    public void pageListsCurrency(String code) throws Exception {
        assertFalse(world.document().select("#currency-list li[data-currency=\"" + code + "\"]").isEmpty(), "Not listed: " + code);
    }
}

package org.example.blockcalc.bdd;

import io.cucumber.spring.ScenarioScope;
import jakarta.servlet.http.Cookie;
import org.example.blockcalc.common.CurrentUser;
import org.example.blockcalc.user.UserService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/** State of one scenario: who is acting, the last response and the browser's cookies. */
@Component
@ScenarioScope
public class World {

    public static final String PASSWORD = "secret1";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserService users;

    private CurrentUser user;
    private MvcResult last;
    private Document document;
    private final Map<String, String> cookies = new HashMap<>();

    // ---- who is acting -------------------------------------------------------------------

    public void loginAs(String username) {
        user = (CurrentUser) users.loadUserByUsername(username);
    }

    public void anonymous() {
        user = null;
    }

    // ---- requests ------------------------------------------------------------------------

    public void get(String path) throws Exception {
        last = perform(MockMvcRequestBuilders.get(path), false);
        document = null;
    }

    public void post(String path) throws Exception {
        post(path, new LinkedMultiValueMap<>());
    }

    public void post(String path, MultiValueMap<String, String> params) throws Exception {
        last = perform(MockMvcRequestBuilders.post(path).params(params), true);
        document = null;
    }

    /** The real form login (no pre-authenticated user). */
    public void formLogin(String username, String password) throws Exception {
        MockHttpServletRequestBuilder b = MockMvcRequestBuilders.post("/login")
                .param("username", username)
                .param("password", password);
        last = mvc.perform(b.with(SecurityMockMvcRequestPostProcessors.csrf())).andReturn();
        document = null;
    }

    private MvcResult perform(MockHttpServletRequestBuilder builder, boolean csrf) throws Exception {
        if (user != null) {
            builder.with(SecurityMockMvcRequestPostProcessors.user(user));
        }
        if (csrf) {
            builder.with(SecurityMockMvcRequestPostProcessors.csrf());
        }
        cookies.forEach((name, value) -> builder.cookie(new Cookie(name, value)));
        MvcResult result = mvc.perform(builder).andReturn();
        Cookie[] set = result.getResponse().getCookies();
        for (Cookie c : set) {
            cookies.put(c.getName(), c.getValue());
        }
        return result;
    }

    // ---- last response -------------------------------------------------------------------

    public int status() {
        return last.getResponse().getStatus();
    }

    /** The Location header of a redirect, as path and query, or null. */
    public String redirect() {
        String location = last.getResponse().getRedirectedUrl();
        return location == null ? null : pathOf(location);
    }

    public String contentType() {
        return last.getResponse().getContentType();
    }

    public String header(String name) {
        return last.getResponse().getHeader(name);
    }

    public byte[] bytes() {
        return last.getResponse().getContentAsByteArray();
    }

    /**
     * The HTML of the last response. When the last response was a redirect, the target page is fetched
     * (as the same user) so that "Then" steps can look at the page the user lands on.
     */
    public Document document() throws Exception {
        if (document == null) {
            MvcResult source = last;
            String location = redirect();
            if (location != null) {
                source = perform(MockMvcRequestBuilders.get(location), false);
            }
            document = Jsoup.parse(source.getResponse().getContentAsString());
        }
        return document;
    }

    private static String pathOf(String location) {
        URI uri = URI.create(location);
        String path = uri.getRawPath() == null ? "" : uri.getRawPath();
        return uri.getRawQuery() == null ? path : path + "?" + uri.getRawQuery();
    }
}

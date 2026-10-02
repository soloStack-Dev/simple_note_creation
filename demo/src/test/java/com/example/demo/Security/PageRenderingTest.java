package com.example.demo.Security;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * Renders the real pages over HTTP and asserts the UI contract holds.
 *
 * <p>MockMvc would be quicker, but this deliberately goes over a real socket: the thing that
 * keeps breaking in this app is markup and headers rather than Java, and rendering the pages is
 * the only way to know a fragment still resolves its model. It also pins the two behaviours that
 * are easy to regress while changing the templates:
 *
 * <ul>
 *   <li>{@code GET /notes} sends anonymous callers to {@code /login?returnUrl=%2Fnotes}, and
 *       renders once a token cookie is present.</li>
 *   <li>The dual-representation routes advertise {@code Vary: HX-Request}.</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PageRenderingTest {

    @LocalServerPort
    private int port;

    private final CookieManager jar = new CookieManager(null, CookiePolicy.ACCEPT_ALL);

    private final HttpClient http = HttpClient.newBuilder()
            .cookieHandler(jar)
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    // Static: JUnit gives every test method a fresh instance, but the in-memory database is
    // shared for the whole run. An instance counter would hand the second method a username the
    // first already registered.
    private static final AtomicInteger accounts = new AtomicInteger();

    private HttpRequest.Builder request(String path) {
        return HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
    }

    private HttpResponse<String> get(String path) {
        return send(request(path).GET());
    }

    private HttpResponse<String> postForm(String path, String form) {
        return send(request(path)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form)));
    }

    private HttpResponse<String> send(HttpRequest.Builder builder) {
        try {
            return http.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("HTTP call failed", e);
        }
    }

    private String match(String text, String regex) {
        Matcher m = Pattern.compile(regex).matcher(text);
        return m.find() ? m.group(1) : null;
    }

    /** The token exactly as a browser reads it out of the page markup. */
    private String tokenOf(HttpResponse<String> page) {
        String token = match(page.body(), "name=\"csrf-token\" content=\"([^\"]+)\"");
        assertThat(token).as("the page renders a CSRF token").isNotBlank();
        return URLEncoder.encode(token, StandardCharsets.UTF_8);
    }

    private void signUpAndIn() {
        String user = "pagetest" + accounts.incrementAndGet();
        HttpResponse<String> signup = get("/signup");
        postForm("/signup", "_csrf=" + tokenOf(signup)
                + "&username=" + user
                + "&email=" + user + "@test.local"
                + "&password=Passw0rd!"
                + "&bio=");

        HttpResponse<String> login = get("/login");
        postForm("/login", "_csrf=" + tokenOf(login)
                + "&username=" + user
                + "&password=Passw0rd!");

        assertThat(jar.getCookieStore().getCookies())
                .as("sign-in issued the auth token cookie")
                .anySatisfy(cookie -> assertThat(cookie.getName()).isEqualTo("note_token"));
    }

    @Test
    void theLandingPageRendersTheHeroAndTheShell() {
        HttpResponse<String> page = get("/");

        assertThat(page.statusCode()).isEqualTo(200);
        assertThat(page.body())
                .as("the design-system shell class")
                .contains("class=\"mn-shell\"")
                .as("the hero, not the old hero-panel")
                .contains("mn-hero")
                .doesNotContain("hero-panel")
                .as("the theme is applied before first paint")
                .contains("data-bs-theme")
                .as("the theme toggle is present for anonymous visitors too")
                .contains("data-mn-theme-toggle");
    }

    @Test
    void anonymousCallersAreSentToLoginAndCannotReachNotes() {
        HttpResponse<String> notes = get("/notes");

        assertThat(notes.statusCode()).isEqualTo(302);
        assertThat(notes.headers().firstValue("Location"))
                .as("absolute, so the assertion checks the path rather than the host")
                .hasValueSatisfying(location -> assertThat(location)
                        .endsWith("/login?returnUrl=%2Fnotes"));

        // /about is behind the token too, which is easy to forget when adding a page.
        assertThat(get("/about").statusCode()).isEqualTo(302);
    }

    @Test
    void theAuthPagesRenderWithoutAToken() {
        assertThat(get("/login").statusCode()).isEqualTo(200);
        assertThat(get("/signup").statusCode()).isEqualTo(200);
        assertThat(get("/login").body()).contains("autocomplete=\"username\"");
    }

    @Test
    void theNotesPageRendersTheStripAndTheModalForASignedInUser() {
        signUpAndIn();

        HttpResponse<String> page = get("/notes");

        assertThat(page.statusCode()).isEqualTo(200);
        assertThat(page.body())
                .as("the grid the create response swaps out-of-band")
                .contains("id=\"notesGrid\"")
                .as("the per-category stat strip")
                .contains("mn-tile__value")
                .contains("Daily routine")
                .as("the add-note modal, server-rendered so it works without htmx")
                .contains("id=\"noteModal\"")
                .contains("id=\"noteModalBody\"")
                .contains("/fragments/note-form");
    }

    @Test
    void notesPageVariesOnTheHtmxHeader() {
        signUpAndIn();

        assertThat(get("/notes").headers().firstValue("Vary")).contains("HX-Request");
    }

    @Test
    void aboutPageRendersForASignedInUser() {
        signUpAndIn();

        HttpResponse<String> page = get("/about");

        assertThat(page.statusCode()).isEqualTo(200);
        assertThat(page.body()).contains("Why Memory").contains("mn-display");
    }

    @Test
    void aCreateRendersTheCardWithItsCategoryAndUpdatesTheStrip() {
        signUpAndIn();
        HttpResponse<String> notesPage = get("/notes");
        String page = notesPage.body();
        String token = match(page, "name=\"csrf-token\" content=\"([^\"]+)\"");
        // Not "X-CSRF-TOKEN": a cookie repository names its header X-XSRF-TOKEN. The page
        // publishes the right one, and app.js reads it from there rather than guessing -
        // which is exactly what this test is checking.
        String headerName = match(page, "name=\"csrf-header\" content=\"([^\"]+)\"");

        HttpResponse<String> created = send(request("/notes")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("HX-Request", "true")
                .header(headerName, token)
                .POST(HttpRequest.BodyPublishers.ofString("category=MEMORIES&message=A+long+walk")));

        assertThat(created.statusCode()).isEqualTo(200);
        assertThat(created.body())
                .as("the new note, carrying the category so the CSS can colour it")
                .contains("A long walk")
                .contains("data-category=\"MEMORIES\"")
                .as("the strip counted it")
                // Not an exact number: the strip totals every note in the database, and the
                // suite shares one in-memory instance across test classes.
                .containsPattern("mn-tile--memory[\\s\\S]{0,400}?mn-tile__value ms-auto\">[1-9]")
                .as("and the marker app.js closes the modal on")
                .contains("data-note-created");
    }
}
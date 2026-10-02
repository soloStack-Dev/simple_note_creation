package com.example.demo.Security;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * The CSRF token has to survive a page render and then stay usable for every mutation that
 * follows. This app runs {@code SessionCreationPolicy.STATELESS}, so the default
 * HttpSession-backed repository is the wrong home for the token - see
 * {@link SecurityConfig#csrfTokenRepository()}.
 *
 * <p>Each test drives a real browser-shaped session: one cookie jar, the token taken from the
 * rendered page, and cookies carried forward automatically.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CsrfTokenRepositoryTest {

    @LocalServerPort
    private int port;

    private final CookieManager jar = new CookieManager(null, CookiePolicy.ACCEPT_ALL);

    private final HttpClient http = HttpClient.newBuilder()
            .cookieHandler(jar)
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    private String uri(String path) {
        return "http://localhost:" + port + path;
    }

    private HttpRequest.Builder request(String path) {
        return HttpRequest.newBuilder(URI.create(uri(path)));
    }

    private HttpResponse<String> get(String path) throws Exception {
        return http.send(request(path).GET().build(), body());
    }

    private HttpResponse<String> post(String path, String form) throws Exception {
        return http.send(request(path)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build(), body());
    }

    /** The htmx-shaped create: token in a header, fragment body back instead of a redirect. */
    private HttpResponse<String> htmxPost(String path, String form, String token) throws Exception {
        return http.send(request(path)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("HX-Request", "true")
                .header(csrfHeaderName(), token)
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build(), body());
    }

    /** The htmx-shaped delete: token in a header, out-of-band grid back. */
    private HttpResponse<String> htmxDelete(String path, String token) throws Exception {
        return http.send(request(path)
                .header("HX-Request", "true")
                .header(csrfHeaderName(), token)
                .DELETE().build(), body());
    }

    private String csrfHeaderName() throws Exception {
        HttpResponse<String> page = get("/");
        String name = matcher(page.body(), "name=\"csrf-header\" content=\"([^\"]+)\"");
        return name != null ? name : "X-CSRF-TOKEN";
    }

    private HttpResponse.BodyHandler<String> body() {
        return HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
    }

    private int status(HttpResponse<String> response) {
        return response.statusCode();
    }

    /**
     * Signs in over HTTP so the cookie jar carries a real auth token. Every request that is
     * meant to reach a controller needs this - an anonymous caller is bounced to /login by the
     * entry point before CSRF is ever considered, so an anonymous test would only ever be
     * measuring the redirect.
     */
    private void signIn() throws Exception {
        String user = "uitester" + counter.incrementAndGet();
        HttpResponse<String> signup = get("/signup");
        post("/signup", "_csrf=" + urlEncode(pageToken(signup))
                + "&username=" + user + "&email=" + user + "@test.local&password=Passw0rd!&bio=");

        HttpResponse<String> login = get("/login");
        post("/login", "_csrf=" + urlEncode(pageToken(login))
                + "&username=" + user + "&password=Passw0rd!");

        assertThat(jar.getCookieStore().getCookies())
                .as("sign-in issued the auth token cookie")
                .anySatisfy(cookie -> assertThat(cookie.getName()).isEqualTo("note_token"));
    }

    private final java.util.concurrent.atomic.AtomicInteger counter =
            new java.util.concurrent.atomic.AtomicInteger();

    private String urlEncode(String value) {
        return value == null ? "" : java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    

    /** The token exactly as a browser would read it out of the page markup. */
    private String pageToken(HttpResponse<String> page) {
        String meta = matcher(page.body(), "name=\"csrf-token\" content=\"([^\"]+)\"");
        return meta != null ? meta : matcher(page.body(), "name=\"_csrf\" value=\"([^\"]+)\"");
    }

    private String matcher(String text, String regex) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(regex).matcher(text);
        return m.find() ? m.group(1) : null;
    }

    @Test
    void theTokenIsDeliveredInACookieTheBrowserKeeps() throws Exception {
        HttpResponse<String> page = get("/");

        assertThat(status(page)).isEqualTo(200);
        assertThat(pageToken(page)).isNotBlank();
        assertThat(jar.getCookieStore().getCookies())
                .anySatisfy(cookie -> assertThat(cookie.getName()).isEqualTo("XSRF-TOKEN"));
    }

    @Test
    void oneTokenStaysValidAcrossManyCreates() throws Exception {
        signIn();
        String token = pageToken(get("/notes"));

        // This is the regression that mattered: the token used to work exactly once.
        for (int attempt = 1; attempt <= 5; attempt++) {
            HttpResponse<String> response = post("/notes",
                    "_csrf=" + token + "&category=MEMORIES&message=create+attempt+" + attempt);
            assertThat(status(response))
                    .as("POST attempt %d reusing the token from the page", attempt)
                    .isIn(200, 302);
            assertThat(jar.getCookieStore().getCookies())
                    .as("attempt %d must not expire the token", attempt)
                    .anySatisfy(cookie -> assertThat(cookie.getName()).isEqualTo("XSRF-TOKEN"));
        }
    }

    @Test
    void theSameTokenWorksForADeleteAfterCreates() throws Exception {
        signIn();
        String token = pageToken(get("/notes"));
        HttpResponse<String> created = htmxPost("/notes",
                "category=MEMORIES&message=note+to+delete", token);

        String id = matcher(created.body(), "id=\"note-(\\d+)\"");
        assertThat(id).as("the create response (status %d) carries the new note", status(created)).isNotNull();

        HttpResponse<String> deleted = htmxDelete("/notes/" + id, token);

        assertThat(status(deleted))
                .as("DELETE carrying the token from the page that listed the note")
                .isIn(200, 302);
        assertThat(deleted.body())
                .as("the deleted note is gone from the refreshed grid")
                .doesNotContain("id=\"note-" + id + "\"");
    }

    @Test
    void aRequestWithNoTokenIsStillRejected() throws Exception {
        signIn();
        HttpResponse<String> response = post("/notes", "category=MEMORIES&message=no+token");

        assertThat(status(response)).isEqualTo(403);
    }

    @Test
    void aRequestWithAWrongTokenIsStillRejected() throws Exception {
        signIn();
        HttpResponse<String> response = post("/notes",
                "_csrf=not-a-real-token&category=MEMORIES&message=bad+token");

        assertThat(status(response)).isEqualTo(403);
    }

    @Test
    void theConsoleStaysOutsideCsrfEntirely() throws Exception {
        // No token at all, yet the console's own sign-in must not be refused.
        HttpResponse<String> response = post("/h2-console/login.do",
                "driver=org.h2.Driver&url=jdbc%3Ah2%3Amem%3ANoteDB&user=sa&password=&name=");

        assertThat(status(response)).isNotIn(401, 403);
    }

    }
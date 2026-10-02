package com.example.demo.Security;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * The H2 console is deliberately outside the app's token auth: an operator opens it straight
 * from the browser and signs in with the database's own JDBC credentials.
 *
 * <p>These run against a real servlet container over HTTP because the console is a servlet
 * registration, not a Spring MVC handler - MockMvc only dispatches the DispatcherServlet and
 * would 404 on every one of these paths.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConsoleAccessTest {

    @LocalServerPort
    private int port;

    /** Redirects are followed, so the console's own /h2-console -> /h2-console/ hop resolves. */
    private final HttpClient following = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    /** Redirects are surfaced, so a security redirect can be asserted rather than followed. */
    private final HttpClient notFollowing = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    private HttpResponse<String> get(HttpClient client, String path) throws Exception {
        return client.send(HttpRequest.newBuilder(uri(path)).GET().build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private HttpResponse<String> postForm(HttpClient client, String path, String form) throws Exception {
        return client.send(HttpRequest.newBuilder(uri(path))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }

    @Test
    void consoleServesItsLoginFormToAnAnonymousCaller() throws Exception {
        HttpResponse<String> response = get(following, "/h2-console");

        // 200, not a redirect to /login: the console serves its own sign-in page (via a small
        // JS hop to login.jsp) instead of the app's token form.
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("<title>H2 Console</title>");
        assertThat(response.headers().firstValue("Location")).isEmpty();
    }

    @Test
    void consoleSignInIsNotRejectedAsMissingACsrfToken() throws Exception {
        // No _csrf parameter and no cookie: if the app's CSRF filter were applied here, the
        // console's own sign-in POST would come back 403 and never reach the database.
        HttpResponse<String> response = postForm(notFollowing, "/h2-console/login.do",
                "driver=org.h2.Driver&url=jdbc%3Ah2%3Amem%3ANoteDB&user=sa&password=&name=");

        assertThat(response.statusCode()).isNotIn(401, 403);
    }

    @Test
    void otherPagesStillRequireAToken() throws Exception {
        HttpResponse<String> response = get(notFollowing, "/notes");

        assertThat(response.statusCode()).isEqualTo(302);
        assertThat(response.headers().firstValue("Location"))
                .hasValueSatisfying(location -> assertThat(location).endsWith("/login?returnUrl=%2Fnotes"));
    }

    @Test
    void theHomePageStaysOpen() throws Exception {
        assertThat(get(notFollowing, "/").statusCode()).isEqualTo(200);
    }
}

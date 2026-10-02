package com.example.demo.Security;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

class LoginRedirectEntryPointTest {

    private final LoginRedirectEntryPoint entryPoint = new LoginRedirectEntryPoint();

    private String redirectFor(String uri, String queryString) throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        if (queryString != null) {
            request.setQueryString(queryString);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new AuthenticationException("anonymous") {});

        return response.getRedirectedUrl();
    }

    @Test
    void sendsTheVisitorToTheLoginPage() throws IOException {
        assertThat(redirectFor("/notes", null)).isEqualTo("/login?returnUrl=%2Fnotes");
    }

    @Test
    void keepsTheOriginalQueryStringInTheReturnUrl() throws IOException {
        assertThat(redirectFor("/fragments/note/7/edit", "mode=compact"))
                .isEqualTo("/login?returnUrl=%2Ffragments%2Fnote%2F7%2Fedit%3Fmode%3Dcompact");
    }

    @Test
    void honoursTheContextPath() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/app/notes");
        request.setContextPath("/app");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new AuthenticationException("anonymous") {});

        assertThat(response.getRedirectedUrl()).isEqualTo("/app/login?returnUrl=%2Fapp%2Fnotes");
    }
}

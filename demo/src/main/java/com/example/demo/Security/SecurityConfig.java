package com.example.demo.Security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

/**
 * Token-based authentication for a server-rendered Thymeleaf app.
 *
 * <p>The session is stateless: the only thing proving who the caller is the auth token
 * cookie, resolved to a SecurityContext on every request by {@link TokenAuthenticationFilter}.
 * Sign-in goes through the normal form-login filter, but the success handler issues the token
 * instead of a session, so the browser ends up holding exactly one credential.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Built here rather than as its own bean: exposing an {@code AuthenticationProvider} bean makes
     * Spring Security warn that the {@code UserDetailsService} is being bypassed, which is exactly
     * what this method arranges for it.
     */
    @Bean
    public AuthenticationManager authenticationManager(AppUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

/**
     * Session-change strategies with the CSRF one removed.
     *
     * <p>Spring Security composes {@code CsrfAuthenticationStrategy} into the session
     * authentication strategy list, and its job is to <em>delete</em> the CSRF token whenever the
     * authenticated identity changes. That assumption does not hold here:
     * {@link TokenAuthenticationFilter} rebuilds the authentication on <em>every single
     * request</em> from the token cookie, so the strategy sees a "changed" identity each time and
     * tried to wipe the CSRF token each time.
     *
     * <p>Passing an explicit composite replaces the default list wholesale, so only the
     * session-related entries are re-declared. Nothing that needs a session belongs here: the
     * session is never created, let alone reused, so session fixation has no attack to prevent
     * and concurrent-session control has no session to count against.
     */
    @Bean
    public CompositeSessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new CompositeSessionAuthenticationStrategy(List.of(
                new NullAuthenticatedSessionStrategy()));
    }

    /**
     * CSRF token storage, and the reason the default is wrong for this app.
     *
     * <p>Spring Security's default repository keeps the token in the HttpSession. This app is
     * {@link SessionCreationPolicy#STATELESS}, so the session is never actually persisted -
     * every response carries a fresh JSESSIONID. The token minted on page load therefore lives
     * in a session that is gone by the time the browser posts, and the first POST after any
     * page render fails with 403. Worse, it looked intermittent: the token held in the page
     * worked exactly once, because the request that carried it happened to reuse the same
     * not-yet-invalidated session.
     *
     * <p>A cookie repository sidesteps all of it - the token travels back with the request
     * regardless of session handling, so it stays valid for as many mutations as the user makes.
     * {@code XSRF-TOKEN} is not marked HttpOnly on purpose: it is not a credential for
     * anything, it is only echoed back as a header by htmx, which cannot read cookies.
     *
     * @see PersistentCookieCsrfTokenRepository
     */
    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        PersistentCookieCsrfTokenRepository repository = new PersistentCookieCsrfTokenRepository();
        repository.setCookieCustomizer(cookie -> cookie.sameSite("Lax").path("/"));
        return repository;
    }

    /**
     * Plain, un-masked token handling.
     *
     * <p>Spring Security defaults to {@code XorCsrfTokenRequestAttributeHandler}, which
     * BREACH-hardens the token by XOR-ing it with a per-render random mask. That is sound on its
     * own, but it means the value written into the page is not the value in the cookie, and the
     * value in the page is what htmx echoes back as a header. Un-masking then has to succeed
     * against a token the server never stored in that form, and every mutation 403s.
     *
     * <p>With a cookie repository the mask buys nothing anyway: an attacker who can read the
     * cross-site response can read the cookie value too. So the raw token is rendered, the raw
     * token is what the browser sends back, and the two always agree.
     */
    @Bean
    public CsrfTokenRequestAttributeHandler csrfTokenRequestAttributeHandler() {
        return new CsrfTokenRequestAttributeHandler();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
            AuthenticationManager authenticationManager,
            TokenAuthenticationFilter tokenAuthenticationFilter,
            LoginRedirectEntryPoint loginRedirectEntryPoint,
            TokenAuthenticationSuccessHandler successHandler,
            TokenAuthenticationFailureHandler failureHandler,
            TokenLogoutSuccessHandler logoutSuccessHandler) throws Exception {

        http
                .authenticationManager(authenticationManager)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        .sessionAuthenticationStrategy(sessionAuthenticationStrategy()))
                .authorizeHttpRequests(auth -> auth
                        // The landing page and the two auth forms stay open: they are how a
                        // visitor gets a token in the first place.
                        .requestMatchers("/", "/login", "/signup", "/error").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                        // The container healthcheck polls this with no token, so it has to answer
                        // 200 rather than bounce the caller through the login redirect.
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        // The H2 console is an operator tool: it is opened straight from the
                        // browser and has its own JDBC URL / username / password form, so it is
                        // deliberately outside the app's token auth.
                        .requestMatchers("/h2-console/**").permitAll()
                        // /about, /notes, every /fragments partial and every note mutation
                        // need a token.
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(loginRedirectEntryPoint))
                // The H2 console renders inside frames; without this its own login page is blocked.
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler(successHandler)
                        .failureHandler(failureHandler)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(logoutSuccessHandler)
                        .invalidateHttpSession(false)
                        .clearAuthentication(true)
                        .deleteCookies("note_token")
                        .permitAll())
                // The console authenticates against the database with a form that POSTs to
                // /h2-console/login.do and carries no CSRF token, so the check has to be skipped
                // for that path or the console's own sign-in is rejected with 403.
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository())
                        .csrfTokenRequestHandler(csrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers("/h2-console/**"))
                .httpBasic(basic -> basic.disable())
                .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

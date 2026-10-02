# Another UI issues

- after the authentication to move on note page to click the button to create new note its not work and not shown the popup panel to shown to create new note page i alread mention note creation ui in /project-info.md file analyze them the title Navlink and page structure section
- In database when i go to /h2-console to give username password to go the database so remove the spring security authentication in the database path

<!-- Both resolved. -->

1. **Add New Note button did nothing.** Cause was not the markup: all three CDN
   `integrity` hashes in `templates/fragments/layout.html` were stale, so the browser
   refused bootstrap.min.css, bootstrap.bundle.min.js and htmx.min.js outright. No
   Bootstrap JS means no modal. Hashes recomputed from the live 5.3.8 / 2.0.11 bytes.
2. **/h2-console required app auth.** `Security/H2ConsoleAuthFilter.java` deleted;
   `SecurityConfig` now permits `/h2-console/**` and ignores CSRF for it, because the
   console's own JDBC sign-in POST carries no CSRF token and would 403 otherwise.

Verified: `.\mvnw.cmd test` green (40 tests, including the new
`Security.ConsoleAccessTest`). Manually confirmed against a running jar that
`GET /h2-console` serves H2's login page anonymously, the console sign-in POST is not
CSRF-rejected, the notes page's served SRI hashes match the real CDN bytes, and an
htmx create returns the out-of-band grid refresh.

---

## Second round: only the FIRST note could be created

After signing in, one note saved and **every later mutation 403'd** - create, edit and
delete alike. It looked like a flaky CSRF check because the first POST after a page load
worked.

**Cause.** The auth token cookie is re-resolved into an `Authentication` on every request
by `TokenAuthenticationFilter`, so Spring Security's `CsrfAuthenticationStrategy` sees a
new identity on every single request and expires the CSRF token every single request. The
response to a successful mutation went out with `XSRF-TOKEN=; Expires=1970`, so the token
already rendered into the page was dead by the next click. Repeated writes were impossible;
a reload was not a workaround either, because the page re-render handed out a fresh token
that the *next* mutation then consumed.

**Fix.** `Security/PersistentCookieCsrfTokenRepository.java` - a cookie-backed repository
that ignores `saveToken(null, ...)`. Rotating the token on an identity change protects a
*session*-bound token; this app is stateless, the identity lives in an HttpOnly cookie, and
rotating only breaks the open tab. `SecurityConfig` also drops `CsrfAuthenticationStrategy`
via an explicit `sessionAuthenticationStrategy()` and renders the token unmasked
(`CsrfTokenRequestAttributeHandler`), because the masked value is not the cookie value.

**Verified:** `Security.CsrfTokenRepositoryTest` - one token now survives five consecutive
creates, a create-then-delete on the same token, a missing token, a forged token, and the
CSRF-exempt console. 53 tests green. Confirmed again through the container.

## UI rework

Not a bug, a request. `static/css/app.css` is now a token-driven design system (warm paper
palette, amber accent, three category hues, dark mode through `data-bs-theme`) that feeds
Bootstrap through its own `--bs-*` variables instead of forking any component. Templates
were rebuilt on the Bootstrap 5.3 reference patterns: hero + feature tiles on the landing
page, a per-category stat strip above the notes grid, category-coloured note cards, and a
navbar with a monogram, a theme toggle and a restructured profile menu.

Two things worth knowing if you touch this next:

- **The stat strip counts every note in the database, not just yours.** That is pre-existing
  - `NoteService.findAll()` has never been owner-scoped. `countByCategory()` follows it, so
  the numbers are right for the current data model and will be wrong for a multi-user
  account. Fixing it means scoping `findAll()` to the authenticated owner.
- **The CSRF header is `X-XSRF-TOKEN`, not `X-CSRF-TOKEN`.** A cookie repository names its
  own header. `static/js/app.js` reads the name out of the `csrf-header` meta tag rather
  than hardcoding it; anything else that posts by hand must do the same or it will 403.
# simple_note_creation

A token-authenticated "Memory Notes" pad — write down your life, memories and ideas.
Sign up, then create, edit and delete short notes filed under one of three categories
(Daily routine, Memories, Something else).

Spring Boot 4.1.1 · Java 21 · Spring Security 7 · Thymeleaf · HTMX 2.0.11 · Bootstrap 5.3.8 · H2

## Layout

The repo root holds no build file. All Maven commands run from `demo/`.

```
demo/
├── docker-compose.yml          # multi-stage build, publishes 8080, healthcheck on /actuator/health
├── pom.xml                     # Boot 4.1.1, Java 21
├── project-Info.md             # the original requirement list
├── AGENTS.md                   # architecture, verified gotchas, before-you-finish checklist
└── src/
    ├── main/java/com/example/demo/
    │   ├── Config/             # data seeding
    │   ├── Controller/         # HelloController (unused scaffold), HomeController, NoteController,
    │   │                       # LoginController, SignupController, GlobalExceptionHandler
    │   ├── Model/              # Note, Category, AuthSource, ErrorEnquiry
    │   ├── Repository/         # JPA repositories
    │   ├── Security/           # SecurityConfig, TokenAuthenticationFilter, TokenService,
    │   │                       # PersistentCookieCsrfTokenRepository, LoginRedirectEntryPoint
    │   └── Service/            # AuthService, NoteService, ErrorEnquiryService
    └── main/resources/
        ├── templates/
        │   ├── pages/          # home, about, notes, login, signup, error (full documents)
        │   └── fragments/      # layout, navbar, footer, notes-grid, note-card, note-form,
        │                       # note-edit-form, note-created, error-alert, flash
        └── static/             # css/app.css (design tokens + Bootstrap bridge), js/app.js
```

Subpackage names are Capitalized, inherited from the original scaffold — that is the
project convention, not a typo.

## Run it

Requires Docker Desktop to be **running** (the CLI alone is not enough; `docker info` must
return a server version).

```powershell
cd demo
docker compose up -d --build
```

Then open <http://localhost:8080>. The database is in-memory, so all data is lost on
restart. To persist it, uncomment the `DATABASE_URL` line in `docker-compose.yml` (the
`notedb` volume is already mounted for exactly that) and point it at
`jdbc:h2:file:/data/NoteDB`.

To run without Docker:

```powershell
cd demo
.\mvnw.cmd spring-boot:run     # http://localhost:8080
```

Use the wrapper, not a system `mvn` — it bootstraps Maven 3.9.16 itself.

## Test

`test` is the only gate; there is no lint or typecheck step.

```powershell
cd demo
.\mvnw.cmd clean test           # 53 tests
```

## Pages

| Route | Auth | What it is |
| --- | --- | --- |
| `/` | public | Landing page: hero, feature tiles |
| `/about` | **token** | About page |
| `/notes` | **token** | The pad — stat strip, notes grid, create modal, inline edit |
| `/login` | public | Sign in |
| `/signup` | public | Create an account; issues a token immediately |
| `/h2-console` | public | H2's own JDBC console — **dev/operator tool, do not publish** |
| `/actuator/health` | public | Liveness only |

Anonymous requests to `/notes` redirect to `/login?returnUrl=%2Fnotes`.

## Things that will bite you

These are all verified and written up in `demo/AGENTS.md`. The short version:

- **The CSRF token must never be rotated.** `TokenAuthenticationFilter` builds a fresh
  `Authentication` per request, so Spring Security's stock strategy expires the token on
  every single request and every mutation after the first one 403s.
  `PersistentCookieCsrfTokenRepository` ignores `saveToken(null, …)`. Don't remove it.
- **The CSRF header is `X-XSRF-TOKEN`**, not `X-CSRF-TOKEN` — a cookie repository names its
  own header. Read it from the `csrf-header` meta tag instead of hardcoding it.
- **Recompute the CDN `integrity` hashes whenever you bump a version.** A stale hash makes
  the browser refuse the asset outright, which silently kills every Bootstrap behaviour and
  all of HTMX while the markup still looks correct.
- **HTMX attributes need `th:attr`, not `th:hx-*`.** `th:hx-post="@{/notes}"` ships the
  literal string `hx-post="@{/notes}"`; htmx then requests a URL containing `@{`.
- **`NoteService.findAll()` is not owner-scoped**, so every signed-in user sees every note.
  Ownership is enforced on edit and delete only.

## License

Private project — no license granted.
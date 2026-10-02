# HTMX Project Reference for Coding Agents

> **Purpose:** Project-oriented HTMX reference for an AI coding agent. Use this file when designing, implementing, reviewing, debugging, or modifying an HTMX-based web application.
>
> **Primary source:** https://htmx.org/docs/
>
> **Documentation reviewed:** HTMX 2.x documentation. Verify version-specific behavior against the official documentation when a project pins a different version.

---

## 1. Core Mental Model

HTMX extends normal HTML so that HTML elements can:

- issue HTTP requests
- react to browser or custom events
- use HTTP methods beyond normal anchor/form behavior
- choose exactly which DOM element receives a response
- choose how returned HTML is inserted
- update browser history
- coordinate requests
- integrate with server-rendered HTML

The preferred architecture is generally:

```text
Browser
  |
  | HTML element + hx-* attributes
  v
HTTP request
  |
  v
Server-side route/controller
  |
  | render HTML / HTML fragment
  v
HTMX
  |
  | target + swap strategy
  v
DOM update
```

### Important rule

HTMX applications normally return **HTML**, not JSON, for HTMX requests.

Use JSON only when the application genuinely needs a JSON API or JavaScript-driven client behavior.

---

# 2. Installation

## CDN

Simple projects can load HTMX directly:

```html
<script
  src="https://cdn.jsdelivr.net/npm/htmx.org@2.0.11/dist/htmx.min.js"
  integrity="sha384-2OatzQy1H+Zd/IIrjr1TcuDGqLXeHhbooAyJY1KdQMKnr4LZ22k31GBLdYKHmVjg"
  crossorigin="anonymous">
</script>
```

For production applications, consider whether the project should vendor or bundle the dependency instead of relying on a CDN.

## npm

```bash
npm install htmx.org
```

Typical import:

```js
import 'htmx.org';
```

If the application needs the global `htmx` object, configure the project accordingly.

---

# 3. Fundamental HTMX Attributes

| Attribute | Purpose |
|---|---|
| `hx-get` | Send GET request |
| `hx-post` | Send POST request |
| `hx-put` | Send PUT request |
| `hx-patch` | Send PATCH request |
| `hx-delete` | Send DELETE request |
| `hx-trigger` | Define event that starts request |
| `hx-target` | Select element that receives response |
| `hx-swap` | Define how response enters DOM |
| `hx-select` | Select part of response before swapping |
| `hx-select-oob` | Select out-of-band response fragments |
| `hx-swap-oob` | Update another DOM location from response |
| `hx-indicator` | Select loading indicator |
| `hx-disabled-elt` | Disable element during request |
| `hx-include` | Include values from other elements |
| `hx-params` | Include/exclude request parameters |
| `hx-vals` | Add additional values |
| `hx-vars` | Add dynamically evaluated values |
| `hx-confirm` | Confirm action before request |
| `hx-sync` | Coordinate multiple requests |
| `hx-boost` | AJAX-enable normal links/forms |
| `hx-push-url` | Push URL into browser history |
| `hx-replace-url` | Replace current browser URL |
| `hx-history` | Control history snapshotting |
| `hx-preserve` | Preserve an element across swaps |
| `hx-ext` | Enable extensions |
| `hx-on:*` / `hx-on-` | Handle events locally |
| `hx-headers` | Add request headers |
| `hx-encoding` | Configure request encoding |
| `hx-prompt` | Prompt for user input |
| `hx-disabled-elt` | Temporarily disable selected elements |

---

# 4. HTTP Requests

## GET

```html
<button hx-get="/users">
  Load Users
</button>
```

## POST

```html
<form hx-post="/users">
  <input name="name">
  <button type="submit">Create</button>
</form>
```

## PUT

```html
<button hx-put="/users/42">
  Update
</button>
```

## PATCH

```html
<button hx-patch="/users/42">
  Save Changes
</button>
```

## DELETE

```html
<button hx-delete="/users/42"
        hx-confirm="Delete this user?">
  Delete
</button>
```

### Agent rule

Choose the HTTP method based on the server-side operation. Do not use `POST` for every action merely because it is convenient.

---

# 5. Request Triggers

Default natural events:

- `input`, `textarea`, `select` → `change`
- `form` → `submit`
- most other elements → `click`

Override behavior with `hx-trigger`.

```html
<div
  hx-get="/notifications"
  hx-trigger="click">
  Load Notifications
</div>
```

## Common trigger modifiers

### once

```html
<div hx-get="/data" hx-trigger="click once">
```

### changed

Only request when the value changed:

```html
<input
  hx-get="/search"
  hx-trigger="keyup changed">
```

### delay

```html
<input
  hx-get="/search"
  hx-trigger="keyup changed delay:500ms">
```

Useful for debounced search.

### throttle

```html
<div
  hx-get="/updates"
  hx-trigger="mousemove throttle:1s">
```

### from

Listen for an event on another element:

```html
<div
  hx-get="/action"
  hx-trigger="click from:body">
```

Be careful with selectors that refer to dynamic content; `from:` selectors are not continuously re-evaluated.

---

# 6. Special Triggers

## load

Runs when the element is loaded.

```html
<div
  hx-get="/dashboard"
  hx-trigger="load">
</div>
```

## revealed

Runs when an element first becomes visible through scrolling.

```html
<div
  hx-get="/more-items"
  hx-trigger="revealed">
</div>
```

## intersect

Intersection Observer-style behavior:

```html
<div
  hx-get="/more-items"
  hx-trigger="intersect threshold:0.5">
</div>
```

---

# 7. Polling

Poll an endpoint repeatedly:

```html
<div
  hx-get="/notifications"
  hx-trigger="every 5s">
</div>
```

Server-driven polling can stop when the server returns the documented `286` response.

## Load polling

Useful for jobs/progress:

```html
<div
  hx-get="/job-status"
  hx-trigger="load delay:1s"
  hx-swap="outerHTML">
</div>
```

The returned fragment can contain another polling element until the operation finishes.

### Agent guidance

Use polling only where appropriate. Prefer event-driven or server-push mechanisms when the product requirements allow them.

---

# 8. Targeting DOM Elements

Default behavior: the response is swapped into the requesting element.

Use `hx-target` to update another element:

```html
<input
  name="q"
  hx-get="/search"
  hx-target="#results">

<div id="results"></div>
```

## Useful target selectors

```html
hx-target="#results"
hx-target=".results"
hx-target="this"
hx-target="closest tr"
hx-target="closest .card"
hx-target="next .result"
hx-target="previous .result"
hx-target="find .result"
```

### Agent rule

Prefer semantic/relative targets where they improve component reuse, but avoid selectors so complex that the behavior becomes difficult to understand.

---

# 9. Swap Strategies

Default:

```html
hx-swap="innerHTML"
```

Available core strategies:

| Strategy | Effect |
|---|---|
| `innerHTML` | Replace target's children |
| `outerHTML` | Replace target itself |
| `afterbegin` | Insert at beginning inside target |
| `beforebegin` | Insert before target |
| `beforeend` | Insert at end inside target |
| `afterend` | Insert after target |
| `delete` | Delete target |
| `none` | Do not insert normal response content |

Examples:

```html
<div
  hx-get="/items"
  hx-target="#list"
  hx-swap="beforeend">
</div>
```

```html
<button
  hx-delete="/items/42"
  hx-target="closest .item"
  hx-swap="outerHTML">
  Delete
</button>
```

### Swap modifiers

Common modifiers include:

```text
transition:true
swap:100ms
settle:100ms
ignoreTitle:true
scroll:top
scroll:bottom
show:top
show:bottom
```

Example:

```html
<div hx-get="/panel"
     hx-swap="innerHTML transition:true">
</div>
```

---

# 10. CSS Transitions

HTMX can work with CSS transitions when stable element IDs are preserved.

Example:

```html
<div id="message">Old Content</div>
```

Server returns:

```html
<div id="message" class="updated">
  New Content
</div>
```

CSS:

```css
.updated {
  transition: all 1s ease-in;
}
```

### Agent rule

When designing animated HTMX fragments, preserve stable IDs where the transition depends on matching old/new DOM elements.

---

# 11. Loading Indicators

Use `htmx-indicator`:

```html
<button hx-get="/data">
  Load

  <span class="htmx-indicator">
    Loading...
  </span>
</button>
```

HTMX adds `htmx-request` during the request.

Custom indicator:

```html
<button
  hx-get="/data"
  hx-indicator="#loading">
  Load
</button>

<div id="loading" class="htmx-indicator">
  Loading...
</div>
```

Useful CSS:

```css
.htmx-indicator {
  opacity: 0;
}

.htmx-request .htmx-indicator {
  opacity: 1;
}
```

---

# 12. Disabling UI During Requests

Use `hx-disabled-elt` where appropriate.

```html
<button
  hx-post="/checkout"
  hx-disabled-elt="this">
  Pay
</button>
```

This helps prevent duplicate submissions.

---

# 13. Forms and Parameters

Normal form controls use their `name` values as request parameter names.

```html
<form hx-post="/users">
  <input name="name">
  <input name="email">
  <button>Save</button>
</form>
```

## Include other elements

```html
<button
  hx-post="/save"
  hx-include="#profile-extra">
  Save
</button>
```

## Filter parameters

Use `hx-params` to include/exclude parameters.

## Add explicit values

```html
<button
  hx-post="/action"
  hx-vals='{"mode":"quick"}'>
  Run
</button>
```

Dynamic values can use the JavaScript-supported form where appropriate, but do not use dynamic evaluation unnecessarily.

---

# 14. File Uploads

Use:

```html
<form
  hx-post="/upload"
  hx-encoding="multipart/form-data">

  <input type="file" name="file">

  <button>Upload</button>
</form>
```

The server must handle multipart form data.

Upload progress can be handled through HTMX progress events.

---

# 15. Confirmation

Simple confirmation:

```html
<button
  hx-delete="/account"
  hx-confirm="Are you sure?">
  Delete Account
</button>
```

For custom modal dialogs, use the `htmx:confirm` event and call the request only after the asynchronous confirmation succeeds.

---

# 16. Attribute Inheritance

Many HTMX attributes are inherited by descendants.

Example:

```html
<div hx-confirm="Are you sure?">

  <button hx-delete="/account">
    Delete
  </button>

  <button hx-put="/account">
    Update
  </button>

</div>
```

Both buttons inherit the confirmation behavior.

Disable inheritance for a subtree/attribute using:

```html
hx-disinherit="hx-confirm"
```

Or unset an inherited attribute when supported:

```html
hx-confirm="unset"
```

### Agent rule

Inheritance can reduce duplication, but do not hide important behavior so deeply that a developer cannot understand where an HTMX request gets its configuration.

---

# 17. HTMX Boost

Boost normal links and forms:

```html
<body hx-boost="true">
```

Links/forms can then use AJAX navigation while retaining normal behavior when JavaScript is unavailable.

Example:

```html
<nav hx-boost="true">
  <a href="/dashboard">Dashboard</a>
  <a href="/settings">Settings</a>
</nav>
```

### Progressive enhancement

Prefer normal `href`, `action`, and `method` values whenever possible so the application retains useful non-JavaScript behavior.

---

# 18. History and Navigation

HTMX can integrate with browser history.

Examples:

```html
<a
  hx-get="/products"
  hx-target="#content"
  hx-push-url="true">
  Products
</a>
```

Or:

```html
<button hx-get="/products"
        hx-target="#content"
        hx-push-url="/products">
  Products
</button>
```

### Important server requirement

A URL pushed into browser history must also be directly navigable and capable of returning a full page when the user opens it directly or history restoration requires it.

### Sensitive pages

Avoid storing sensitive page content in history snapshots:

```html
<div hx-history="false">
```

---

# 19. Server Detection of HTMX Requests

HTMX sends useful request headers.

Important headers:

```text
HX-Request
HX-Boosted
HX-Current-URL
HX-History-Restore-Request
HX-Target
HX-Trigger
HX-Trigger-Name
HX-Prompt
```

Most server applications should use:

```text
HX-Request: true
```

to distinguish an HTMX request from a normal browser request when different HTML representations are needed.

### Recommended server pattern

```text
Normal request
    -> full HTML page

HTMX request
    -> HTML fragment/partial
```

Do not assume every endpoint must have two representations; use this pattern only when the UI architecture needs it.

---

# 20. Server Responses

HTMX normally expects HTML or an HTML fragment.

Example server response:

```html
<div id="result">
  Operation completed successfully.
</div>
```

### 204 No Content

A `204` response can indicate that there is nothing to swap.

### Error responses

By default, error status responses such as 4xx/5xx are treated differently from successful responses. Application-specific response handling can be configured.

---

# 21. Response Handling

HTMX has a configurable `htmx.config.responseHandling` array.

Conceptually:

```js
responseHandling: [
  { code: "204", swap: false },
  { code: "[23]..", swap: true },
  { code: "[45]..", swap: false, error: true },
  { code: "...", swap: false }
]
```

A common application pattern is to treat validation errors such as `422` as renderable HTML:

```text
POST /users
    |
    +-- 200 -> success fragment
    |
    +-- 422 -> validation-error fragment
    |
    +-- 500 -> error handling
```

This can be configured globally or through supported extensions/events.

---

# 22. HTMX Response Headers

Useful response headers include:

| Header | Purpose |
|---|---|
| `HX-Location` | Client-side navigation without full page reload |
| `HX-Push-Url` | Push URL into browser history |
| `HX-Redirect` | Redirect client to another URL |
| `HX-Refresh` | Request a full browser refresh |
| `HX-Replace-Url` | Replace current browser URL |

Use server response headers when the server needs to control client-side HTMX behavior without embedding JavaScript in the response.

---

# 23. Out-of-Band Swaps

A response can update another DOM element using `hx-swap-oob`.

Example:

```html
<div id="main-content">
  Updated content
</div>

<div id="cart-count"
     hx-swap-oob="true">
  5
</div>
```

This is useful when one server operation needs to update:

- main content
- notification count
- shopping cart count
- flash message
- navigation state

all from one response.

---

# 24. Selecting Response Content

Use `hx-select` when the server returns more HTML than the target needs.

```html
<div
  hx-get="/page"
  hx-select="#results"
  hx-target="#results">
</div>
```

For OOB content, `hx-select-oob` can select additional fragments.

---

# 25. Preserving DOM Elements

Use:

```html
<div id="player" hx-preserve="true">
  ...
</div>
```

Useful for stateful elements such as:

- media players
- embedded widgets
- complex client-side controls
- elements where losing DOM state is undesirable

Use sparingly because preserved content increases lifecycle complexity.

---

# 26. Partial Server-Sent Updates

HTMX supports server-sent partial swap commands through `<hx-partial>`.

Example:

```html
<div>Main updated content</div>

<hx-partial hx-target="#cart-count">
  3
</hx-partial>

<hx-partial hx-target="#cart-total">
  $29.97
</hx-partial>
```

Use this when one response must declaratively update multiple locations.

---

# 27. Request Synchronization

Use `hx-sync` when multiple elements can issue competing requests.

Example:

```html
<form hx-post="/store">

  <input
    name="title"
    hx-post="/validate"
    hx-trigger="change"
    hx-sync="closest form:abort">

  <button type="submit">
    Save
  </button>

</form>
```

Possible synchronization strategies include patterns such as:

```text
abort
drop
replace
queue
```

### Agent rule

If two controls can mutate the same server-side state, explicitly consider race conditions before implementing independent requests.

---

# 28. WebSockets and SSE

HTMX supports real-time communication through extensions.

Core extensions include:

- `sse`
- `ws`

Use them when the application needs server push or bidirectional updates.

Typical use cases:

```text
Chat
Live notifications
Progress updates
Live dashboards
Collaboration
Job status
```

Do not introduce WebSockets/SSE when ordinary request/response interactions are sufficient.

---

# 29. Extensions

HTMX provides an extension mechanism.

Core extensions include:

- `head-support`
- `htmx-1-compat`
- `idiomorph`
- `preload`
- `response-targets`
- `sse`
- `ws`

Enable an extension:

```html
<body hx-ext="response-targets">
```

Then use its documented attributes/features.

### Agent rule

Before implementing custom JavaScript for behavior that HTMX already supports through an official/core extension, check the extension documentation first.

---

# 30. Morphing Swaps

Morphing extensions can merge new DOM into existing DOM rather than replacing nodes directly.

Examples include:

- Idiomorph
- Morphdom-based swapping
- Alpine morph integration

Use morphing when preserving DOM state/focus/media state is important and ordinary swaps are insufficient.

Tradeoff: morphing can require more CPU and introduces another dependency/behavior layer.

---

# 31. Events

HTMX emits many lifecycle events.

Useful events include:

```text
htmx:configRequest
htmx:beforeRequest
htmx:afterRequest
htmx:beforeSwap
htmx:afterSwap
htmx:afterSettle
htmx:load
htmx:responseError
htmx:sendError
htmx:confirm
htmx:validateUrl
htmx:beforeHistorySave
```

Event names are available in camelCase and kebab-case forms.

Example:

```js
document.body.addEventListener("htmx:load", (event) => {
  initializeComponent(event.detail.elt);
});
```

Or:

```js
htmx.onLoad((target) => {
  initializeComponent(target);
});
```

---

# 32. Configure Requests with Events

Example:

```js
document.body.addEventListener("htmx:configRequest", (event) => {
  event.detail.parameters["source"] = "dashboard";
  event.detail.headers["X-App-Version"] = "1";
});
```

Use this for cross-cutting request behavior.

Do not put authentication secrets directly into visible HTML.

---

# 33. Customize Swapping with Events

Example:

```js
document.body.addEventListener("htmx:beforeSwap", (event) => {
  if (event.detail.xhr.status === 422) {
    event.detail.shouldSwap = true;
    event.detail.isError = false;
  }
});
```

This can support server-rendered validation fragments.

---

# 34. Local Event Behavior with hx-on

Example:

```html
<button
  hx-on:click="console.log('clicked')">
  Click
</button>
```

HTMX's event attributes allow behavior to remain close to the HTML it affects.

Use local event handlers for small UI behavior. Move larger business/client logic into maintainable JavaScript modules.

---

# 35. Third-Party JavaScript

When HTMX replaces DOM content, newly inserted elements may need initialization.

Use:

```js
htmx.onLoad((target) => {
  initializeThirdPartyWidgets(target);
});
```

If JavaScript inserts HTMX markup into the DOM outside of HTMX, call:

```js
htmx.process(element);
```

Example:

```js
const container = document.querySelector("#container");

fetch("/fragment")
  .then(response => response.text())
  .then(html => {
    container.innerHTML = html;
    htmx.process(container);
  });
```

### Agent rule

Always consider the lifecycle of third-party components after an HTMX swap.

---

# 36. Debugging

Useful techniques:

## Inspect the network request

Check:

- URL
- HTTP method
- status code
- request headers
- request parameters
- response HTML
- response headers

## Check HTMX headers

Look for:

```text
HX-Request
HX-Target
HX-Trigger
HX-Current-URL
```

## Add HTMX logging

```js
htmx.logger = function (element, event, data) {
  console.log(event, element, data);
};
```

## Debug request lifecycle

Listen for:

```js
document.body.addEventListener("htmx:beforeRequest", console.log);
document.body.addEventListener("htmx:afterRequest", console.log);
document.body.addEventListener("htmx:beforeSwap", console.log);
document.body.addEventListener("htmx:afterSwap", console.log);
```

### Debugging checklist

1. Did the event fire?
2. Did the request start?
3. Is the URL correct?
4. Is the HTTP method correct?
5. Are parameters present?
6. Did the server return the expected HTML?
7. Is the response status expected?
8. Does `hx-target` resolve to an element?
9. Is `hx-swap` correct?
10. Did another request overwrite the result?
11. Did a third-party library fail after the swap?
12. Is browser caching involved?

---

# 37. Caching

HTMX uses normal HTTP caching mechanisms.

If the same URL can return different representations depending on headers, make sure the server uses the appropriate `Vary` response header.

Important example:

```http
Vary: HX-Request
```

This matters when:

```text
Normal request -> full HTML
HTMX request   -> HTML fragment
```

If both responses are cached under only the URL, the wrong representation can be returned.

ETag and Last-Modified can also be used normally.

---

# 38. Security

## Rule 1: Escape untrusted content

Never trust user-generated HTML.

Avoid inserting unsanitized HTML directly into the DOM.

If raw HTML is unavoidable, sanitize it.

Pay special attention to HTMX-specific attributes:

```text
hx-*
data-hx-*
```

and script tags.

## hx-disable

Use:

```html
<div hx-disable>
  ...
</div>
```

to prevent HTMX processing inside untrusted/raw HTML content.

## History cache

Do not store sensitive information in the browser history cache.

Use:

```html
hx-history="false"
```

where appropriate.

## Configuration security options

Relevant options include:

```js
htmx.config.selfRequestsOnly = true;
htmx.config.allowScriptTags = false;
htmx.config.historyCacheSize = 0;
htmx.config.allowEval = false;
```

Do not enable/disable these blindly. Evaluate application requirements and compatibility.

---

# 39. URL Validation

For applications that must allow requests to selected external domains, validate the URL using:

```text
htmx:validateUrl
```

Example:

```js
document.body.addEventListener("htmx:validateUrl", (event) => {
  if (
    !event.detail.sameHost &&
    event.detail.url.hostname !== "trusted.example.com"
  ) {
    event.preventDefault();
  }
});
```

Prefer same-origin requests unless there is a clear reason to use cross-origin requests.

---

# 40. Content Security Policy

Use CSP as a security layer.

Example:

```html
<meta
  http-equiv="Content-Security-Policy"
  content="default-src 'self';">
```

A real production CSP should be designed for the application's actual resources and JavaScript architecture.

---

# 41. CSRF Protection

CSRF protection is primarily a backend responsibility.

HTMX can send a CSRF token using request headers:

```html
<body
  hx-headers='{"X-CSRF-TOKEN":"CSRF_TOKEN"}'>
```

Framework-native CSRF mechanisms are preferred where available.

### Agent rule

Never remove CSRF protection merely because the UI is implemented with HTMX.

---

# 42. CORS

When HTMX is used cross-origin, configure the server's CORS policy correctly.

Relevant headers include:

```text
Access-Control-Allow-Headers
Access-Control-Expose-Headers
```

Do not use `Access-Control-Allow-Origin: *` for authenticated applications without carefully evaluating the security implications.

---

# 43. Progressive Enhancement

Preferred pattern:

```html
<form action="/search" method="GET">
  <input
    name="q"
    hx-get="/search"
    hx-trigger="keyup changed delay:500ms"
    hx-target="#results">

  <button type="submit">
    Search
  </button>
</form>
```

This provides:

```text
JavaScript available
    -> enhanced HTMX experience

JavaScript unavailable
    -> normal HTML form behavior
```

### Agent rule

When possible, make HTMX an enhancement of a valid HTML interaction instead of making the application completely dependent on JavaScript.

---

# 44. Recommended HTMX Application Architecture

A maintainable server-rendered application can use:

```text
project/
├── src/
│   ├── controllers/
│   ├── services/
│   ├── repositories/
│   ├── models/
│   └── validation/
│
├── templates/
│   ├── layouts/
│   ├── pages/
│   ├── components/
│   └── fragments/
│
├── static/
│   ├── css/
│   ├── js/
│   └── images/
│
└── tests/
```

The exact folders depend on the backend framework.

## Recommended separation

### Full pages

```text
templates/pages/
```

Return complete HTML documents.

### Reusable components

```text
templates/components/
```

Cards, navigation, modals, forms, etc.

### HTMX fragments

```text
templates/fragments/
```

Small server-rendered pieces intended to be swapped into existing DOM.

---

# 45. Server Endpoint Design

Example:

```text
GET  /products
GET  /products/:id
POST /products
PATCH /products/:id
DELETE /products/:id
GET  /products/search
POST /products/:id/favorite
```

For each endpoint, define:

```text
Normal request representation
HTMX request representation
Success status
Validation error status
Authorization behavior
CSRF behavior
Fragment target
```

Example:

```text
POST /products

Normal request:
    redirect -> /products/123

HTMX request:
    return product-card.html

Validation:
    422 + form fragment

Authorization failure:
    403 + appropriate UI response
```

---

# 46. HTMX + Java/Spring Boot Pattern

For a Spring Boot application, a common structure is:

```text
src/main/java/
  controller/
  service/
  repository/
  model/

src/main/resources/
  templates/
    pages/
    fragments/
  static/
    css/
    js/
```

Example controller concept:

```java
@PostMapping("/users")
public String createUser(
        @Valid UserForm form,
        BindingResult result) {

    if (result.hasErrors()) {
        return "fragments/user-form";
    }

    userService.create(form);

    return "fragments/user-row";
}
```

The exact return path depends on the chosen template engine.

### Important

HTMX does not require Spring Boot. The same architectural idea can be used with Django, Rails, Laravel, ASP.NET, Node server rendering, Go templates, or another server-rendering framework.

---

# 47. HTMX + Template Engine Rules

When using server templates:

1. Keep fragments small.
2. Avoid duplicated HTML structures.
3. Give important interactive components stable IDs.
4. Keep validation errors in reusable fragments.
5. Keep business logic out of templates.
6. Escape untrusted content.
7. Return full pages for direct navigation.
8. Return fragments for targeted HTMX interactions.
9. Use consistent component naming.
10. Keep response status codes meaningful.

---

# 48. Common UI Patterns

## Live Search

```html
<input
  type="search"
  name="q"
  hx-get="/search"
  hx-trigger="keyup changed delay:300ms, search"
  hx-target="#results"
  hx-indicator="#search-loading">

<div id="search-loading" class="htmx-indicator">
  Searching...
</div>

<div id="results"></div>
```

## Delete Row

```html
<tr id="user-42">
  <td>Faleel</td>
  <td>
    <button
      hx-delete="/users/42"
      hx-target="closest tr"
      hx-swap="outerHTML"
      hx-confirm="Delete this user?">
      Delete
    </button>
  </td>
</tr>
```

## Inline Edit

```html
<div id="profile">
  <button
    hx-get="/profile/edit"
    hx-target="#profile"
    hx-swap="outerHTML">
    Edit
  </button>
</div>
```

Server returns:

```html
<form
  id="profile"
  hx-post="/profile"
  hx-target="#profile"
  hx-swap="outerHTML">

  <input name="name">

  <button type="submit">
    Save
  </button>
</form>
```

## Load More

```html
<div id="items">
  ...
</div>

<button
  hx-get="/items?page=2"
  hx-target="#items"
  hx-swap="beforeend">
  Load More
</button>
```

## Modal

```html
<button
  hx-get="/users/42/delete-confirmation"
  hx-target="#modal"
  hx-swap="innerHTML">
  Delete
</button>

<div id="modal"></div>
```

---

# 49. Error Handling Pattern

Recommended conceptual flow:

```text
Request
   |
   +--> 2xx
   |      |
   |      +--> success HTML fragment
   |
   +--> 422
   |      |
   |      +--> validation HTML fragment
   |
   +--> 401
   |      |
   |      +--> authentication UI
   |
   +--> 403
   |      |
   |      +--> authorization UI
   |
   +--> 404
   |      |
   |      +--> not-found fragment
   |
   +--> 5xx
          |
          +--> generic error UI
```

Do not use HTTP 200 for every failure just to make HTMX swap the response. Configure response handling when the application intentionally uses another status code such as 422 for validation.

---

# 50. Coding Agent Decision Rules

When asked to implement an HTMX feature:

## Step 1 — Identify the interaction

Example:

```text
User clicks Delete
```

## Step 2 — Choose HTTP method

```text
DELETE
```

## Step 3 — Choose endpoint

```text
DELETE /users/{id}
```

## Step 4 — Choose server response

```text
Empty/appropriate response if deletion removes the target
```

## Step 5 — Choose target

```text
closest tr
```

## Step 6 — Choose swap

```text
outerHTML
```

or:

```text
delete
```

depending on the desired behavior.

## Step 7 — Add UX

```text
hx-confirm
hx-disabled-elt
loading indicator
```

as needed.

## Step 8 — Check security

```text
authorization
CSRF
input validation
XSS
```

## Step 9 — Check progressive enhancement

If the action can reasonably work as a normal form/link, preserve that behavior.

---

# 51. Coding Agent Rules — Do and Don't

## DO

- Prefer semantic HTML.
- Use normal links/forms where appropriate.
- Return HTML fragments for HTMX interactions.
- Use meaningful HTTP methods.
- Use meaningful HTTP status codes.
- Keep fragments reusable.
- Use stable IDs for stateful/animated components.
- Handle loading states.
- Prevent accidental duplicate submissions.
- Validate on the server.
- Escape untrusted HTML.
- Protect state-changing requests against CSRF.
- Consider request races.
- Inspect HTMX events when debugging.
- Use official/core extensions when appropriate.
- Preserve direct URL navigation.
- Keep client-side JavaScript focused on behavior HTMX cannot reasonably express.

## DON'T

- Return JSON just because the frontend is dynamic.
- Build a large SPA architecture around HTMX unless there is a strong reason.
- Put business logic inside HTML attributes.
- Trust client-side validation.
- inject untrusted raw HTML.
- expose secrets in `hx-headers`, `hx-vals`, or page source.
- use `hx-delete` without server-side authorization.
- assume 200 means business success.
- ignore HTTP caching behavior.
- forget that swapped DOM may need third-party initialization.
- add WebSockets for interactions that only need normal HTTP.
- use complicated selector chains when a simple target is possible.

---

# 52. HTMX vs JavaScript: Boundary Guidelines

Use HTMX for:

```text
HTTP requests
Server-rendered HTML updates
Forms
Navigation
Partial page updates
CRUD interactions
Search
Pagination
Modals
Inline editing
Notifications
Progress polling
```

Use JavaScript when you need:

```text
Complex browser-only state
Canvas/WebGL
Advanced drag/drop behavior
Complex client-side calculations
Third-party widgets
Custom browser APIs
Rich offline state
Client-side libraries
```

Use both when appropriate.

HTMX does not mean "no JavaScript"; it means HTML can handle a large amount of interaction declaratively.

---

# 53. Performance Guidelines

Prefer:

```text
small HTML fragments
server-side pagination
appropriate HTTP caching
debounced search
minimal JavaScript
minimal DOM updates
```

Avoid:

```text
huge HTML responses
unnecessary polling
repeated full-page rendering for small changes
excessive client-side initialization
unbounded history snapshots
unnecessary morphing
```

---

# 54. Testing Strategy

## Unit tests

Test:

- service logic
- validation
- authorization
- repository logic

## Controller/integration tests

Test:

- HTTP method
- status code
- HTML fragment
- response headers
- authentication
- CSRF behavior

## Browser tests

Test:

- click triggers request
- target updates
- swap behavior
- validation UI
- loading state
- error UI
- browser history
- progressive enhancement where required

### Example acceptance test

```text
Given the user is viewing a user list
When the user clicks Delete
Then DELETE /users/{id} is issued
And the server validates authorization
And the user row is removed
And no duplicate request can be created by repeated clicks
```

---

# 55. Troubleshooting Matrix

| Symptom | Check |
|---|---|
| Nothing happens | HTMX loaded? trigger correct? JavaScript error? |
| Request goes to wrong URL | `hx-*` URL and inherited attributes |
| Request has missing values | `name`, form association, `hx-include`, `hx-params` |
| Response appears in wrong place | `hx-target` |
| Entire component disappears | `hx-swap="outerHTML"` or target selection |
| Response not inserted | status code / response handling |
| Loading spinner absent | `hx-indicator`, CSS |
| Duplicate form submissions | `hx-disabled-elt`, server idempotency |
| Search sends too many requests | `delay`, `changed`, `throttle` |
| Browser back breaks | history configuration / full URL response |
| Cache returns wrong content | `Vary: HX-Request` |
| Widget stops working after update | `htmx:load` / reinitialization |
| OOB update fails | matching IDs and valid response structure |
| Cross-origin request fails | CORS configuration |
| User HTML executes HTMX behavior | sanitize content / `hx-disable` |
| CSRF failure | framework token/header configuration |

---

# 56. Recommended Project Conventions

## HTML

Use:

```text
hx-* attributes
meaningful IDs
semantic elements
normal href/action fallback
```

## Endpoints

Use REST-like resource URLs:

```text
GET    /products
GET    /products/{id}
POST   /products
PATCH  /products/{id}
DELETE /products/{id}
```

## Fragments

Use clear names:

```text
fragments/product-card
fragments/product-form
fragments/product-row
fragments/search-results
fragments/validation-errors
fragments/notifications
```

## JavaScript

Organize non-trivial behavior:

```text
static/js/
  app.js
  components/
  integrations/
```

Use `htmx.onLoad()` for initialization that must happen after swaps.

---

# 57. Quick Attribute Cookbook

```html
<!-- GET -->
<button hx-get="/data">Load</button>

<!-- POST -->
<form hx-post="/users">...</form>

<!-- Target -->
<button hx-get="/data" hx-target="#result">Load</button>

<!-- Append -->
<button hx-get="/items" hx-target="#items" hx-swap="beforeend">
  Load More
</button>

<!-- Replace self -->
<button hx-get="/edit" hx-swap="outerHTML">
  Edit
</button>

<!-- Delete closest element -->
<button
  hx-delete="/items/1"
  hx-target="closest .item"
  hx-swap="outerHTML">
  Delete
</button>

<!-- Debounced search -->
<input
  hx-get="/search"
  hx-trigger="keyup changed delay:300ms"
  hx-target="#results">

<!-- Loading indicator -->
<button hx-get="/data" hx-indicator="#loading">
  Load
</button>

<!-- Confirmation -->
<button hx-delete="/account"
        hx-confirm="Are you sure?">
  Delete
</button>

<!-- Include another element -->
<button hx-post="/save" hx-include="#extra">
  Save
</button>

<!-- Additional values -->
<button hx-post="/action"
        hx-vals='{"mode":"quick"}'>
  Run
</button>

<!-- Boost navigation -->
<body hx-boost="true">

<!-- Browser history -->
<a hx-get="/dashboard"
   hx-target="#content"
   hx-push-url="true">
  Dashboard
</a>

<!-- Preserve -->
<div id="player" hx-preserve="true">
  ...
</div>
```

---

# 58. Final Coding-Agent Checklist

Before generating HTMX code, verify:

- [ ] Is HTMX 2.x the intended version?
- [ ] Is the interaction clearly defined?
- [ ] Is the HTTP method appropriate?
- [ ] Is the endpoint appropriate?
- [ ] Does the server return HTML/fragment as intended?
- [ ] Is `hx-target` correct?
- [ ] Is `hx-swap` correct?
- [ ] Is the trigger correct?
- [ ] Are form values correctly named?
- [ ] Is loading state handled?
- [ ] Are duplicate requests prevented where necessary?
- [ ] Are concurrent requests synchronized where necessary?
- [ ] Are validation errors represented with meaningful status codes?
- [ ] Is CSRF protection present?
- [ ] Is server-side authorization present?
- [ ] Is untrusted HTML escaped/sanitized?
- [ ] Is browser history behavior correct?
- [ ] Is caching safe for full pages vs fragments?
- [ ] Will third-party components survive DOM swaps?
- [ ] Is progressive enhancement desirable?
- [ ] Are extensions actually necessary?
- [ ] Is custom JavaScript kept to the smallest useful boundary?
- [ ] Are browser tests covering the interaction?

---

# 59. Source and Further Reference

Primary documentation:

https://htmx.org/docs/

Useful official sections:

- Documentation: https://htmx.org/docs/
- Reference: https://htmx.org/reference/
- Examples: https://htmx.org/examples/
- Extensions: https://htmx.org/extensions/

This file is an **implementation-oriented summary**, not a replacement for the official documentation. When exact syntax, edge cases, extension behavior, browser compatibility, or version-specific behavior matters, consult the official HTMX documentation.


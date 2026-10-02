# Bootstrap 5.3 — Web Application Development Reference

> **Purpose:** This document is a compact, agent-friendly reference for coding agents developing or modifying web applications with **Bootstrap 5.3**.
>
> **Primary source:** https://getbootstrap.com/docs/5.3/getting-started/introduction/
>
> **Documentation version checked:** Bootstrap 5.3.x; the official documentation currently identifies **v5.3.8**.
>
> **Important:** Use this file as a practical implementation reference. When an exact API, component option, class, or behavior is uncertain, consult the official Bootstrap 5.3 documentation rather than guessing.

---

## 1. Bootstrap Role in the Project

Bootstrap 5.3 is a frontend CSS/JavaScript toolkit intended for responsive web interfaces.

Use Bootstrap for:

- Responsive page layouts
- Containers, rows, and columns
- Spacing and sizing
- Typography
- Buttons
- Forms
- Navigation
- Cards
- Alerts
- Modals
- Dropdowns
- Tabs
- Accordions
- Carousels
- Toasts
- Offcanvas sidebars
- Responsive utilities
- Common visual states and component behavior

Bootstrap follows a **mobile-first** approach. Build the base/mobile layout first, then progressively add responsive behavior for larger breakpoints.

---

# 2. Core Development Rules for the Coding Agent

When generating or modifying UI code:

1. **Use Bootstrap 5.3 classes first** when Bootstrap already provides the required styling.
2. Avoid unnecessary custom CSS.
3. Prefer Bootstrap utility classes for common spacing, display, sizing, flexbox, typography, borders, shadows, positioning, and responsive behavior.
4. Use Bootstrap components when an equivalent component exists.
5. Keep custom CSS only for project-specific visual requirements that Bootstrap cannot express cleanly.
6. Do not invent Bootstrap class names.
7. Do not mix Bootstrap 4 syntax with Bootstrap 5.3 syntax.
8. Do not introduce another CSS framework unless explicitly requested.
9. Preserve existing application architecture and framework conventions.
10. Keep HTML semantic and accessible.
11. Build responsive behavior from the smallest viewport upward.
12. When Bootstrap JavaScript functionality is required, make sure the Bootstrap JS bundle is loaded.
13. For dropdowns, popovers, and tooltips, remember that Popper is required; the Bootstrap bundle already includes Popper.
14. Avoid writing custom JavaScript for behavior that Bootstrap already provides.
15. Keep components modular and reusable.

---

# 3. Required HTML Foundation

A Bootstrap page should use the HTML5 doctype:

```html
<!doctype html>
<html lang="en">
```

Recommended `<head>` foundation:

```html
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Application</title>
</head>
<body>
  <!-- application -->
</body>
</html>
```

## Viewport Rule

Always include:

```html
<meta name="viewport" content="width=device-width, initial-scale=1">
```

This is important for Bootstrap's mobile-first responsive behavior.

---

# 4. Bootstrap Installation Options

## 4.1 CDN

For a simple HTML project, Bootstrap can be loaded through CDN.

Current Bootstrap 5.3.x CDN example from the official documentation:

```html
<link
  href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css"
  rel="stylesheet"
  integrity="sha384-sRIl4kxILFvY47J16cr9ZwB07vP4J8+LH7qKQnuqkuIAvNWLzeN8tE5YBujZqJLB"
  crossorigin="anonymous"
>
```

Bootstrap JavaScript bundle:

```html
<script
  src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js"
  integrity="sha384-FKyoEForCGlyvwx9Hj09JcYn3nv7wiPVlz7YYjR5WcKX/BmnVDxM+D2scQbITxI"
  crossorigin="anonymous"
></script>
```

> Always verify the exact CDN URL and integrity hash against the official Bootstrap documentation when changing Bootstrap versions.

## 4.2 Bundle vs Separate JavaScript

The Bootstrap bundle contains Popper.

Use:

```html
<script src=".../bootstrap.bundle.min.js"></script>
```

when the application uses components such as:

- Dropdown
- Tooltip
- Popover

If the project intentionally avoids features requiring Popper, Bootstrap JS can also be loaded separately.

---

# 5. Bootstrap Architecture

Bootstrap can be thought of as four major UI layers:

```text
Bootstrap
├── Layout
│   ├── Containers
│   ├── Grid
│   ├── Columns
│   ├── Gutters
│   ├── Flex utilities
│   └── CSS Grid
│
├── Content
│   ├── Typography
│   ├── Images
│   ├── Tables
│   └── Figures
│
├── Components
│   ├── Navbar
│   ├── Buttons
│   ├── Cards
│   ├── Modal
│   ├── Dropdown
│   ├── Tabs
│   ├── Accordion
│   └── etc.
│
└── Utilities
    ├── Spacing
    ├── Display
    ├── Flex
    ├── Sizing
    ├── Colors
    ├── Borders
    ├── Position
    ├── Shadows
    └── responsive helpers
```

---

# 6. Layout Reference

## 6.1 Container

Use a container as the main horizontal layout boundary.

```html
<div class="container">
  <!-- content -->
</div>
```

For a full-width container:

```html
<div class="container-fluid">
  <!-- content -->
</div>
```

Responsive container:

```html
<div class="container-md">
  <!-- content -->
</div>
```

Use the container appropriate to the application's desired maximum width.

---

# 7. Grid System

Bootstrap uses a responsive 12-column grid.

Basic structure:

```html
<div class="container">
  <div class="row">
    <div class="col">
      Content A
    </div>

    <div class="col">
      Content B
    </div>
  </div>
</div>
```

Explicit widths:

```html
<div class="row">
  <div class="col-12 col-md-8">
    Main content
  </div>

  <div class="col-12 col-md-4">
    Sidebar
  </div>
</div>
```

### Agent Rule

For responsive two-column layouts, prefer:

```html
<div class="col-12 col-md-8">...</div>
<div class="col-12 col-md-4">...</div>
```

rather than writing custom media queries for ordinary grid behavior.

---

# 8. Responsive Design

Bootstrap uses breakpoint-based responsive classes.

Common breakpoint prefixes:

```text
(no prefix)  = extra small / base
sm
md
lg
xl
xxl
```

Example:

```html
<div class="d-block d-md-flex">
```

Meaning:

- Base/mobile: block
- Medium and larger: flex

Another example:

```html
<div class="col-12 col-lg-6">
```

Meaning:

- Small screens: full width
- Large screens and above: half width

### Mobile-First Rule

Write:

```html
class="d-block d-md-flex"
```

instead of thinking in desktop-first terms.

Base styles apply to smaller screens. Larger breakpoint classes progressively enhance the layout.

---

# 9. Spacing Utilities

Bootstrap provides spacing utilities for:

- Margin
- Padding
- Horizontal spacing
- Vertical spacing
- Responsive spacing

Common pattern:

```text
m-*   margin
mt-*  margin-top
mb-*  margin-bottom
ms-*  margin-start
me-*  margin-end
mx-*  margin left/right
my-*  margin top/bottom

p-*   padding
pt-*  padding-top
pb-*  padding-bottom
ps-*  padding-start
pe-*  padding-end
px-*  padding left/right
py-*  padding top/bottom
```

Examples:

```html
<div class="p-4">
```

```html
<div class="mt-3 mb-4">
```

```html
<div class="px-3 py-5">
```

Responsive spacing:

```html
<div class="p-2 p-md-4 p-lg-5">
```

### Agent Rule

Prefer spacing utilities over:

```css
.my-custom-margin {
  margin-top: ...;
  margin-bottom: ...;
}
```

when Bootstrap's spacing scale satisfies the requirement.

---

# 10. Display Utilities

Common classes:

```text
d-none
d-block
d-inline
d-inline-block
d-flex
d-inline-flex
```

Responsive display:

```html
<div class="d-none d-md-block">
```

Mobile hidden, medium+ visible.

Example mobile navigation control:

```html
<button class="d-md-none">
```

---

# 11. Flex Utilities

Useful classes include:

```text
d-flex
flex-row
flex-column
flex-wrap
justify-content-start
justify-content-center
justify-content-between
justify-content-end
align-items-start
align-items-center
align-items-end
gap-*
```

Example:

```html
<div class="d-flex justify-content-between align-items-center gap-3">
  <div>Left</div>
  <div>Right</div>
</div>
```

Responsive direction:

```html
<div class="d-flex flex-column flex-md-row">
```

---

# 12. Typography

Use Bootstrap typography utilities rather than unnecessary custom CSS.

Examples:

```html
<h1 class="display-4">Heading</h1>
<p class="lead">Supporting text</p>
<p class="text-muted">Secondary information</p>
```

Common text utilities include:

```text
text-start
text-center
text-end
text-uppercase
text-lowercase
text-capitalize
fw-bold
fw-semibold
fw-normal
fst-italic
```

Responsive text alignment can be used:

```html
<p class="text-center text-md-start">
```

---

# 13. Colors

Bootstrap provides contextual color utilities and theme colors.

Typical contextual classes:

```text
primary
secondary
success
danger
warning
info
light
dark
```

Examples:

```html
<button class="btn btn-primary">Save</button>
<div class="text-danger">Error</div>
<div class="bg-dark text-white">Dark section</div>
```

Do not assume that arbitrary color names automatically have Bootstrap classes.

---

# 14. Buttons

Base button:

```html
<button type="button" class="btn btn-primary">
  Save
</button>
```

Common variants:

```text
btn-primary
btn-secondary
btn-success
btn-danger
btn-warning
btn-info
btn-light
btn-dark
btn-link
```

Outline variants:

```text
btn-outline-primary
btn-outline-secondary
btn-outline-success
btn-outline-danger
```

Sizes:

```text
btn-sm
btn-lg
```

Example:

```html
<div class="d-flex gap-2">
  <button class="btn btn-primary">Primary action</button>
  <button class="btn btn-outline-secondary">Cancel</button>
</div>
```

---

# 15. Cards

Typical card:

```html
<div class="card">
  <div class="card-body">
    <h5 class="card-title">Title</h5>
    <p class="card-text">
      Description.
    </p>
    <a href="#" class="btn btn-primary">
      View
    </a>
  </div>
</div>
```

Use cards for:

- Dashboard statistics
- Product information
- Blog previews
- User profiles
- Feature blocks
- Content summaries

---

# 16. Navbar

Bootstrap provides a responsive navbar.

Typical structure:

```html
<nav class="navbar navbar-expand-lg bg-body-tertiary">
  <div class="container">
    <a class="navbar-brand" href="#">
      Application
    </a>

    <button
      class="navbar-toggler"
      type="button"
      data-bs-toggle="collapse"
      data-bs-target="#mainNavbar"
      aria-controls="mainNavbar"
      aria-expanded="false"
      aria-label="Toggle navigation"
    >
      <span class="navbar-toggler-icon"></span>
    </button>

    <div class="collapse navbar-collapse" id="mainNavbar">
      <ul class="navbar-nav ms-auto">
        <li class="nav-item">
          <a class="nav-link active" href="#">Home</a>
        </li>
      </ul>
    </div>
  </div>
</nav>
```

### Agent Rule

If a navbar requires mobile collapse behavior, Bootstrap's JavaScript must be available.

---

# 17. Forms

Use Bootstrap form classes.

Example:

```html
<div class="mb-3">
  <label for="email" class="form-label">
    Email
  </label>

  <input
    type="email"
    class="form-control"
    id="email"
    placeholder="name@example.com"
  >
</div>
```

Common form classes:

```text
form-label
form-control
form-select
form-check
form-check-input
form-check-label
input-group
form-text
```

For a select:

```html
<select class="form-select">
  <option selected>Select an option</option>
  <option value="1">Option 1</option>
</select>
```

---

# 18. Alerts

Example:

```html
<div class="alert alert-success" role="alert">
  Operation completed successfully.
</div>
```

Common variants:

```text
alert-primary
alert-secondary
alert-success
alert-danger
alert-warning
alert-info
alert-light
alert-dark
```

---

# 19. Badges

Example:

```html
<span class="badge text-bg-primary">
  New
</span>
```

Useful for:

- Status
- Categories
- Counts
- Labels
- Notifications

---

# 20. Modal

Basic modal architecture:

```html
<button
  type="button"
  class="btn btn-primary"
  data-bs-toggle="modal"
  data-bs-target="#exampleModal"
>
  Open modal
</button>

<div
  class="modal fade"
  id="exampleModal"
  tabindex="-1"
  aria-labelledby="exampleModalLabel"
  aria-hidden="true"
>
  <div class="modal-dialog">
    <div class="modal-content">

      <div class="modal-header">
        <h1 class="modal-title fs-5" id="exampleModalLabel">
          Modal title
        </h1>

        <button
          type="button"
          class="btn-close"
          data-bs-dismiss="modal"
          aria-label="Close"
        ></button>
      </div>

      <div class="modal-body">
        Modal content.
      </div>

      <div class="modal-footer">
        <button
          type="button"
          class="btn btn-secondary"
          data-bs-dismiss="modal"
        >
          Close
        </button>
      </div>

    </div>
  </div>
</div>
```

Modal behavior requires Bootstrap JavaScript.

---

# 21. Dropdowns

Example:

```html
<div class="dropdown">
  <button
    class="btn btn-secondary dropdown-toggle"
    type="button"
    data-bs-toggle="dropdown"
    aria-expanded="false"
  >
    Menu
  </button>

  <ul class="dropdown-menu">
    <li>
      <a class="dropdown-item" href="#">Profile</a>
    </li>
    <li>
      <a class="dropdown-item" href="#">Settings</a>
    </li>
  </ul>
</div>
```

Dropdowns require Bootstrap JavaScript and Popper.

The Bootstrap bundle includes Popper.

---

# 22. Accordion

Use accordion when content needs expandable/collapsible sections.

Common use cases:

- FAQ
- Settings groups
- Documentation
- Help content

Accordion functionality requires Bootstrap JavaScript.

---

# 23. Collapse

Use Collapse for expandable sections.

Common use cases:

- Mobile navbar
- Expandable content
- Sidebar sections
- Filters

Bootstrap's `data-bs-toggle="collapse"` mechanism can be used where appropriate.

---

# 24. Tabs and Navs

Use Bootstrap nav/tab components for switching between related content panes.

Typical concepts:

```text
nav
nav-tabs
nav-pills
nav-link
tab-content
tab-pane
```

Interactive tab behavior requires Bootstrap JavaScript.

---

# 25. Offcanvas

Use Offcanvas for responsive side panels.

Good use cases:

- Mobile navigation
- Filter drawer
- Settings panel
- Secondary sidebar

Offcanvas behavior requires Bootstrap JavaScript.

---

# 26. Toasts

Use Toasts for temporary status messages.

Good use cases:

- Saved successfully
- Background action completed
- Notification
- API operation status

Toast behavior requires Bootstrap JavaScript.

---

# 27. Carousel

Use Carousel for:

- Image slides
- Promotional slides
- Product galleries
- Content slides

Carousel behavior requires Bootstrap JavaScript.

Avoid using a carousel simply because it looks attractive. Use it when the content genuinely benefits from sequential slides.

---

# 28. Tooltips and Popovers

Tooltips:

```html
<button
  type="button"
  class="btn btn-secondary"
  data-bs-toggle="tooltip"
  data-bs-title="Helpful information"
>
  Hover
</button>
```

Popovers provide richer contextual information.

Both require Bootstrap JavaScript, and they depend on Popper.

> When using tooltips/popovers in framework applications, follow the framework's lifecycle rules and initialize Bootstrap's JavaScript behavior appropriately.

---

# 29. Tables

Use:

```html
<table class="table">
```

Useful variants include:

```text
table
table-striped
table-hover
table-bordered
table-borderless
table-sm
```

Responsive table wrapper:

```html
<div class="table-responsive">
  <table class="table">
    ...
  </table>
</div>
```

---

# 30. Images

Common Bootstrap image utility:

```html
<img src="..." class="img-fluid" alt="Description">
```

`img-fluid` should be preferred when an image needs responsive sizing.

Always provide meaningful `alt` text unless the image is purely decorative.

---

# 31. Borders and Rounded Corners

Common utilities:

```text
border
border-0
border-top
border-bottom
border-start
border-end

rounded
rounded-0
rounded-1
rounded-2
rounded-3
rounded-circle
rounded-pill
```

Example:

```html
<div class="border rounded-3 p-4">
  Content
</div>
```

---

# 32. Shadows

Use Bootstrap shadow utilities when appropriate:

```text
shadow-none
shadow-sm
shadow
shadow-lg
```

Example:

```html
<div class="card shadow-sm">
```

Avoid excessive shadows that reduce visual clarity.

---

# 33. Positioning

Bootstrap provides positioning utilities.

Common concepts:

```text
position-static
position-relative
position-absolute
position-fixed
position-sticky
top-0
bottom-0
start-0
end-0
```

Example:

```html
<div class="position-relative">
  <span class="position-absolute top-0 end-0">
    Badge
  </span>
</div>
```

---

# 34. Sizing

Bootstrap provides width and height utilities.

Common examples:

```text
w-25
w-50
w-75
w-100
w-auto

h-25
h-50
h-75
h-100
h-auto
```

Use responsive/layout utilities before introducing custom sizing CSS.

---

# 35. Accessibility

The coding agent should preserve accessibility while using Bootstrap.

Rules:

- Use semantic HTML.
- Use `<button>` for actions.
- Use `<a>` for navigation.
- Provide labels for form inputs.
- Provide `alt` text for meaningful images.
- Preserve keyboard accessibility.
- Use `aria-*` attributes where required by interactive components.
- Do not remove visible focus behavior without providing an accessible alternative.
- Maintain meaningful heading hierarchy.
- Use Bootstrap's documented accessibility patterns for interactive components.

Example:

```html
<button
  type="button"
  class="btn btn-primary"
  aria-label="Close notification"
>
  Close
</button>
```

---

# 36. Bootstrap JavaScript Components

The following Bootstrap components require JavaScript behavior:

- Accordion
- Alerts
- Buttons with interactive states
- Carousel
- Collapse
- Dropdowns
- Modals
- Navbar responsive collapse/offcanvas behavior
- Navs/Tabs
- Offcanvas
- Scrollspy
- Toasts
- Tooltips
- Popovers

Some components, especially Dropdowns, Tooltips, and Popovers, rely on Popper.

### Agent Decision Rule

Before implementing custom JavaScript:

```text
Does Bootstrap already provide this interaction?
        |
       YES
        |
Use Bootstrap's documented component/data API.
        |
       NO
        |
Implement application-specific JavaScript.
```

---

# 37. Bootstrap Data Attributes

Bootstrap interactive components commonly use `data-bs-*` attributes.

Examples:

```html
data-bs-toggle="modal"
data-bs-target="#exampleModal"
```

```html
data-bs-toggle="collapse"
data-bs-target="#content"
```

```html
data-bs-toggle="dropdown"
```

Do not use old Bootstrap 4 attributes such as:

```html
data-toggle
data-target
```

Use Bootstrap 5 syntax:

```html
data-bs-toggle
data-bs-target
```

---

# 38. Base + Modifier Component Pattern

Bootstrap commonly uses a base class plus modifier classes.

Example:

```html
<button class="btn btn-primary">
```

Here:

```text
btn          = base component
btn-primary  = visual variant
```

Another example:

```html
<div class="alert alert-danger">
```

Conceptually:

```text
alert        = base component
alert-danger = variant
```

### Agent Rule

When Bootstrap provides a base + modifier pattern, use it rather than creating duplicate custom component CSS.

---

# 39. Utilities vs Custom CSS

Prefer Bootstrap utility classes for simple one-property or common layout requirements.

Example:

Instead of:

```css
.center-content {
  display: flex;
  justify-content: center;
  align-items: center;
}
```

prefer:

```html
<div class="d-flex justify-content-center align-items-center">
```

Use custom CSS when:

- The design requires a project-specific visual system.
- Bootstrap utilities cannot express the requirement.
- A repeated custom component needs a dedicated abstraction.
- Complex animations or visual effects are required.
- Brand-specific styling is needed.

---

# 40. Avoid CSS Class Explosion

Do not blindly stack many utilities when it makes markup difficult to understand.

Bad:

```html
<div class="d-flex flex-row flex-wrap align-items-center justify-content-between p-1 p-sm-2 p-md-3 p-lg-4 mt-1 mb-2 ms-1 me-2 border rounded shadow-sm">
```

If the same combination is repeated throughout the application, consider a reusable component or a small custom class.

Use Bootstrap utilities for layout, but keep the implementation maintainable.

---

# 41. Framework Integration

Bootstrap can be used with frontend frameworks such as:

- React
- Angular
- Vue
- Next.js
- Vite-based applications
- Plain HTML/JavaScript

### React Rule

Use Bootstrap classes through JSX:

```jsx
<button className="btn btn-primary">
  Save
</button>
```

Do not use HTML's `class` attribute in JSX.

### General Framework Rule

When using a framework:

1. Install/import Bootstrap according to the project's existing build system.
2. Avoid duplicating Bootstrap CSS imports.
3. Respect the framework's component lifecycle.
4. Initialize JavaScript plugins in a framework-safe way.
5. Prefer framework components/abstractions when the project already has them.
6. Do not introduce jQuery for Bootstrap 5.

---

# 42. Package-Based Projects

For Vite, React, Angular, Vue, or similar build-tool projects, prefer the project's package manager/build pipeline when Bootstrap is already configured.

Typical dependency:

```text
bootstrap
```

Do not automatically add CDN links to a package-based application unless the project specifically requires CDN delivery.

---

# 43. Bootstrap Files and Builds

Bootstrap provides compiled CSS and JavaScript builds.

Conceptually:

```text
bootstrap/
├── dist/
│   ├── css/
│   └── js/
├── scss/
├── js/
└── documentation/
```

Relevant compiled CSS options include:

```text
bootstrap.css
bootstrap.min.css
bootstrap.rtl.css
bootstrap.rtl.min.css
bootstrap-grid.css
bootstrap-utilities.css
bootstrap-reboot.css
```

Relevant JavaScript options include:

```text
bootstrap.js
bootstrap.min.js
bootstrap.bundle.js
bootstrap.bundle.min.js
```

The bundle includes Popper.

---

# 44. Customization Strategy

When a project requires branding:

```text
Bootstrap defaults
        ↓
Bootstrap theme/custom variables
        ↓
Bootstrap utilities/components
        ↓
Small project-specific CSS layer
```

Avoid rewriting Bootstrap components from scratch unless there is a strong project requirement.

Bootstrap's Sass-based customization and Utility API can be used for deeper customization.

---

# 45. Utility API

Bootstrap utilities are generated using a Sass-based Utility API.

The Utility API can be used to:

- Modify existing utilities
- Add custom utilities
- Generate responsive utilities
- Customize utility values

Use this approach when the project needs a reusable utility system rather than repeatedly writing custom CSS.

---

# 46. Theme and Color Modes

Bootstrap 5.3 includes modern customization and color-mode capabilities.

When implementing light/dark themes:

1. Check Bootstrap 5.3 color-mode documentation.
2. Prefer Bootstrap's supported color-mode mechanisms.
3. Avoid manually duplicating an entire dark theme unless required.
4. Keep application-specific theme variables separate from framework defaults.
5. Ensure text/background contrast remains accessible.

---

# 47. Z-Index and Layering

Bootstrap components follow a common z-index strategy.

When implementing:

- Navbar overlays
- Dropdowns
- Modals
- Tooltips
- Offcanvas panels
- Sticky elements

prefer Bootstrap's documented layering approach.

Do not randomly assign extremely large values such as:

```css
z-index: 999999;
```

unless there is a documented application-specific reason.

---

# 48. Recommended Page Construction Pattern

For a normal responsive application page:

```text
HTML document
└── Navbar
    └── Container
        └── Main
            └── Container
                ├── Page heading
                ├── Row
                │   ├── Main column
                │   └── Sidebar column
                └── Footer/content
```

Example:

```html
<nav class="navbar navbar-expand-lg bg-body-tertiary">
  ...
</nav>

<main>
  <div class="container py-4">

    <div class="mb-4">
      <h1 class="display-6">Dashboard</h1>
      <p class="text-body-secondary">
        Overview of your application.
      </p>
    </div>

    <div class="row g-4">
      <section class="col-12 col-lg-8">
        <!-- main content -->
      </section>

      <aside class="col-12 col-lg-4">
        <!-- sidebar -->
      </aside>
    </div>

  </div>
</main>
```

---

# 49. Dashboard Pattern

Recommended Bootstrap structure:

```html
<div class="container-fluid py-4">

  <div class="row g-4">

    <div class="col-12 col-sm-6 col-xl-3">
      <div class="card h-100 shadow-sm">
        <div class="card-body">
          <p class="text-body-secondary mb-1">
            Revenue
          </p>
          <h2 class="mb-0">$12,450</h2>
        </div>
      </div>
    </div>

  </div>

</div>
```

Useful dashboard classes:

```text
container-fluid
row
g-*
col-12
col-sm-6
col-lg-*
col-xl-*
card
h-100
shadow-sm
```

---

# 50. Responsive Sidebar Pattern

Desktop sidebar + mobile collapse/offcanvas can be implemented using Bootstrap responsive utilities and Offcanvas.

Conceptual structure:

```text
Mobile
└── Toggle button
    └── Offcanvas sidebar

Desktop
└── Sidebar visible
    └── Main content
```

Do not create separate duplicated desktop/mobile sidebar markup unless necessary.

---

# 51. Loading States

Bootstrap provides spinners.

Example:

```html
<div
  class="spinner-border"
  role="status"
  aria-label="Loading"
></div>
```

Use loading indicators for:

- API requests
- Form submission
- Page transitions
- Async content

Avoid indefinite spinners when the application can provide meaningful progress/status information.

---

# 52. Empty States

Bootstrap does not require a dedicated empty-state component.

Build empty states from:

```text
container/card
+
text utilities
+
spacing utilities
+
button
```

Example:

```html
<div class="text-center py-5">
  <h2 class="h5">No projects yet</h2>
  <p class="text-body-secondary">
    Create your first project to get started.
  </p>
  <button class="btn btn-primary">
    Create project
  </button>
</div>
```

---

# 53. Error States

For validation or operation errors, use:

```text
alert-danger
text-danger
is-invalid
invalid-feedback
```

Example:

```html
<input
  class="form-control is-invalid"
  type="email"
  aria-describedby="emailError"
>

<div id="emailError" class="invalid-feedback">
  Please enter a valid email address.
</div>
```

---

# 54. Success States

Use:

```text
alert-success
text-success
is-valid
valid-feedback
```

Example:

```html
<div class="alert alert-success" role="alert">
  Changes saved successfully.
</div>
```

---

# 55. Common Bootstrap 4 → 5 Migration Warning

Do not generate old Bootstrap 4 syntax.

Examples of Bootstrap 5 style:

```text
ms-*  instead of ml-*
me-*  instead of mr-*
ps-*  instead of pl-*
pe-*  instead of pr-*
data-bs-* instead of data-*
```

Bootstrap 5 uses logical start/end naming to better support RTL layouts.

---

# 56. RTL Considerations

Bootstrap 5.3 provides RTL builds.

When the project requires RTL:

- Prefer logical utilities such as `ms-*` and `me-*`.
- Avoid hardcoded `margin-left`/`margin-right` when a Bootstrap logical utility is sufficient.
- Use Bootstrap's RTL build/configuration according to project requirements.

---

# 57. Agent Component Selection Guide

When asked to build a UI element, use this mapping:

| Requirement | Bootstrap feature |
|---|---|
| Main page width | `container`, `container-fluid` |
| Responsive columns | Grid / `row` / `col-*` |
| Spacing | Margin/padding utilities |
| Horizontal alignment | Flex utilities |
| Button | `btn` + modifier |
| Card | `card` |
| Navigation | Navbar / Nav |
| Form input | `form-control` |
| Select | `form-select` |
| Checkbox | `form-check` |
| Status message | Alert |
| Label/status | Badge |
| Popup dialog | Modal |
| Dropdown menu | Dropdown |
| Expandable content | Collapse |
| FAQ | Accordion |
| Tabs | Nav + Tabs |
| Side drawer | Offcanvas |
| Temporary notification | Toast |
| Image slider | Carousel |
| Loading | Spinner |
| Responsive table | `table-responsive` |
| Image responsiveness | `img-fluid` |

---

# 58. Coding-Agent Decision Process

Before writing UI code:

```text
1. Inspect existing project structure.
        ↓
2. Detect whether Bootstrap 5.3 is already installed/imported.
        ↓
3. Identify the required UI pattern.
        ↓
4. Check whether Bootstrap has a matching component/utility.
        ↓
5. Use Bootstrap classes/components where appropriate.
        ↓
6. Add custom CSS only for project-specific requirements.
        ↓
7. Verify responsive behavior.
        ↓
8. Verify accessibility.
        ↓
9. Verify Bootstrap JS is available for interactive components.
        ↓
10. Check that Bootstrap 4 syntax has not been introduced.
```

---

# 59. Rules for AI-Generated Code

The coding agent MUST:

- Use Bootstrap 5.3 syntax.
- Prefer official Bootstrap classes.
- Avoid fabricated classes.
- Avoid deprecated/Bootstrap 4 classes.
- Avoid unnecessary custom CSS.
- Avoid jQuery.
- Use `data-bs-*` attributes for Bootstrap 5 interactions.
- Include accessible labels/ARIA attributes where needed.
- Make layouts mobile-first.
- Test or reason through mobile, tablet, and desktop layouts.
- Keep responsive breakpoints consistent.
- Use semantic HTML.
- Keep component markup readable.
- Reuse existing project components where available.
- Avoid duplicating Bootstrap imports.
- Avoid adding CDN dependencies to package-managed applications unless requested.
- Verify exact Bootstrap API details against official documentation when uncertain.

---

# 60. Do Not Assume

The coding agent should **not assume**:

- Every Bootstrap component requires JavaScript.
- Every Bootstrap class exists for every CSS property.
- Bootstrap automatically provides icons.
- Bootstrap 4 syntax works in Bootstrap 5.
- A CDN is appropriate for every project.
- Popper is separately required when `bootstrap.bundle` is already used.
- Custom CSS is always better than utilities.
- Utilities should be stacked without considering maintainability.

---

# 61. Icons

Bootstrap itself does not mean that every icon is automatically available.

If the project needs icons:

1. Check whether an icon library is already installed.
2. Reuse the project's existing icon system.
3. If Bootstrap Icons are explicitly selected, configure Bootstrap Icons separately.
4. Do not invent icon class names.

---

# 62. Performance Guidance

Prefer:

- Minified production CSS/JS for production.
- `bootstrap.bundle.min.js` when required.
- Only the Bootstrap functionality the project actually needs when custom builds are being used.
- Utility classes instead of large amounts of repeated custom CSS.
- Optimized images.
- Lazy loading for appropriate non-critical images.

Avoid:

- Multiple copies of Bootstrap.
- Multiple conflicting CSS frameworks.
- Loading both CDN Bootstrap and package Bootstrap.
- Custom CSS that duplicates Bootstrap utilities.
- Unnecessary JavaScript plugins.

---

# 63. Debugging Checklist

If Bootstrap styles are not working:

```text
[ ] Is Bootstrap CSS actually imported?
[ ] Is the correct Bootstrap 5.3 version being used?
[ ] Is the class name spelled correctly?
[ ] Is another CSS rule overriding it?
[ ] Is the DOM structure valid?
[ ] Is the responsive breakpoint appropriate?
```

If an interactive component is not working:

```text
[ ] Is Bootstrap JS loaded?
[ ] Is the correct Bootstrap 5 data attribute used?
[ ] Is `data-bs-*` being used instead of `data-*`?
[ ] Does the component require Popper?
[ ] If yes, is bootstrap.bundle being used?
[ ] In a framework, is initialization happening at the correct lifecycle stage?
```

---

# 64. Official Documentation Map

Use these official Bootstrap 5.3 documentation sections when more detail is required:

- Introduction:
  https://getbootstrap.com/docs/5.3/getting-started/introduction/

- Contents:
  https://getbootstrap.com/docs/5.3/getting-started/contents/

- Breakpoints:
  https://getbootstrap.com/docs/5.3/layout/breakpoints/

- Containers:
  https://getbootstrap.com/docs/5.3/layout/containers/

- Grid:
  https://getbootstrap.com/docs/5.3/layout/grid/

- Utilities:
  https://getbootstrap.com/docs/5.3/utilities/

- Components:
  https://getbootstrap.com/docs/5.3/components/

- Forms:
  https://getbootstrap.com/docs/5.3/forms/overview/

- JavaScript:
  https://getbootstrap.com/docs/5.3/getting-started/javascript/

- Customize:
  https://getbootstrap.com/docs/5.3/customize/overview/

- Color modes:
  https://getbootstrap.com/docs/5.3/customize/color-modes/

- Utility API:
  https://getbootstrap.com/docs/5.3/utilities/api/

- Approach:
  https://getbootstrap.com/docs/5.3/extend/approach/

---

# 65. Source-Derived Key Facts

The official Bootstrap 5.3 introduction establishes that:

- Bootstrap is a frontend toolkit.
- Bootstrap supports production-ready CSS and JavaScript through CDN.
- The responsive viewport meta tag is required for proper mobile behavior.
- Bootstrap uses a mobile-first approach.
- The Bootstrap JS bundle includes Popper.
- Dropdowns, popovers, and tooltips require Popper.
- Bootstrap includes many JavaScript-powered components.
- Bootstrap uses the HTML5 doctype.
- Bootstrap applies a global `border-box` box-sizing approach.
- Bootstrap Reboot provides cross-browser normalization.

Bootstrap's broader documentation also emphasizes:

- Responsive, mobile-first components.
- Base + modifier component classes.
- Utility classes for common styling.
- A common z-index strategy.
- Prefer HTML/CSS over JavaScript when possible.
- Use utilities where appropriate.
- Bootstrap's Utility API can generate and customize utility classes.

---

# 66. Final Agent Instruction

When this file is supplied as project context, treat Bootstrap 5.3 as the project's frontend UI foundation.

**Priority order:**

```text
Existing project architecture
        ↓
Existing reusable project components
        ↓
Bootstrap 5.3 components/utilities
        ↓
Small project-specific CSS
        ↓
Custom JavaScript only when necessary
```

The agent should always preserve the existing project's architecture and avoid introducing unnecessary dependencies.

For uncertain or version-sensitive Bootstrap behavior, consult the official Bootstrap 5.3 documentation before implementing.

**Reference source:** https://getbootstrap.com/docs/5.3/

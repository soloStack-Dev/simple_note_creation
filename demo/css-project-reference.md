# CSS Project Reference for Coding Agents

> **Purpose:** A project-oriented CSS reference for an AI coding agent. Use this file when designing, implementing, reviewing, debugging, or refactoring the styling of a web application.
>
> **Primary source:** MDN Web Docs — CSS  
> https://developer.mozilla.org/en-US/docs/Web/CSS
>
> **Reference scope:** Modern CSS concepts documented by MDN, organized around practical application development rather than reproducing the entire MDN reference.
>
> **Important:** CSS is modular rather than a single versioned "CSS3/CSS4" specification. Individual CSS modules progress independently. Verify browser support for advanced features before relying on them in production.

---

# 1. CSS Mental Model

CSS describes how HTML/XML documents are presented.

Think of the styling pipeline as:

```text
HTML structure
     |
     v
CSS selectors
     |
     v
Declarations
(property: value)
     |
     v
Cascade + inheritance + specificity
     |
     v
Computed styles
     |
     v
Layout
     |
     v
Painting / compositing
```

A basic CSS rule:

```css
.card {
  padding: 1rem;
  border-radius: 0.75rem;
}
```

Structure:

```text
.card       -> selector
padding     -> property
1rem        -> value
```

---

# 2. Core Development Principles

When generating CSS for an application:

1. Prefer semantic class names.
2. Keep selectors predictable.
3. Avoid unnecessary specificity.
4. Prefer reusable design tokens.
5. Prefer layout systems over manual positioning.
6. Use Flexbox for one-dimensional layouts.
7. Use Grid for two-dimensional layouts.
8. Use normal document flow whenever possible.
9. Use responsive design rather than fixed desktop dimensions.
10. Prefer logical properties for reusable/internationalized layouts.
11. Use accessible focus states.
12. Respect reduced-motion preferences.
13. Avoid excessive `!important`.
14. Avoid deeply nested selectors.
15. Keep component styles isolated where the framework supports it.
16. Verify advanced features against target browser requirements.
17. Prefer CSS over JavaScript for visual state whenever CSS can express it.

---

# 3. CSS Syntax

Basic:

```css
selector {
  property: value;
}
```

Multiple declarations:

```css
.button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.75rem 1rem;
  border-radius: 0.5rem;
}
```

Comments:

```css
/* Component styles */
```

Multiple selectors:

```css
h1,
h2,
h3 {
  line-height: 1.2;
}
```

---

# 4. Selectors

Selectors determine which elements receive CSS rules.

## Universal

```css
* {
  box-sizing: border-box;
}
```

## Type

```css
button {
  cursor: pointer;
}
```

## Class

```css
.card {
  padding: 1rem;
}
```

## ID

```css
#app {
  min-height: 100vh;
}
```

Prefer classes for reusable styling. IDs are generally better for unique structural references than for reusable component styling.

## Attribute

```css
input[type="email"] {
  width: 100%;
}
```

## Descendant

```css
.card p {
  margin: 0;
}
```

## Child

```css
.card > p {
  margin: 0;
}
```

## Adjacent sibling

```css
.label + input {
  margin-top: 0.5rem;
}
```

## General sibling

```css
h2 ~ p {
  color: inherit;
}
```

---

# 5. Pseudo-Classes

Pseudo-classes represent element states or relationships.

Common examples:

```css
button:hover {
  /* hover */
}

button:focus-visible {
  /* keyboard-visible focus */
}

input:disabled {
  /* disabled */
}

input:checked {
  /* checked */
}

input:invalid {
  /* invalid */
}
```

Useful structural selectors:

```css
:first-child
:last-child
:nth-child()
:nth-of-type()
:not()
:is()
:where()
:has()
```

Example:

```css
.card:not(.featured) {
  border: 1px solid var(--border);
}
```

Relational selector:

```css
.form-group:has(input:invalid) {
  border-color: var(--danger);
}
```

Use `:has()` when it makes the component logic substantially clearer, and verify browser support if legacy browsers matter.

---

# 6. Pseudo-Elements

Pseudo-elements style a specific generated/virtual part of an element.

Common:

```css
::before
::after
::first-letter
::first-line
::selection
::placeholder
::marker
```

Example:

```css
.badge::before {
  content: "";
  width: 0.5rem;
  height: 0.5rem;
  border-radius: 50%;
}
```

Do not use pseudo-elements for essential semantic content.

---

# 7. CSS Nesting

Modern CSS supports nesting:

```css
.card {
  padding: 1rem;

  & .title {
    font-size: 1.25rem;
  }

  &:hover {
    transform: translateY(-2px);
  }
}
```

Use nesting carefully.

Recommended:

```text
Component
  -> small number of related states
```

Avoid:

```text
Component
  -> many levels
      -> many descendants
          -> many states
```

Deep nesting increases maintenance and specificity complexity.

---

# 8. Cascade

The cascade decides which CSS declaration wins when multiple declarations apply.

Conceptually, CSS considers:

```text
Relevance
   ↓
Origin + importance
   ↓
Specificity
   ↓
Scoping proximity
   ↓
Order of appearance
```

The cascade is fundamental to CSS.

Example:

```css
.button {
  color: blue;
}

.button.primary {
  color: red;
}
```

The second selector has greater specificity.

### Agent rule

Before adding another selector to "fix" a style, determine why the existing rule wins or loses.

---

# 9. Specificity

Specificity can be thought of as:

```text
inline styles
    >
IDs
    >
classes / attributes / pseudo-classes
    >
elements / pseudo-elements
```

Example:

```css
p {
  color: black;
}

.card p {
  color: blue;
}

#main .card p {
  color: red;
}
```

Avoid specificity escalation.

Bad pattern:

```css
.page .content .card .body p span {
  ...
}
```

Prefer:

```css
.card-description {
  ...
}
```

---

# 10. `!important`

Avoid unless there is a specific reason.

```css
.button {
  color: red !important;
}
```

Using `!important` repeatedly usually indicates a cascade/specificity architecture problem.

Prefer:

```text
correct layer
correct selector
correct component boundary
design tokens
```

---

# 11. Cascade Layers

Use layers when a project has multiple style sources.

Example:

```css
@layer reset, base, components, utilities;

@layer reset {
  * {
    box-sizing: border-box;
  }
}

@layer base {
  body {
    margin: 0;
  }
}

@layer components {
  .button {
    ...
  }
}
```

Useful for:

- reset styles
- third-party libraries
- framework styles
- application components
- utility styles

Example structure:

```text
reset
base
theme
components
utilities
overrides
```

Use a deliberate layer architecture instead of random stylesheet ordering.

---

# 12. Inheritance

Some properties inherit from parents.

Example:

```css
body {
  color: #222;
  font-family: system-ui, sans-serif;
}
```

Child elements can inherit these values.

Useful keywords:

```css
inherit
initial
unset
revert
revert-layer
```

Example:

```css
button {
  font: inherit;
  color: inherit;
}
```

This is often useful for controls that should match surrounding typography.

---

# 13. CSS Custom Properties

Use custom properties for design tokens.

```css
:root {
  --color-primary: #2563eb;
  --color-surface: #ffffff;
  --color-text: #111827;
  --radius-md: 0.75rem;
  --space-md: 1rem;
}
```

Use:

```css
.button {
  background: var(--color-primary);
  border-radius: var(--radius-md);
}
```

Fallback:

```css
color: var(--color-text, #111);
```

Component-local variable:

```css
.card {
  --card-padding: 1rem;
  padding: var(--card-padding);
}
```

### Agent rule

Prefer custom properties for values reused across components.

---

# 14. `@property`

For advanced custom properties, CSS supports registering custom properties.

Example:

```css
@property --progress {
  syntax: "<percentage>";
  inherits: false;
  initial-value: 0%;
}
```

This can be useful for animation and type-aware custom properties.

Use only when the project benefits from the additional complexity.

---

# 15. Box Model

Every element is represented through the box model:

```text
content
  ↓
padding
  ↓
border
  ↓
margin
```

Example:

```css
.card {
  width: 300px;
  padding: 20px;
  border: 1px solid;
  margin: 20px;
}
```

---

# 16. `box-sizing`

Recommended application default:

```css
*,
*::before,
*::after {
  box-sizing: border-box;
}
```

Then:

```css
.card {
  width: 100%;
  padding: 1rem;
}
```

With `border-box`, declared width includes content + padding + border.

---

# 17. Margin Collapse

Vertical margins can collapse in normal block layout.

Example:

```css
.title {
  margin-bottom: 1rem;
}

.description {
  margin-top: 1rem;
}
```

The resulting vertical separation is not necessarily the sum of both margins.

When consistent component spacing is needed, prefer parent layout mechanisms such as:

```css
.stack {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
```

This avoids many margin-collapse surprises.

---

# 18. Display

Common values:

```css
display: block;
display: inline;
display: inline-block;
display: flex;
display: grid;
display: none;
```

Modern layout:

```css
display: flex;
display: grid;
```

Use `display: none` when content should be removed from layout and generally from the accessibility tree.

For visually hiding content while retaining it for assistive technology, use a dedicated accessible-visually-hidden pattern instead.

---

# 19. Normal Flow

Prefer normal document flow whenever possible.

Example:

```css
.page {
  max-width: 1200px;
  margin-inline: auto;
  padding-inline: 1rem;
}
```

Avoid using absolute positioning to construct an entire page.

Use positioning for elements that actually need positioning:

- overlays
- badges
- popovers
- floating controls
- decorative elements

---

# 20. Width and Height

Prefer flexible constraints:

```css
.container {
  width: min(100% - 2rem, 1200px);
  margin-inline: auto;
}
```

Useful properties:

```css
width
min-width
max-width
height
min-height
max-height
inline-size
min-inline-size
max-inline-size
block-size
min-block-size
max-block-size
```

Logical dimensions are often better for internationalized applications.

---

# 21. Logical Properties

Prefer:

```css
margin-inline
margin-block
padding-inline
padding-block
inset-inline
inset-block
border-inline
border-block
```

instead of always using physical directions:

```css
margin-left
margin-right
```

Example:

```css
.card {
  padding-inline: 1rem;
  padding-block: 1.5rem;
}
```

This works better with writing modes and RTL layouts.

---

# 22. Flexbox

Flexbox is primarily a one-dimensional layout system.

Use it for:

```text
navigation bars
button groups
horizontal cards
vertical stacks
centering
toolbars
form rows
```

Example:

```css
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}
```

---

# 23. Flex Direction

```css
display: flex;
flex-direction: row;
```

or:

```css
flex-direction: column;
```

Other:

```css
row-reverse
column-reverse
```

Prefer normal direction unless the visual order requirement is intentional.

---

# 24. Flex Alignment

Main-axis:

```css
justify-content:
  flex-start
  center
  flex-end
  space-between
  space-around
  space-evenly;
```

Cross-axis:

```css
align-items:
  stretch
  flex-start
  center
  flex-end
  baseline;
```

Example:

```css
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
```

---

# 25. Flex Wrapping

```css
.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
}
```

Useful for responsive toolbars.

---

# 26. Flex Item Sizing

```css
.item {
  flex: 1;
}
```

More explicit:

```css
.item {
  flex-grow: 1;
  flex-shrink: 1;
  flex-basis: 0;
}
```

Prevent shrinking:

```css
.icon {
  flex: 0 0 auto;
}
```

---

# 27. Flex Common Pitfall

Long content can prevent expected shrinking.

Use:

```css
.item {
  min-width: 0;
}
```

especially for flex children containing:

- long text
- code
- URLs
- truncation
- nested layouts

---

# 28. CSS Grid

Grid is primarily a two-dimensional layout system.

Use it for:

```text
dashboard layouts
card grids
page sections
complex forms
sidebar + content
tablesque layouts
```

Example:

```css
.dashboard {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: 1.5rem;
}
```

---

# 29. Responsive Grid

A powerful card layout:

```css
.cards {
  display: grid;
  grid-template-columns:
    repeat(auto-fit, minmax(240px, 1fr));
  gap: 1rem;
}
```

This can reduce the need for many media queries.

---

# 30. Grid Areas

Example:

```css
.page {
  display: grid;
  grid-template-areas:
    "header header"
    "sidebar main"
    "footer footer";

  grid-template-columns: 240px 1fr;
}

.header {
  grid-area: header;
}

.sidebar {
  grid-area: sidebar;
}

.main {
  grid-area: main;
}

.footer {
  grid-area: footer;
}
```

Responsive variation:

```css
@media (max-width: 768px) {
  .page {
    grid-template-areas:
      "header"
      "main"
      "footer";

    grid-template-columns: 1fr;
  }

  .sidebar {
    display: none;
  }
}
```

---

# 31. `gap`

Prefer `gap` for spacing between layout children:

```css
.container {
  display: flex;
  gap: 1rem;
}
```

or:

```css
.grid {
  display: grid;
  gap: 1.5rem;
}
```

This is usually cleaner than adding margins to every child.

---

# 32. Positioning

Values:

```css
position: static;
position: relative;
position: absolute;
position: fixed;
position: sticky;
```

## Relative

Creates a positioning context:

```css
.card {
  position: relative;
}
```

## Absolute

Position relative to an appropriate containing block:

```css
.badge {
  position: absolute;
  inset-block-start: 0.5rem;
  inset-inline-end: 0.5rem;
}
```

## Fixed

Relative to viewport:

```css
.floating-action {
  position: fixed;
  inset-inline-end: 1rem;
  inset-block-end: 1rem;
}
```

## Sticky

```css
.header {
  position: sticky;
  top: 0;
}
```

Use positioning intentionally. Do not use it as the primary page layout mechanism.

---

# 33. Stacking and `z-index`

`z-index` controls stacking order within relevant stacking contexts.

Example:

```css
.modal {
  position: fixed;
  z-index: 1000;
}
```

Do not create arbitrary values such as:

```text
999999
9999999
99999999
```

Instead define project layers:

```css
:root {
  --z-dropdown: 100;
  --z-sticky: 200;
  --z-modal: 1000;
  --z-toast: 1100;
}
```

---

# 34. Overflow

Useful:

```css
overflow: visible;
overflow: hidden;
overflow: auto;
overflow: scroll;
```

Modern logical:

```css
overflow-inline
overflow-block
```

For horizontal code:

```css
.code {
  overflow-x: auto;
}
```

Avoid accidentally hiding content that users need to access.

---

# 35. Text Overflow

Single-line truncation:

```css
.title {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
```

For multi-line truncation, use modern line-clamping approaches appropriate for the project's browser targets.

---

# 36. Responsive Design

Responsive design should adapt to:

```text
viewport width
viewport height
input capabilities
display characteristics
user preferences
component/container size
```

Do not design only for one desktop resolution.

---

# 37. Media Queries

Example:

```css
@media (max-width: 768px) {
  .navigation {
    display: none;
  }
}
```

Prefer content-driven breakpoints rather than blindly using device names.

Example:

```text
mobile
tablet
desktop
```

is less useful than:

```text
breakpoint where this layout stops working
```

---

# 38. Container Queries

Container queries allow a component to respond to its container instead of the viewport.

Example:

```css
.card-container {
  container-type: inline-size;
}

@container (min-width: 500px) {
  .card {
    display: grid;
    grid-template-columns: 1fr 1fr;
  }
}
```

Use container queries for reusable components that may appear in different page layouts.

---

# 39. User Preference Media Queries

Reduced motion:

```css
@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms;
    animation-iteration-count: 1;
    transition-duration: 0.01ms;
    scroll-behavior: auto;
  }
}
```

Dark mode:

```css
@media (prefers-color-scheme: dark) {
  :root {
    --color-background: #111;
    --color-text: #fff;
  }
}
```

Other useful media features may include:

```text
prefers-contrast
forced-colors
hover
pointer
orientation
print
```

---

# 40. Colors

CSS supports multiple color representations:

```css
color: red;
color: #2563eb;
color: rgb(37 99 235);
color: hsl(217 91% 60%);
```

Modern relative/transformation tools include:

```css
color-mix()
```

Example:

```css
background: color-mix(
  in srgb,
  var(--primary),
  white 20%
);
```

---

# 41. Color Tokens

Prefer centralized colors:

```css
:root {
  --color-primary: #2563eb;
  --color-primary-hover: #1d4ed8;
  --color-background: #ffffff;
  --color-surface: #f8fafc;
  --color-text: #0f172a;
  --color-muted: #64748b;
  --color-border: #e2e8f0;
  --color-success: #16a34a;
  --color-warning: #d97706;
  --color-danger: #dc2626;
}
```

Use tokens rather than scattering literal colors throughout components.

---

# 42. Gradients

Linear:

```css
background:
  linear-gradient(
    135deg,
    #7c3aed,
    #ec4899
  );
```

Radial:

```css
background:
  radial-gradient(
    circle at top,
    rgba(124, 58, 237, 0.25),
    transparent 60%
  );
```

Use gradients for visual hierarchy, not as a replacement for accessible text contrast.

---

# 43. Typography

Common properties:

```css
font-family
font-size
font-weight
font-style
line-height
letter-spacing
text-align
text-transform
text-decoration
text-overflow
white-space
word-break
overflow-wrap
```

Base:

```css
body {
  font-family:
    system-ui,
    -apple-system,
    BlinkMacSystemFont,
    "Segoe UI",
    sans-serif;

  font-size: 1rem;
  line-height: 1.5;
}
```

---

# 44. Responsive Typography

Prefer fluid values where appropriate:

```css
h1 {
  font-size: clamp(2rem, 5vw, 4rem);
}
```

Pattern:

```text
minimum
preferred fluid size
maximum
```

Avoid unnecessarily large text that damages layout on small screens.

---

# 45. `clamp()`

Useful for responsive dimensions:

```css
padding-inline: clamp(1rem, 4vw, 4rem);
```

Example:

```css
.hero-title {
  font-size: clamp(2rem, 6vw, 5rem);
}
```

---

# 46. CSS Math Functions

Common:

```css
calc()
min()
max()
clamp()
```

Example:

```css
.sidebar {
  width: min(320px, 30vw);
}
```

Example:

```css
.content {
  width: calc(100% - 2rem);
}
```

Use math functions to create fluid layouts instead of many hard-coded breakpoints.

---

# 47. Units

Common:

```text
px
%
rem
em
vw
vh
dvw
dvh
svh
lvh
ch
ex
fr
```

Guidelines:

- `rem` → scalable typography/spacing
- `%` → relative dimensions
- `fr` → Grid tracks
- `vw/vh` → viewport-relative design
- `dvh` → dynamic viewport height
- `svh/lvh` → small/large viewport variants
- `ch` → text-width constraints

Example:

```css
.page {
  min-height: 100dvh;
}
```

---

# 48. `rem` vs `em`

Use `rem` when a value should relate to the root font size:

```css
padding: 1rem;
```

Use `em` when the value should scale with the current element's font size:

```css
.icon-button {
  padding: 0.5em;
}
```

---

# 49. `aspect-ratio`

Useful for cards/media:

```css
.thumbnail {
  aspect-ratio: 16 / 9;
  overflow: hidden;
}
```

Image:

```css
.thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
```

---

# 50. Images

Common:

```css
img {
  max-width: 100%;
  height: auto;
}
```

Cover:

```css
img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
```

Contain:

```css
img {
  object-fit: contain;
}
```

Use `object-position` when focal positioning matters.

---

# 51. Background Images

```css
.hero {
  background-image: url("/images/hero.webp");
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}
```

Avoid using CSS background images for content that should be semantically represented as an image.

---

# 52. Borders and Radius

```css
.card {
  border: 1px solid var(--color-border);
  border-radius: 0.75rem;
}
```

Use design tokens:

```css
:root {
  --radius-sm: 0.375rem;
  --radius-md: 0.75rem;
  --radius-lg: 1rem;
  --radius-xl: 1.5rem;
}
```

---

# 53. Shadows

```css
.card {
  box-shadow:
    0 10px 30px rgb(0 0 0 / 0.08);
}
```

Keep shadows consistent through tokens:

```css
:root {
  --shadow-sm: 0 1px 3px rgb(0 0 0 / 0.08);
  --shadow-md: 0 10px 30px rgb(0 0 0 / 0.10);
}
```

---

# 54. Transforms

Common:

```css
transform: translateY(-4px);
transform: scale(1.02);
transform: rotate(2deg);
```

Prefer transform for visual movement/animation instead of repeatedly changing layout properties.

---

# 55. Transitions

Example:

```css
.button {
  transition:
    background-color 180ms ease,
    transform 180ms ease;
}

.button:hover {
  transform: translateY(-1px);
}
```

Prefer animating properties that do not cause unnecessary layout work.

---

# 56. Animations

Define keyframes:

```css
@keyframes fade-in {
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
  }
}
```

Apply:

```css
.dialog {
  animation: fade-in 200ms ease-out;
}
```

---

# 57. Animation Performance

Prefer:

```text
transform
opacity
```

for frequently animated UI.

Be cautious when animating:

```text
width
height
top
left
margin
```

because they may cause layout recalculation.

---

# 58. Reduced Motion

Always consider users who request reduced motion.

Example:

```css
@media (prefers-reduced-motion: reduce) {
  .animated-element {
    animation: none;
    transition: none;
  }
}
```

Do not remove important state information merely because motion is disabled.

---

# 59. Scroll Behavior

Smooth scrolling:

```css
html {
  scroll-behavior: smooth;
}
```

Respect reduced motion:

```css
@media (prefers-reduced-motion: reduce) {
  html {
    scroll-behavior: auto;
  }
}
```

---

# 60. Scroll Snap

Example:

```css
.carousel {
  display: flex;
  overflow-x: auto;
  scroll-snap-type: x mandatory;
}

.carousel-item {
  scroll-snap-align: start;
}
```

Use for appropriate touch/scroll experiences.

---

# 61. Forms

Base:

```css
input,
textarea,
select,
button {
  font: inherit;
}
```

Input:

```css
.input {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1px solid var(--color-border);
  border-radius: 0.5rem;
}
```

Focus:

```css
.input:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}
```

Do not remove focus indicators without providing an equally visible replacement.

---

# 62. Buttons

Example:

```css
.button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;

  min-height: 2.75rem;
  padding-inline: 1rem;

  border: 0;
  border-radius: 0.5rem;

  font: inherit;
  font-weight: 600;
  cursor: pointer;
}
```

Disabled:

```css
.button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}
```

---

# 63. Accessibility

CSS must not destroy accessibility.

Important rules:

- Keep visible focus indicators.
- Maintain sufficient color contrast.
- Do not communicate meaning by color alone.
- Avoid hiding content required by assistive technologies.
- Respect user zoom.
- Support keyboard navigation.
- Avoid excessive motion.
- Preserve readable line lengths.
- Do not use CSS pseudo-elements as the only source of important information.

---

# 64. Visually Hidden Content

A common pattern:

```css
.visually-hidden {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}
```

Use for content that should remain available to assistive technology while not being visually displayed.

Do not use this to hide content that sighted users genuinely need.

---

# 65. `content-visibility`

For suitable large pages:

```css
.section {
  content-visibility: auto;
}
```

This can allow the browser to skip work for off-screen content.

Measure before applying broadly because behavior depends on page structure and browser support.

---

# 66. Containment

CSS containment can limit layout/paint/style effects:

```css
.widget {
  contain: layout paint;
}
```

Use when there is a real performance/isolation reason.

Do not add containment randomly because it can change layout/positioning behavior.

---

# 67. Anchor Positioning

Modern CSS provides anchor positioning capabilities for UI such as:

```text
tooltips
menus
popover-like elements
floating controls
```

Conceptually:

```css
.anchor {
  anchor-name: --trigger;
}

.popover {
  position-anchor: --trigger;
}
```

Because this is an advanced/newer feature, verify browser support and fallback requirements before production use.

---

# 68. CSS Functions

Important categories:

## Color

```text
rgb()
hsl()
hwb()
lab()
lch()
oklab()
oklch()
color-mix()
```

## Math

```text
calc()
min()
max()
clamp()
```

## Resource

```text
url()
```

## Attribute

```text
attr()
```

Use functions when they make the design system more flexible and maintainable.

---

# 69. At-Rules

Important at-rules include:

```css
@media
@supports
@container
@font-face
@keyframes
@layer
@property
@import
```

---

# 70. `@supports`

Feature detection:

```css
@supports (display: grid) {
  .layout {
    display: grid;
  }
}
```

Use when a fallback is needed.

Example:

```css
.layout {
  display: flex;
}

@supports (display: grid) {
  .layout {
    display: grid;
  }
}
```

---

# 71. `@font-face`

Custom fonts:

```css
@font-face {
  font-family: "MyFont";
  src: url("/fonts/my-font.woff2") format("woff2");
  font-display: swap;
}
```

Use appropriate font loading strategy and provide fallback fonts.

---

# 72. CSS Architecture

For a medium/large application, organize styles conceptually:

```text
styles/
├── reset.css
├── tokens.css
├── base.css
├── layout.css
├── components/
│   ├── button.css
│   ├── card.css
│   ├── modal.css
│   └── form.css
├── utilities.css
└── pages/
```

Framework-specific component styles can use their own conventions.

---

# 73. Design Tokens

Centralize:

```css
:root {
  /* colors */
  --color-primary: ...;
  --color-background: ...;
  --color-surface: ...;
  --color-text: ...;

  /* spacing */
  --space-xs: ...;
  --space-sm: ...;
  --space-md: ...;
  --space-lg: ...;

  /* typography */
  --font-size-sm: ...;
  --font-size-md: ...;
  --font-size-lg: ...;

  /* radius */
  --radius-sm: ...;
  --radius-md: ...;

  /* shadows */
  --shadow-sm: ...;
  --shadow-md: ...;

  /* z-index */
  --z-dropdown: ...;
  --z-modal: ...;
}
```

The exact token values should come from the application's visual design system.

---

# 74. Component CSS Pattern

Example:

```css
.card {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);

  padding: var(--space-lg);

  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
}

.card__title {
  font-size: var(--font-size-lg);
}

.card__actions {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-sm);
}
```

Keep component selectors shallow.

---

# 75. Utility Classes

Small utilities can be useful:

```css
.text-center {
  text-align: center;
}

.flex {
  display: flex;
}

.grid {
  display: grid;
}

.hidden {
  display: none;
}
```

Do not create hundreds of arbitrary one-off utilities unless the project intentionally uses a utility-first design system.

---

# 76. CSS Reset / Normalize

A small reset can create predictable defaults:

```css
*,
*::before,
*::after {
  box-sizing: border-box;
}

html {
  line-height: 1.5;
}

body {
  margin: 0;
}

img,
svg,
video {
  display: block;
  max-width: 100%;
}
```

Do not reset styles blindly. Preserve useful browser behavior and accessibility.

---

# 77. Responsive Container Pattern

```css
.container {
  width: min(100% - 2rem, 1200px);
  margin-inline: auto;
}
```

Alternative:

```css
.container {
  width: 100%;
  max-width: 1200px;
  margin-inline: auto;
  padding-inline: 1rem;
}
```

Choose one consistent project convention.

---

# 78. Stack Pattern

```css
.stack {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
```

Use instead of repeated vertical margins when appropriate.

---

# 79. Cluster Pattern

```css
.cluster {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.75rem;
}
```

Useful for:

- tags
- action buttons
- navigation links
- filters

---

# 80. Centering Pattern

Flex:

```css
.center {
  display: flex;
  align-items: center;
  justify-content: center;
}
```

Grid:

```css
.center {
  display: grid;
  place-items: center;
}
```

Do not use absolute positioning merely to center ordinary content.

---

# 81. Card Grid Pattern

```css
.card-grid {
  display: grid;
  grid-template-columns:
    repeat(auto-fit, minmax(min(100%, 280px), 1fr));
  gap: 1.5rem;
}
```

This provides a flexible baseline for responsive card layouts.

---

# 82. Full-Height Application Shell

```css
.app {
  min-height: 100dvh;
  display: grid;
  grid-template-rows: auto 1fr auto;
}
```

Useful for:

```text
header
main
footer
```

---

# 83. Sidebar Layout

```css
.shell {
  display: grid;
  grid-template-columns:
    minmax(220px, 280px)
    minmax(0, 1fr);

  min-height: 100dvh;
}
```

Important:

```css
main {
  min-width: 0;
}
```

This prevents long content from causing grid overflow.

---

# 84. Modal Pattern

```css
.modal-backdrop {
  position: fixed;
  inset: 0;

  display: grid;
  place-items: center;

  padding: 1rem;
  background: rgb(0 0 0 / 0.5);
}

.modal {
  width: min(100%, 32rem);
  max-height: min(90dvh, 48rem);

  overflow: auto;

  background: var(--color-surface);
  border-radius: var(--radius-lg);
}
```

The HTML/JavaScript must also implement proper modal semantics and keyboard/focus behavior.

---

# 85. Toast Pattern

```css
.toast-container {
  position: fixed;
  inset-inline-end: 1rem;
  inset-block-end: 1rem;

  display: grid;
  gap: 0.75rem;

  z-index: var(--z-toast);
}

.toast {
  width: min(24rem, calc(100vw - 2rem));
}
```

Ensure toast messages remain accessible and are not the only way important information is communicated.

---

# 86. Skeleton Loading

```css
.skeleton {
  background:
    linear-gradient(
      90deg,
      var(--color-surface-muted) 25%,
      var(--color-surface) 50%,
      var(--color-surface-muted) 75%
    );

  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
}

@keyframes skeleton-loading {
  to {
    background-position: -200% 0;
  }
}

@media (prefers-reduced-motion: reduce) {
  .skeleton {
    animation: none;
  }
}
```

Use skeletons only when they meaningfully communicate loading state.

---

# 87. Debugging CSS

When a style is not working:

## Step 1

Inspect the element.

## Step 2

Check whether the selector matches.

## Step 3

Check whether the declaration is crossed out.

If crossed out:

```text
cascade
specificity
layer
importance
```

are likely relevant.

## Step 4

Check computed styles.

## Step 5

Check layout boxes.

## Step 6

Check parent constraints:

```text
width
height
overflow
display
position
grid
flex
```

## Step 7

Check media/container queries.

## Step 8

Check pseudo-class state:

```text
:hover
:focus
:active
:disabled
```

## Step 9

Check inherited values.

## Step 10

Check browser support for newer CSS features.

---

# 88. Common CSS Bugs

| Problem | Likely cause |
|---|---|
| Element overflows flex container | `min-width: 0` missing |
| Grid content overflows | grid track/content constraints |
| `z-index` does not work | stacking context |
| Margin spacing looks wrong | margin collapse |
| Width unexpectedly larger | `box-sizing` |
| Text not truncating | missing width/min-width/overflow |
| Sticky not working | ancestor overflow/containing context |
| Fixed element moves unexpectedly | transformed ancestor/context |
| CSS rule ignored | specificity/layer/order |
| Mobile layout breaks | fixed widths / missing responsive constraints |
| Animation causes jank | layout-heavy animated properties |
| Dark mode incomplete | hard-coded colors |
| Focus invisible | outline removed |
| Font causes layout shift | font loading/metrics |
| `height: 100vh` behaves poorly on mobile | viewport unit behavior; consider `dvh` |
| New feature fails in browser | unsupported CSS feature |

---

# 89. Performance Guidelines

Prefer:

```text
simple selectors
small component styles
CSS containment when justified
transform/opacity for frequent animation
responsive images
minimal unnecessary DOM
```

Avoid:

```text
deep selector chains
huge global stylesheets
excessive box shadows
continuous expensive animations
unnecessary JavaScript layout manipulation
hundreds of competing overrides
```

Measure performance before applying complex optimization.

---

# 90. Browser Compatibility

For standard properties and modern layouts:

```text
Flexbox
Grid
custom properties
media queries
container queries
```

are widely useful, but individual advanced features can differ in support.

For production decisions:

1. Identify target browsers.
2. Check MDN browser compatibility data.
3. Provide a fallback if required.
4. Test the actual application.

Do not assume that "modern CSS" means "supported everywhere."

---

# 91. Progressive Enhancement

Example:

```css
.layout {
  display: flex;
  flex-direction: column;
}

@supports (display: grid) {
  .layout {
    display: grid;
    grid-template-columns: 240px 1fr;
  }
}
```

Use fallback + enhancement when compatibility requirements justify it.

---

# 92. Print Styles

Example:

```css
@media print {
  .navigation,
  .actions {
    display: none;
  }

  .content {
    max-width: none;
  }
}
```

Use print styles when the application generates documents or printable reports.

---

# 93. Dark Mode Architecture

Token-based:

```css
:root {
  --color-background: #ffffff;
  --color-surface: #f8fafc;
  --color-text: #0f172a;
}

@media (prefers-color-scheme: dark) {
  :root {
    --color-background: #0f172a;
    --color-surface: #1e293b;
    --color-text: #f8fafc;
  }
}
```

If the application has a manual theme toggle, use a theme attribute/class:

```html
<html data-theme="dark">
```

```css
[data-theme="dark"] {
  --color-background: #0f172a;
  --color-surface: #1e293b;
  --color-text: #f8fafc;
}
```

---

# 94. CSS + JavaScript Boundary

Prefer CSS for:

```text
hover
focus
visual state
responsive layout
transitions
animations
theme tokens
show/hide based on state when CSS can express it
```

Use JavaScript for:

```text
application state
data fetching
complex interactions
browser APIs
dynamic measurements that CSS cannot express
```

Avoid JavaScript that directly writes styles when a class/state attribute is sufficient.

Prefer:

```js
element.classList.toggle("is-open");
```

over:

```js
element.style.display = "block";
```

---

# 95. CSS + Framework Guidance

## Component frameworks

For Angular/React/Vue/etc.:

- Keep component styles near components.
- Keep global tokens/reset global.
- Avoid leaking component-specific selectors globally.
- Use CSS custom properties for shared theme values.
- Keep responsive rules close to the component they affect.
- Avoid unnecessary global overrides.

## Utility-first CSS

If the project uses Tailwind or another utility system:

- follow the existing project's conventions
- do not introduce large custom CSS files for styling that the utility system already handles
- use custom CSS for genuinely reusable/complex patterns
- preserve design tokens

## Plain CSS

Use a clear architecture:

```text
tokens
base
layout
components
utilities
pages
```

---

# 96. Coding Agent Decision Rules

When implementing a visual feature, follow this sequence.

## Step 1 — Identify semantic structure

Ask:

```text
What HTML elements represent this UI?
```

## Step 2 — Choose layout system

Use:

```text
normal flow -> simple document structure
flex -> one-dimensional alignment
grid -> two-dimensional layout
position -> overlays/floating UI
```

## Step 3 — Identify responsive behavior

Ask:

```text
At what size does the current layout stop working?
```

Use content-driven breakpoints.

## Step 4 — Identify design tokens

Reuse:

```text
colors
spacing
typography
radius
shadows
z-index
```

before creating new values.

## Step 5 — Implement component states

Consider:

```text
default
hover
focus-visible
active
disabled
loading
error
success
selected
expanded
```

## Step 6 — Check accessibility

Verify:

```text
keyboard focus
contrast
reduced motion
readability
touch target
```

## Step 7 — Check overflow

Test:

```text
long text
small screen
large text
large images
empty states
```

## Step 8 — Check browser support

Especially for:

```text
container queries
anchor positioning
advanced selectors
new color functions
advanced animations
```

---

# 97. Coding Agent Rules — DO

- Use semantic HTML.
- Prefer classes/custom properties for reusable styling.
- Use Flexbox and Grid appropriately.
- Prefer `gap` for component spacing.
- Use `min-width: 0` when appropriate in flexible layouts.
- Use `max-width` constraints for readable content.
- Use `clamp()` for fluid typography where appropriate.
- Use logical properties for reusable layouts.
- Use CSS variables for design tokens.
- Preserve visible focus.
- Support reduced motion.
- Test responsive states.
- Test long content.
- Keep selectors shallow.
- Keep global styles minimal.
- Check the cascade before adding overrides.
- Check browser support for advanced CSS.
- Prefer CSS state over JavaScript style mutation.

---

# 98. Coding Agent Rules — DON'T

- Do not use absolute positioning for the entire page layout.
- Do not solve every layout problem with `margin-left`.
- Do not use arbitrary `z-index: 999999`.
- Do not remove focus outlines without replacement.
- Do not rely on color alone to communicate state.
- Do not use `!important` as the normal override mechanism.
- Do not create deep selector chains.
- Do not hard-code the same color/spacing repeatedly.
- Do not use fixed widths when content should be fluid.
- Do not animate expensive layout properties unnecessarily.
- Do not assume `100vh` is always correct on mobile.
- Do not hide overflow without understanding what is being clipped.
- Do not use pseudo-elements for essential semantic information.
- Do not introduce a CSS framework if the project already has a styling system unless requested.
- Do not mix multiple CSS architectures without a clear reason.

---

# 99. Project CSS Checklist

Before considering a CSS feature complete:

- [ ] Semantic HTML is used.
- [ ] Correct layout system selected.
- [ ] Desktop layout works.
- [ ] Mobile layout works.
- [ ] Intermediate widths work.
- [ ] Long content works.
- [ ] Empty states work.
- [ ] Loading state works.
- [ ] Error state works.
- [ ] Hover state works where applicable.
- [ ] Keyboard focus is visible.
- [ ] Disabled state is understandable.
- [ ] Contrast is acceptable.
- [ ] Reduced-motion behavior is considered.
- [ ] Design tokens are reused.
- [ ] No unnecessary `!important`.
- [ ] Selectors are not excessively specific.
- [ ] No accidental overflow.
- [ ] No unexplained `z-index`.
- [ ] Images maintain appropriate aspect ratios.
- [ ] Advanced CSS features have appropriate fallbacks/support.
- [ ] Styles do not leak unexpectedly into unrelated components.
- [ ] Browser DevTools inspection shows expected computed styles.

---

# 100. Quick CSS Cookbook

## Center

```css
.center {
  display: grid;
  place-items: center;
}
```

## Responsive container

```css
.container {
  width: min(100% - 2rem, 1200px);
  margin-inline: auto;
}
```

## Responsive cards

```css
.cards {
  display: grid;
  grid-template-columns:
    repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
}
```

## Horizontal actions

```css
.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
}
```

## Vertical stack

```css
.stack {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
```

## Responsive title

```css
.title {
  font-size: clamp(2rem, 5vw, 4rem);
}
```

## Full-height page

```css
.page {
  min-height: 100dvh;
}
```

## Image cover

```css
.image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
```

## Focus

```css
:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}
```

## Reduced motion

```css
@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms;
    animation-iteration-count: 1;
    transition-duration: 0.01ms;
    scroll-behavior: auto;
  }
}
```

---

# 101. Troubleshooting Workflow

When the coding agent receives:

> "The CSS isn't working."

Do not immediately add more CSS.

Use:

```text
1. Inspect element
2. Confirm selector matches
3. Check computed styles
4. Check crossed-out declarations
5. Check specificity
6. Check cascade layers
7. Check inheritance
8. Check parent layout
9. Check overflow
10. Check media/container query
11. Check pseudo-class state
12. Check stacking context
13. Check browser support
```

This prevents CSS override accumulation.

---

# 102. Recommended CSS Project Structure

For a framework-independent application:

```text
src/
├── styles/
│   ├── tokens.css
│   ├── reset.css
│   ├── base.css
│   ├── layout.css
│   ├── utilities.css
│   ├── components/
│   │   ├── button.css
│   │   ├── card.css
│   │   ├── form.css
│   │   ├── modal.css
│   │   └── navigation.css
│   └── pages/
│       ├── home.css
│       └── dashboard.css
```

For Angular or another component framework:

```text
src/
├── styles.css
├── styles/
│   ├── tokens.css
│   ├── reset.css
│   └── utilities.css
└── app/
    └── components/
        ├── button/
        │   └── button.component.css
        ├── card/
        │   └── card.component.css
        └── modal/
            └── modal.component.css
```

Follow the existing project's framework conventions if they differ.

---

# 103. CSS Reference Categories

When an implementation requires deeper research, search MDN by category:

```text
Selectors
Cascade
Inheritance
Specificity
Nesting
Scoping
Box model
Box sizing
Display
Flexbox
Grid
Positioning
Overflow
Logical properties
Media queries
Container queries
Colors
Typography
Images
Backgrounds
Borders
Transforms
Transitions
Animations
Scroll snap
Containment
Custom properties
At-rules
Accessibility
Browser compatibility
```

---

# 104. Official MDN References

Primary CSS documentation:

https://developer.mozilla.org/en-US/docs/Web/CSS

CSS selectors:

https://developer.mozilla.org/en-US/docs/Web/CSS/Reference/Selectors

CSS cascade:

https://developer.mozilla.org/en-US/docs/Web/CSS/Guides/Cascade

Flexbox:

https://developer.mozilla.org/en-US/docs/Web/CSS/Guides/Flexible_box_layout

CSS layout:

https://developer.mozilla.org/en-US/docs/Learn_web_development/Core/CSS_layout

CSS reference:

https://developer.mozilla.org/en-US/docs/Web/CSS/Reference

---

# 105. Final Agent Instruction

When using this file as project context:

```text
You are implementing a modern web application.

Before writing CSS:
1. Inspect existing project styles.
2. Reuse existing tokens and component patterns.
3. Determine the correct layout model.
4. Prefer semantic HTML + CSS.
5. Follow the project's established CSS architecture.
6. Avoid unnecessary overrides.
7. Check responsive behavior.
8. Check accessibility.
9. Check reduced-motion behavior.
10. Check browser support for advanced features.

When CSS conflicts occur:
- inspect the cascade first
- inspect specificity
- inspect layers
- inspect inheritance
- inspect layout constraints
- only then change the CSS

Do not blindly add more selectors or !important rules.
```

---

# Source Note

This document is an **implementation-oriented reference derived from the MDN CSS documentation**, not a replacement for MDN's complete specification/reference material.

MDN currently organizes CSS documentation around CSS properties, selectors, at-rules, values, guides, layout, cascade/inheritance, box model, Flexbox, Grid, responsive queries, animations, transitions, transforms, and many additional CSS modules.

For exact syntax, browser compatibility, edge cases, or newly introduced CSS features, consult the relevant MDN reference page before making a production-critical decision.

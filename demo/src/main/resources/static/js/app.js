/*
 * The only custom JavaScript in the project. Everything else is Bootstrap data
 * attributes or htmx attributes.
 */
(function () {
  "use strict";

  const csrfMeta = document.querySelector('meta[name="csrf-token"]');
  const csrfHeader = document.querySelector('meta[name="csrf-header"]')?.getAttribute("content")
    || "X-CSRF-TOKEN";

  // Read per request rather than cached in a variable: the server keeps one token alive for the
  // life of the XSRF-TOKEN cookie, so the value in the markup stays valid for as many
  // mutations as the user makes, and re-reading costs nothing. This is also why an htmx swap
  // that replaces the page body does not need to re-seed anything.
  document.body.addEventListener("htmx:configRequest", function (event) {
    const token = csrfMeta?.getAttribute("content");
    if (token) {
      event.detail.headers[csrfHeader] = token;
    }
  });

  // A 422 means "the form did not validate and here is why". htmx does not swap
  // 4xx bodies by default, so opt this one status in.
  document.body.addEventListener("htmx:beforeSwap", function (event) {
    if (event.detail.xhr.status === 422) {
      event.detail.shouldSwap = true;
      event.detail.isError = false;
    }
  });

  // A create returns the refreshed grid out-of-band plus this marker; the grid is
  // already updated by the time the swap lands, so all that is left is closing the
  // modal and putting a clean form back for next time.
  document.body.addEventListener("htmx:afterSwap", function (event) {
    if (!event.detail.target || !event.detail.target.querySelector("[data-note-created]")) {
      return;
    }
    const modalElement = document.getElementById("noteModal");
    if (!modalElement) {
      return;
    }
    const body = document.getElementById("noteModalBody");
    const modal = window.bootstrap?.Modal.getInstance(modalElement);
    if (modal) {
      modal.hide();
    }
    // Re-fetch the form only once the close animation has finished, otherwise the
    // fresh form is thrown away by the transition still in progress.
    modalElement.addEventListener("hidden.bs.modal", function onHidden() {
      modalElement.removeEventListener("hidden.bs.modal", onHidden);
      if (body && window.htmx) {
        window.htmx.ajax("GET", body.dataset.noteFormUrl || "/fragments/note-form", {
          target: "#noteModalBody",
          swap: "innerHTML",
        });
      }
    }, { once: true });
  });

  // The bio has a minimum length, so show the count as it is typed rather than
  // letting the browser reject the form with no explanation.
  const counterFor = (bio) => {
    const counter = document.querySelector("[data-bio-counter]");
    if (!counter) {
      return;
    }
    const length = bio.value.trim().length;
    counter.textContent = length + " / " + bio.minLength + " characters";
    counter.classList.toggle("text-body-secondary", length >= Number(bio.minLength));
    counter.classList.toggle("text-danger", length < Number(bio.minLength));
  };

  document.body.addEventListener("input", function (event) {
    const bio = event.target;
    if (bio.matches && bio.matches("[data-bio-input]")) {
      counterFor(bio);
    }
  });

  // Seed the counter from the bio that is already there, otherwise opening the
  // editor on a saved profile shows "0 / 150" for a bio that is already valid.
  document.querySelectorAll("[data-bio-input]").forEach(counterFor);

  /* --- Theme -------------------------------------------------------------
   * Bootstrap 5.3 ships the dark theme already; all this does is flip
   * data-bs-theme and remember the choice. The head script sets the initial
   * value before first paint, so there is nothing to do on load beyond
   * labelling the button correctly.
   */
  const themeLabel = document.querySelector("[data-mn-theme-label]");

  const labelTheme = (theme) => {
    if (themeLabel) {
      themeLabel.textContent = theme === "dark" ? "Light" : "Dark";
    }
  };

  const currentTheme = () =>
    document.documentElement.getAttribute("data-bs-theme") || "light";

  labelTheme(currentTheme());

  document.querySelectorAll("[data-mn-theme-toggle]").forEach((button) => {
    button.addEventListener("click", () => {
      const next = currentTheme() === "dark" ? "light" : "dark";
      document.documentElement.setAttribute("data-bs-theme", next);
      labelTheme(next);
      try {
        localStorage.setItem("mn-theme", next);
      } catch (e) {
        /* Private mode: the theme still applies, it just will not persist. */
      }
    });
  });
})();
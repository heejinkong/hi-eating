(() => {
  const root = document.documentElement;
  const brandRow = document.querySelector(".header-brand-row");
  const threshold = 24;

  const updateHeaderOffset = () => {
    if (!brandRow) {
      return;
    }

    root.style.setProperty(
      "--header-sticky-offset",
      `${brandRow.getBoundingClientRect().height}px`,
    );
  };

  const updateHeaderState = () => {
    root.classList.toggle("is-header-condensed", window.scrollY > threshold);
  };

  updateHeaderOffset();
  updateHeaderState();
  window.addEventListener("resize", updateHeaderOffset);
  window.addEventListener("scroll", updateHeaderState, { passive: true });

  if ("ResizeObserver" in window && brandRow) {
    new ResizeObserver(updateHeaderOffset).observe(brandRow);
  }
})();

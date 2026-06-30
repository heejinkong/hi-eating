(() => {
  const carousel = document.querySelector("[data-hotdeal-carousel]");

  if (!carousel) {
    return;
  }

  const track = carousel.querySelector("[data-hotdeal-track]");
  const cards = Array.from(track.children);
  const prevButton = carousel.querySelector("[data-hotdeal-prev]");
  const nextButton = carousel.querySelector("[data-hotdeal-next]");

  if (cards.length === 0) {
    return;
  }

  const visibleCount = () => window.matchMedia("(max-width: 900px)").matches ? 1 : 2;
  let index = 0;

  const maxIndex = () => Math.max(0, cards.length - visibleCount());

  const moveTo = (nextIndex) => {
    index = Math.max(0, Math.min(nextIndex, maxIndex()));
    const cardWidth = cards[0].getBoundingClientRect().width;
    const gap = parseFloat(getComputedStyle(track).columnGap || getComputedStyle(track).gap || "0");
    track.style.transform = `translateX(-${index * (cardWidth + gap)}px)`;
  };

  const moveNext = () => {
    const nextIndex = index + visibleCount();
    moveTo(nextIndex > maxIndex() ? 0 : nextIndex);
  };

  prevButton.addEventListener("click", () => moveTo(index - visibleCount()));
  nextButton.addEventListener("click", moveNext);
  window.addEventListener("resize", () => moveTo(index));
  window.setInterval(moveNext, 5000);

  const formatTime = (seconds) => {
    const safeSeconds = Math.max(0, seconds);
    const hours = String(Math.floor(safeSeconds / 3600)).padStart(2, "0");
    const minutes = String(Math.floor((safeSeconds % 3600) / 60)).padStart(2, "0");
    const secs = String(safeSeconds % 60).padStart(2, "0");
    return `${hours}:${minutes}:${secs}`;
  };

  const timers = Array.from(document.querySelectorAll("[data-remaining-seconds]"));
  const tick = () => {
    timers.forEach((timer) => {
      const remainingSeconds = Number(timer.dataset.remainingSeconds || "0");
      timer.textContent = formatTime(remainingSeconds);
      timer.dataset.remainingSeconds = String(Math.max(0, remainingSeconds - 1));
    });
  };

  moveTo(0);
  tick();
  window.setInterval(tick, 1000);
})();

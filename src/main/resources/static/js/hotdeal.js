(() => {
  const page = document.querySelector("[data-hotdeal-page]");

  if (!page) {
    return;
  }

  const grid = page.querySelector("[data-hotdeal-grid]");
  const emptyMessage = page.querySelector("[data-hotdeal-empty]");
  const allHero = page.querySelector("[data-hotdeal-all-hero]");
  const selectedHero = page.querySelector("[data-hotdeal-selected-hero]");
  const heroTitle = page.querySelector("[data-hotdeal-hero-title]");
  const heroImage = page.querySelector("[data-hotdeal-hero-image]");
  const listTitle = page.querySelector("[data-hotdeal-list-title]");
  const sortSelect = page.querySelector("[data-hotdeal-sort]");
  const loader = page.querySelector("[data-hotdeal-loader]");
  const endMessage = page.querySelector("[data-hotdeal-end]");
  const sentinel = page.querySelector("[data-hotdeal-sentinel]");
  const tabs = Array.from(page.querySelectorAll("[data-hotdeal-tab]"));

  let currentPage = Number(page.dataset.page || "1");
  let hasMore = page.dataset.hasMore === "true";
  let loading = false;

  const formatTime = (seconds) => {
    const safeSeconds = Math.max(0, seconds);
    const hours = String(Math.floor(safeSeconds / 3600)).padStart(2, "0");
    const minutes = String(Math.floor((safeSeconds % 3600) / 60)).padStart(2, "0");
    const secs = String(safeSeconds % 60).padStart(2, "0");
    return `${hours}:${minutes}:${secs}`;
  };

  const tickTimers = () => {
    page.querySelectorAll("[data-remaining-seconds]").forEach((timer) => {
      const remainingSeconds = Number(timer.dataset.remainingSeconds || "0");
      timer.textContent = formatTime(remainingSeconds);
      timer.dataset.remainingSeconds = String(Math.max(0, remainingSeconds - 1));
    });
  };

  const formatPrice = (price) => `${Number(price || 0).toLocaleString("ko-KR")}\uC6D0`;

  const escapeHtml = (value) =>
    String(value ?? "")
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;")
      .replaceAll('"', "&quot;")
      .replaceAll("'", "&#039;");

  const productCardTemplate = (product) => {
    const imageSrc = product.pictureLocation || "/images/logo-hi-eating.png";
    const detailUrl = `/product/${product.productId}`;

    return `
      <article class="hotdeal-product-card">
        <a class="hotdeal-product-card__image" href="${detailUrl}">
          <img src="${escapeHtml(imageSrc)}"
               alt="${escapeHtml(product.productName)}"
               data-fallback-src="/images/logo-hi-eating.png">
          <span class="hotdeal-product-card__badge">${product.discountRate}%</span>
          <span class="hotdeal-product-card__stock">\uC7AC\uACE0 ${product.stock}</span>
        </a>
        <p class="hotdeal-product-card__deal">${escapeHtml(product.hotDealTitle)}</p>
        <a class="hotdeal-product-card__name" href="${detailUrl}">
          ${escapeHtml(product.productName)}
        </a>
        <div class="hotdeal-product-card__price">
          <span>${product.discountRate}%</span>
          <strong>${formatPrice(product.hotDealPrice)}</strong>
        </div>
        <del>${formatPrice(product.originalPrice)}</del>
        <p class="hotdeal-product-card__meta">
          <span>\uC870\uD68C ${product.viewCount}</span>
          <span>\uAD6C\uB9E4 ${product.totalQuantity}</span>
        </p>
      </article>
    `;
  };

  const buildProductsUrl = () => {
    const params = new URLSearchParams();
    const hotDealId = page.dataset.hotdealId;

    if (hotDealId) {
      params.set("hotDealId", hotDealId);
    }

    params.set("sort", page.dataset.sort || "popular");
    params.set("page", String(currentPage + 1));
    params.set("size", page.dataset.size || "20");

    return `/api/hot-deals/products?${params.toString()}`;
  };

  const buildPageUrl = () => {
    const url = new URL(window.location.href);
    url.searchParams.delete("hotDealId");
    url.searchParams.set("sort", page.dataset.sort || "popular");

    if (page.dataset.hotdealId) {
      url.searchParams.set("hotDealId", page.dataset.hotdealId);
    }

    return url.toString();
  };

  const setLoading = (nextLoading) => {
    loading = nextLoading;

    if (loader) {
      loader.hidden = !nextLoading;
    }
  };

  const loadMore = async () => {
    if (!grid || !hasMore || loading) {
      return;
    }

    setLoading(true);

    try {
      const response = await fetch(buildProductsUrl(), {
        headers: {
          Accept: "application/json"
        }
      });

      if (!response.ok) {
        throw new Error("Failed to load hot deal products.");
      }

      const productPage = await response.json();
      grid.insertAdjacentHTML(
        "beforeend",
        (productPage.products || []).map(productCardTemplate).join("")
      );

      currentPage = productPage.page;
      hasMore = productPage.hasMore;

      if (endMessage) {
        endMessage.hidden = hasMore;
      }
    } catch (error) {
      if (endMessage) {
        endMessage.hidden = false;
        endMessage.textContent =
            "\uC0C1\uD488\uC744 \uBD88\uB7EC\uC624\uC9C0 \uBABB\uD588\uC2B5\uB2C8\uB2E4. \uC7A0\uC2DC \uD6C4 \uB2E4\uC2DC \uC2DC\uB3C4\uD574\uC8FC\uC138\uC694.";
      }
      hasMore = false;
    } finally {
      setLoading(false);
    }
  };

  const renderProductPage = (productPage) => {
    const products = productPage.products || [];

    if (grid) {
      grid.innerHTML = products.map(productCardTemplate).join("");
      grid.hidden = products.length === 0;
    }

    if (emptyMessage) {
      emptyMessage.hidden = products.length > 0;
    }

    currentPage = productPage.page;
    hasMore = productPage.hasMore;

    if (endMessage) {
      endMessage.hidden = hasMore || products.length === 0;
      endMessage.textContent = "\uBAA8\uB4E0 \uD56B\uB51C \uC0C1\uD488\uC744 \uD655\uC778\uD588\uC2B5\uB2C8\uB2E4.";
    }
  };

  const updateHero = (tab) => {
    const hotDealId = tab.dataset.hotdealTabId || "";
    const title = tab.dataset.hotdealTabTitle || "\uC804\uCCB4 \uD56B\uB51C \uC0C1\uD488";

    if (listTitle) {
      listTitle.textContent = title;
    }

    if (!hotDealId) {
      if (allHero) {
        allHero.hidden = false;
      }
      if (selectedHero) {
        selectedHero.hidden = true;
      }
      return;
    }

    if (allHero) {
      allHero.hidden = true;
    }
    if (selectedHero) {
      selectedHero.hidden = false;
    }
    if (heroTitle) {
      heroTitle.textContent = title;
    }
    if (heroImage) {
      heroImage.src = tab.dataset.hotdealTabImage || "/images/logo-hi-eating.png";
      heroImage.alt = title;
      heroImage.dataset.fallbackApplied = "false";
    }

    const timer = selectedHero?.querySelector("[data-remaining-seconds]");
    if (timer) {
      timer.dataset.remainingSeconds = tab.dataset.hotdealTabRemaining || "0";
      timer.textContent = formatTime(Number(timer.dataset.remainingSeconds || "0"));
    }
  };

  const activateTab = (selectedTab) => {
    tabs.forEach((tab) => tab.classList.toggle("is-active", tab === selectedTab));
    page.dataset.hotdealId = selectedTab.dataset.hotdealTabId || "";
    updateHero(selectedTab);
  };

  const switchHotDeal = async (tab) => {
    if (loading || tab.classList.contains("is-active")) {
      return;
    }

    activateTab(tab);
    currentPage = 0;
    hasMore = true;
    history.pushState(null, "", buildPageUrl());

    setLoading(true);

    try {
      const response = await fetch(buildProductsUrl(), {
        headers: {
          Accept: "application/json"
        }
      });

      if (!response.ok) {
        throw new Error("Failed to switch hot deal products.");
      }

      renderProductPage(await response.json());
    } catch (error) {
      if (emptyMessage) {
        emptyMessage.hidden = false;
        emptyMessage.textContent =
            "\uC0C1\uD488\uC744 \uBD88\uB7EC\uC624\uC9C0 \uBABB\uD588\uC2B5\uB2C8\uB2E4. \uC7A0\uC2DC \uD6C4 \uB2E4\uC2DC \uC2DC\uB3C4\uD574\uC8FC\uC138\uC694.";
      }
      if (grid) {
        grid.hidden = true;
      }
      hasMore = false;
    } finally {
      setLoading(false);
    }
  };

  sortSelect?.addEventListener("change", () => {
    const url = new URL(window.location.href);
    url.searchParams.set("sort", sortSelect.value);
    url.searchParams.delete("page");
    window.location.href = url.toString();
  });

  tabs.forEach((tab) => {
    tab.addEventListener("click", (event) => {
      event.preventDefault();
      switchHotDeal(tab);
    });
  });

  if (sentinel && "IntersectionObserver" in window) {
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries.some((entry) => entry.isIntersecting)) {
          loadMore();
        }
      },
      { rootMargin: "240px 0px" }
    );

    observer.observe(sentinel);
  }

  tickTimers();
  window.setInterval(tickTimers, 1000);
})();

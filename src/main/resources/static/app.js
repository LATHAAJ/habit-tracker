(() => {
  const TOKEN_KEY = "habit_tracker_token";
  const EMAIL_KEY = "habit_tracker_email";
  const HEATMAP_DAYS = 30;

  const authView = document.getElementById("auth-view");
  const dashboardView = document.getElementById("dashboard-view");
  const userChip = document.getElementById("user-chip");
  const userEmailEl = document.getElementById("user-email");
  const habitGrid = document.getElementById("habit-grid");
  const emptyState = document.getElementById("empty-state");
  const habitCardTemplate = document.getElementById("habit-card-template");

  function getToken() {
    return localStorage.getItem(TOKEN_KEY);
  }

  function setSession(token, email) {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(EMAIL_KEY, email);
  }

  function clearSession() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(EMAIL_KEY);
  }

  async function api(path, options = {}) {
    const headers = Object.assign({ "Content-Type": "application/json" }, options.headers || {});
    const token = getToken();
    if (token) headers["Authorization"] = "Bearer " + token;

    const response = await fetch(path, Object.assign({}, options, { headers }));

    if (response.status === 401) {
      clearSession();
      showAuthView();
      throw new Error("Session expired, please log in again");
    }

    if (!response.ok) {
      let message = "Something went wrong";
      try {
        const body = await response.json();
        message = body.error || message;
      } catch (_) {}
      throw new Error(message);
    }

    if (response.status === 204) return null;
    return response.json();
  }

  function showAuthView() {
    authView.classList.remove("hidden");
    dashboardView.classList.add("hidden");
    userChip.classList.add("hidden");
  }

  function showDashboard() {
    authView.classList.add("hidden");
    dashboardView.classList.remove("hidden");
    userChip.classList.remove("hidden");
    userEmailEl.textContent = localStorage.getItem(EMAIL_KEY) || "";
    loadHabits();
  }

  // --- auth tabs ---

  document.querySelectorAll(".auth-tab").forEach((tab) => {
    tab.addEventListener("click", () => {
      document.querySelectorAll(".auth-tab").forEach((t) => t.classList.remove("active"));
      tab.classList.add("active");
      const isLogin = tab.dataset.tab === "login";
      document.getElementById("login-form").classList.toggle("hidden", !isLogin);
      document.getElementById("signup-form").classList.toggle("hidden", isLogin);
    });
  });

  document.getElementById("login-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const form = e.target;
    const errorEl = document.getElementById("login-error");
    errorEl.textContent = "";
    try {
      const data = await api("/api/auth/login", {
        method: "POST",
        body: JSON.stringify({
          email: form.email.value.trim(),
          password: form.password.value,
        }),
      });
      setSession(data.token, data.email);
      form.reset();
      showDashboard();
    } catch (err) {
      errorEl.textContent = err.message;
    }
  });

  document.getElementById("signup-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const form = e.target;
    const errorEl = document.getElementById("signup-error");
    errorEl.textContent = "";
    try {
      const data = await api("/api/auth/signup", {
        method: "POST",
        body: JSON.stringify({
          email: form.email.value.trim(),
          password: form.password.value,
        }),
      });
      setSession(data.token, data.email);
      form.reset();
      showDashboard();
    } catch (err) {
      errorEl.textContent = err.message;
    }
  });

  document.getElementById("logout-btn").addEventListener("click", () => {
    clearSession();
    showAuthView();
  });

  // --- habits ---

  document.getElementById("add-habit-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const form = e.target;
    try {
      await api("/api/habits", {
        method: "POST",
        body: JSON.stringify({
          name: form.name.value.trim(),
          description: form.description.value.trim(),
        }),
      });
      form.reset();
      loadHabits();
    } catch (err) {
      alert(err.message);
    }
  });

  async function loadHabits() {
    let habits;
    try {
      habits = await api("/api/habits");
    } catch (err) {
      return;
    }

    habitGrid.innerHTML = "";
    emptyState.classList.toggle("hidden", habits.length > 0);

    for (const habit of habits) {
      const card = renderHabitCard(habit);
      habitGrid.appendChild(card);
      loadHeatmap(habit.id, card);
    }
  }

  function renderHabitCard(habit) {
    const node = habitCardTemplate.content.cloneNode(true);
    const card = node.querySelector(".habit-card");
    card.dataset.habitId = habit.id;

    card.querySelector(".habit-name").textContent = habit.name;
    card.querySelector(".habit-description").textContent = habit.description || "";

    const badges = card.querySelectorAll(".streak-number");
    badges[0].textContent = habit.currentStreak;
    badges[1].textContent = habit.longestStreak;

    const toggleBtn = card.querySelector(".btn-toggle");
    updateToggleButton(toggleBtn, habit.completedToday);
    toggleBtn.addEventListener("click", async () => {
      try {
        const updated = await api(`/api/habits/${habit.id}/toggle`, { method: "POST" });
        badges[0].textContent = updated.currentStreak;
        badges[1].textContent = updated.longestStreak;
        updateToggleButton(toggleBtn, updated.completedToday);
        loadHeatmap(habit.id, card);
      } catch (err) {
        alert(err.message);
      }
    });

    card.querySelector(".delete-btn").addEventListener("click", async () => {
      if (!confirm(`Delete "${habit.name}"? This removes all its history.`)) return;
      try {
        await api(`/api/habits/${habit.id}`, { method: "DELETE" });
        loadHabits();
      } catch (err) {
        alert(err.message);
      }
    });

    return node;
  }

  function updateToggleButton(btn, done) {
    btn.dataset.done = String(done);
    btn.classList.toggle("done", done);
    btn.textContent = done ? "Done today ✓" : "Mark today done";
  }

  async function loadHeatmap(habitId, card) {
    const today = new Date();
    const from = new Date(today);
    from.setDate(from.getDate() - (HEATMAP_DAYS - 1));

    const fromStr = toIsoDate(from);
    const toStr = toIsoDate(today);

    let completed = [];
    try {
      completed = await api(`/api/habits/${habitId}/logs?from=${fromStr}&to=${toStr}`);
    } catch (err) {
      return;
    }
    const completedSet = new Set(completed);

    const heatmap = card.querySelector(".heatmap");
    heatmap.innerHTML = "";
    for (let i = 0; i < HEATMAP_DAYS; i++) {
      const d = new Date(from);
      d.setDate(d.getDate() + i);
      const iso = toIsoDate(d);
      const dot = document.createElement("div");
      dot.className = "day" + (completedSet.has(iso) ? " filled" : "");
      dot.title = iso;
      heatmap.appendChild(dot);
    }
  }

  function toIsoDate(d) {
    return d.toISOString().slice(0, 10);
  }

  // --- boot ---

  if (getToken()) {
    showDashboard();
  } else {
    showAuthView();
  }
})();

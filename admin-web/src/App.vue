<template>
  <router-view v-slot="{ Component, route }">
    <transition name="page-fade" mode="out-in">
      <component :is="Component" :key="route.path" />
    </transition>
  </router-view>
</template>

<script setup>
</script>

<style>
/* ===========================================
   NEO-BRUTALIST SOFT — Design System
   粗野主义 × 温暖人性化
   =========================================== */

:root {
  /* === Brand === */
  --color-primary: #111;
  --color-accent: #e53935;
  --color-accent-glow: #ff6f60;
  --color-highlight: #ffb300;

  /* === Backgrounds === */
  --bg-page: #fafaf9;
  --bg-surface: #fff;
  --bg-muted: #f2f0ed;

  /* === Borders (hard, visible — brutalist) === */
  --border: #111;
  --border-light: #d9d7d4;

  /* === Text === */
  --text: #111;
  --text-secondary: #555;
  --text-muted: #999;

  /* === Semantic === */
  --color-success: #1b5e20;
  --color-danger: #c62828;
  --color-warning: #e65100;
  --color-info: #0d47a1;

  /* === Spacing === */
  --s-xs: 4px;
  --s-sm: 8px;
  --s-md: 16px;
  --s-lg: 24px;
  --s-xl: 32px;
  --s-2xl: 48px;

  /* === Radii (slight, not rounded — brutalist) === */
  --r-sm: 2px;
  --r-md: 4px;
  --r-lg: 6px;

  /* === Neo-Brutalist Shadows === */
  --shadow-hard: 4px 4px 0 #111;
  --shadow-hard-sm: 2px 2px 0 #111;
  --shadow-hard-lg: 6px 6px 0 #111;

  /* === Typography === */
  --font-display: 'ZCOOL XiaoWei', 'Noto Sans SC', serif;
  --font-body: 'Noto Sans SC', -apple-system, sans-serif;
  --font-mono: 'SF Mono', 'Cascadia Code', 'Consolas', monospace;

  /* === Transitions === */
  --ease: cubic-bezier(0.16, 1, 0.3, 1);
  --fast: 120ms;
  --normal: 200ms;
}

/* === Element Plus Brutalist Overrides === */
:root {
  --el-color-primary: #111;
  --el-color-primary-light-3: #444;
  --el-color-primary-light-5: #777;
  --el-color-primary-light-7: #ccc;
  --el-color-primary-light-9: #eee;
  --el-color-primary-dark-2: #000;

  --el-color-success: var(--color-success);
  --el-color-danger: var(--color-danger);
  --el-color-warning: var(--color-warning);
  --el-color-info: var(--color-info);

  --el-bg-color: var(--bg-page);
  --el-bg-color-page: var(--bg-page);
  --el-bg-color-overlay: var(--bg-surface);
  --el-border-color: var(--border);
  --el-border-color-light: var(--border-light);
  --el-border-color-lighter: var(--border-light);
  --el-border-color-extra-light: var(--border-light);

  --el-text-color-primary: var(--text);
  --el-text-color-regular: var(--text-secondary);
  --el-text-color-secondary: var(--text-muted);
  --el-text-color-placeholder: var(--text-muted);

  --el-fill-color: var(--bg-muted);
  --el-fill-color-blank: var(--bg-surface);
  --el-fill-color-light: var(--bg-muted);
  --el-fill-color-lighter: var(--bg-muted);

  --el-font-size-base: 14px;
  --el-border-radius-base: var(--r-sm);
  --el-border-radius-round: 0;
  --el-border-color-hover: #111;
}

/* ============================================
   GLOBAL RESETS
   ============================================ */
*,
*::before,
*::after {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html { height: 100%; -webkit-font-smoothing: antialiased; }

body {
  height: 100%;
  font-family: var(--font-body);
  font-size: 14px;
  color: var(--text);
  background: var(--bg-page);
  line-height: 1.6;
  overflow: hidden;
}

#app { height: 100%; }

/* === Scrollbar (minimal brutalist) === */
::-webkit-scrollbar { width: 8px; height: 8px; }
::-webkit-scrollbar-track { background: var(--bg-muted); border-left: 1px solid var(--border-light); }
::-webkit-scrollbar-thumb { background: #111; }
::-webkit-scrollbar-thumb:hover { background: #333; }
* { scrollbar-width: thin; scrollbar-color: #111 var(--bg-muted); }

/* === Focus === */
:focus-visible { outline: 3px solid #111; outline-offset: 3px; }

/* === Selection === */
::selection { background: #111; color: #fff; }

/* === Page Transition === */
.page-fade-enter-active { transition: opacity 0.15s, transform 0.15s var(--ease); }
.page-fade-leave-active { transition: opacity 0.1s; }
.page-fade-enter-from { opacity: 0; transform: translateX(4px); }
.page-fade-leave-to { opacity: 0; }

/* === Global element overrides for brutalist feel === */
h1, h2, h3, h4 { font-family: var(--font-display); font-weight: 400; line-height: 1.2; }
button, .el-button { font-weight: 600 !important; letter-spacing: 0.02em; }
.el-button--primary { border: 2px solid #111 !important; }
</style>

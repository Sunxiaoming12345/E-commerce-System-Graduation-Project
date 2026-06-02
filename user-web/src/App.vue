<template>
  <router-view />
</template>

<script setup>
</script>

<style>
:root {
  --primary: #1a1a2e;
  --primary-light: #16213e;
  --primary-dark: #0f0f1a;
  --accent: #e2b96f;
  --accent-light: #f0d89d;
  --danger: #d35d6e;
  --success: #5e9b7a;
  --info: #7eb8c9;
  --bg: #faf9f7;
  --bg-warm: #f5f0eb;
  --surface: #ffffff;
  --text: #1a1a2e;
  --text-secondary: #8c8c8c;
  --text-muted: #b0b0b0;
  --border: #e8e4df;
  --border-light: #f0ede9;
  --shadow-xs: 0 1px 2px rgba(0,0,0,.04);
  --shadow-sm: 0 2px 8px rgba(0,0,0,.06);
  --shadow: 0 4px 16px rgba(0,0,0,.08);
  --shadow-lg: 0 12px 32px rgba(0,0,0,.10);
  --radius: 16px;
  --radius-sm: 10px;
  --radius-xs: 6px;
}

* { margin: 0; padding: 0; box-sizing: border-box; }
html, body, #app {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Noto Sans SC', sans-serif;
  background: var(--bg);
  color: var(--text);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* 全局 Element Plus 覆盖 */
.el-button--primary {
  --el-button-bg-color: var(--primary);
  --el-button-border-color: var(--primary);
  --el-button-hover-bg-color: var(--primary-light);
  --el-button-hover-border-color: var(--primary-light);
}
.el-button--primary:hover { opacity: .92; }
.el-tabs__active-bar { background: var(--primary) !important; }
.el-tabs__item.is-active { color: var(--text) !important; font-weight: 600; }
.el-input__wrapper { border-radius: var(--radius-xs) !important; }

/* 滚动条 */
::-webkit-scrollbar { width: 6px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb { background: #d4d0cb; border-radius: 3px; }

/* 全局动画 */
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to   { opacity: 1; transform: translateY(0); }
}
@keyframes fadeIn {
  from { opacity: 0; }
  to   { opacity: 1; }
}
@keyframes scaleIn {
  from { opacity: 0; transform: scale(.95); }
  to   { opacity: 1; transform: scale(1); }
}
@keyframes slideDown {
  from { opacity: 0; transform: translateY(-12px); }
  to   { opacity: 1; transform: translateY(0); }
}
.anim-fade-up  { animation: fadeInUp .5s ease both; }
.anim-fade-in  { animation: fadeIn .4s ease both; }
.anim-scale-in { animation: scaleIn .35s ease both; }
</style>

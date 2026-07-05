<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">AS</div>
        <div>
          <strong>Agent Study</strong>
          <span>高数学习 Agent</span>
        </div>
      </div>

      <nav class="nav-list">
        <RouterLink to="/student">学生学习闭环</RouterLink>
        <RouterLink to="/admin">后台管理</RouterLink>
      </nav>

      <div class="sidebar-note">
        <span>Backend</span>
        <strong>http://localhost:8080</strong>
      </div>
    </aside>

    <main class="main-view">
      <RouterView />
    </main>
  </div>
</template>

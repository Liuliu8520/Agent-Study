<template>
  <section class="page">
    <header class="page-header">
      <div>
        <p class="eyebrow">Admin Console</p>
        <h1>Agent 后台管理</h1>
      </div>
      <div v-if="auth.loggedIn" class="header-actions">
        <span class="admin-badge">{{ auth.username }}</span>
        <button class="button" @click="logout">退出</button>
      </div>
    </header>

    <div v-if="error" class="alert danger">{{ error }}</div>
    <div v-if="notice" class="alert success">{{ notice }}</div>

    <section v-if="!auth.loggedIn" class="login-panel auth-panel">
      <div class="panel-title">
        <h2>后台登录</h2>
      </div>
      <div class="form-grid">
        <label>
          <span>用户名</span>
          <input v-model="loginForm.username" class="input" />
        </label>
        <label>
          <span>密码</span>
          <input v-model="loginForm.password" class="input" type="password" />
        </label>
      </div>
      <button class="button primary" :disabled="loading" @click="login">登录</button>
    </section>

    <template v-else>
      <section class="status-strip admin-metrics">
        <div>
          <span>会话总数</span>
          <strong>{{ statistics?.sessions?.totalSessions ?? '-' }}</strong>
        </div>
        <div>
          <span>Agent 调用</span>
          <strong>{{ statistics?.agentCalls?.sampleSize ?? '-' }}</strong>
        </div>
        <div>
          <span>平均耗时</span>
          <strong>{{ statistics?.agentCalls?.averageDurationMillis ?? '-' }} ms</strong>
        </div>
        <div>
          <span>薄弱点</span>
          <strong>{{ statistics?.weakPoints?.length ?? '-' }}</strong>
        </div>
      </section>

      <section class="insight-grid">
        <article class="work-panel insight-panel">
          <div class="panel-title">
            <h2>薄弱点排行</h2>
          </div>
          <div v-if="weakPointBars.length" class="bar-list">
            <div v-for="item in weakPointBars" :key="item.code" class="bar-row">
              <div>
                <strong>{{ item.name }}</strong>
                <span>{{ item.latestReason }}</span>
              </div>
              <div class="bar-track">
                <i :style="{ width: `${item.percent}%` }"></i>
              </div>
              <em>{{ item.count }}</em>
            </div>
          </div>
          <div v-else class="empty-state compact-state">
            <strong>暂无薄弱点数据</strong>
            <span>完成诊断后，这里会出现高频薄弱知识点。</span>
          </div>
        </article>

        <article class="work-panel insight-panel">
          <div class="panel-title">
            <h2>Agent 耗时</h2>
          </div>
          <div v-if="agentDurationBars.length" class="bar-list">
            <div v-for="item in agentDurationBars" :key="item.agentType" class="bar-row">
              <div>
                <strong>{{ item.agentType }}</strong>
                <span>{{ item.callCount }} 次调用</span>
              </div>
              <div class="bar-track">
                <i :style="{ width: `${item.percent}%` }"></i>
              </div>
              <em>{{ formatMillis(item.averageDurationMillis) }}</em>
            </div>
          </div>
          <div v-else class="empty-state compact-state">
            <strong>暂无 Agent 调用数据</strong>
            <span>执行诊断、计划、讲义或复习后会记录调用耗时。</span>
          </div>
        </article>
      </section>

      <section class="admin-tabs">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          class="tab-button"
          :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
        </button>
      </section>

      <section v-if="activeTab === 'prompts'" class="admin-grid">
        <article class="work-panel">
          <div class="panel-title">
            <h2>Prompt 模板</h2>
            <button class="button small" :disabled="loading" @click="loadPrompts">刷新</button>
          </div>
          <div v-if="prompts.length" class="list-column">
            <button
              v-for="prompt in prompts"
              :key="prompt.code"
              class="select-row"
              :class="{ active: selectedPrompt?.code === prompt.code }"
              @click="selectPrompt(prompt)"
            >
              <strong>{{ prompt.code }}</strong>
              <span>{{ prompt.agentType }} · {{ prompt.version }}</span>
            </button>
          </div>
          <div v-else class="empty-state compact-state">
            <strong>暂无 Prompt 模板</strong>
            <span>后端启动后会初始化默认模板。</span>
          </div>
        </article>

        <article class="work-panel detail-panel">
          <div class="panel-title">
            <h2>编辑模板</h2>
            <button class="button small primary" :disabled="!promptForm.code || loading" @click="savePrompt">保存</button>
          </div>
          <div class="form-grid two">
            <label>
              <span>编码</span>
              <input v-model="promptForm.code" class="input" placeholder="lesson.custom" />
            </label>
            <label>
              <span>Agent 类型</span>
              <select v-model="promptForm.agentType" class="input">
                <option v-for="type in agentTypes" :key="type" :value="type">{{ type }}</option>
              </select>
            </label>
            <label>
              <span>版本</span>
              <input v-model="promptForm.version" class="input" placeholder="v2" />
            </label>
            <label>
              <span>名称</span>
              <input v-model="promptForm.name" class="input" />
            </label>
          </div>
          <label class="stack-label">
            <span>System Prompt</span>
            <textarea v-model="promptForm.systemPrompt" class="textarea" rows="4" />
          </label>
          <label class="stack-label">
            <span>User Prompt Template</span>
            <textarea v-model="promptForm.userPromptTemplate" class="textarea" rows="5" />
          </label>

          <div v-if="promptVersions.length" class="table-wrap">
            <table>
              <thead>
                <tr><th>版本</th><th>创建人</th><th>时间</th><th>状态</th><th>操作</th></tr>
              </thead>
              <tbody>
                <tr v-for="version in promptVersions" :key="version.versionId">
                  <td>{{ version.version }}</td>
                  <td>{{ version.createdBy }}</td>
                  <td>{{ formatTime(version.createdAt) }}</td>
                  <td>{{ version.active ? '启用中' : '历史' }}</td>
                  <td>
                    <button class="button tiny" :disabled="version.active || loading" @click="activatePromptVersion(version)">
                      启用
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </article>
      </section>

      <section v-if="activeTab === 'knowledge'" class="admin-grid">
        <article class="work-panel">
          <div class="panel-title">
            <h2>知识切片</h2>
            <button class="button small" :disabled="loading" @click="loadChunks">刷新</button>
          </div>
          <div v-if="chunks.length" class="list-column">
            <button
              v-for="chunk in chunks"
              :key="chunk.id"
              class="select-row"
              :class="{ active: chunkForm.id === chunk.id }"
              @click="selectChunk(chunk)"
            >
              <strong>{{ chunk.title }}</strong>
              <span>{{ chunk.chapter }} · {{ chunk.embeddingReady ? '已向量化' : '未向量化' }}</span>
            </button>
          </div>
          <div v-else class="empty-state compact-state">
            <strong>暂无知识切片</strong>
            <span>后端启动后会初始化默认高数知识库。</span>
          </div>
        </article>

        <article class="work-panel detail-panel">
          <div class="panel-title">
            <h2>维护切片</h2>
            <div class="button-row">
              <button class="button small" :disabled="loading" @click="clearChunkForm">新建</button>
              <button class="button small primary" :disabled="loading" @click="saveChunk">保存</button>
              <button class="button small" :disabled="!chunkForm.id || loading" @click="rebuildEmbedding">向量化</button>
              <button class="button small danger" :disabled="!chunkForm.id || loading" @click="deleteChunk">删除</button>
            </div>
          </div>
          <div class="form-grid two">
            <label>
              <span>ID</span>
              <input v-model="chunkForm.id" class="input" placeholder="chunk-custom" />
            </label>
            <label>
              <span>章节</span>
              <input v-model="chunkForm.chapter" class="input" />
            </label>
            <label class="span-two">
              <span>标题</span>
              <input v-model="chunkForm.title" class="input" />
            </label>
          </div>
          <label class="stack-label">
            <span>内容</span>
            <textarea v-model="chunkForm.content" class="textarea" rows="7" />
          </label>
          <label class="stack-label">
            <span>标签</span>
            <input v-model="chunkForm.tagsText" class="input" placeholder="chain_rule, derivative" />
          </label>
          <div class="retrieve-box">
            <input v-model="retrieveKeywords" class="input" placeholder="检索关键词，逗号分隔" />
            <button class="button" :disabled="loading" @click="retrieveChunks">检索</button>
          </div>
          <div v-if="retrievedChunks.length" class="side-list">
            <div v-for="item in retrievedChunks" :key="item.id" class="mini-card">
              <span>{{ item.title }}</span>
              <small>{{ item.id }} · {{ Number(item.score).toFixed(2) }}</small>
            </div>
          </div>
        </article>
      </section>

      <section v-if="activeTab === 'logs'" class="admin-grid">
        <article class="work-panel wide">
          <div class="panel-title">
            <h2>操作审计</h2>
            <button class="button small" :disabled="loading" @click="loadOperationLogs">查询</button>
          </div>
          <div class="form-grid four">
            <input v-model="logFilters.action" class="input" placeholder="action" />
            <input v-model="logFilters.targetType" class="input" placeholder="targetType" />
            <input v-model="logFilters.targetId" class="input" placeholder="targetId" />
            <input v-model="logFilters.operator" class="input" placeholder="operator" />
          </div>
          <div class="table-wrap">
            <table>
              <thead>
                <tr><th>时间</th><th>操作人</th><th>动作</th><th>对象</th><th>说明</th></tr>
              </thead>
              <tbody>
                <tr v-for="log in operationLogs" :key="log.logId">
                  <td>{{ formatTime(log.createdAt) }}</td>
                  <td>{{ log.operator }}</td>
                  <td>{{ log.action }}</td>
                  <td>{{ log.targetType }} / {{ log.targetId }}</td>
                  <td>{{ log.detail }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </article>

        <article class="work-panel wide">
          <div class="panel-title">
            <h2>Agent 调用日志</h2>
            <button class="button small" :disabled="loading" @click="loadAgentLogs">刷新</button>
          </div>
          <div class="table-wrap">
            <table>
              <thead>
                <tr><th>时间</th><th>Agent</th><th>Prompt</th><th>状态</th><th>耗时</th></tr>
              </thead>
              <tbody>
                <tr v-for="log in agentLogs" :key="log.callId">
                  <td>{{ formatTime(log.createdAt) }}</td>
                  <td>{{ log.agentType }}</td>
                  <td>{{ log.promptCode }} / {{ log.promptVersion }}</td>
                  <td>{{ log.status }}</td>
                  <td>{{ log.durationMillis }} ms</td>
                </tr>
              </tbody>
            </table>
          </div>
        </article>
      </section>
    </template>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useAdminAuthStore } from '../../stores/adminAuth'
import { adminApi } from '../../api/admin'
import { agentApi } from '../../api/agent'
import { ragApi } from '../../api/rag'
import { statisticsApi } from '../../api/statistics'

const auth = useAdminAuthStore()
const tabs = [
  { key: 'prompts', label: 'Prompt 管理' },
  { key: 'knowledge', label: '知识库' },
  { key: 'logs', label: '日志与统计' }
]
const agentTypes = ['DIAGNOSTICIAN', 'PLANNER', 'LESSON_GENERATOR', 'EXERCISE_GENERATOR', 'REVIEWER']

const activeTab = ref('prompts')
const loading = ref(false)
const error = ref('')
const notice = ref('')
const statistics = ref(null)
const prompts = ref([])
const selectedPrompt = ref(null)
const promptVersions = ref([])
const chunks = ref([])
const retrievedChunks = ref([])
const retrieveKeywords = ref('chain_rule')
const operationLogs = ref([])
const agentLogs = ref([])

const loginForm = reactive({ username: 'admin', password: 'agentstudy' })
const promptForm = reactive({
  code: '',
  agentType: 'LESSON_GENERATOR',
  version: 'v1',
  name: '',
  systemPrompt: '',
  userPromptTemplate: ''
})
const chunkForm = reactive({
  id: '',
  chapter: '',
  title: '',
  content: '',
  tagsText: ''
})
const logFilters = reactive({
  action: '',
  targetType: '',
  targetId: '',
  operator: ''
})

const weakPointBars = computed(() => {
  const items = statistics.value?.weakPoints || []
  const max = Math.max(...items.map((item) => item.count), 1)
  return items.slice(0, 5).map((item) => ({
    ...item,
    percent: Math.max(8, Math.round((item.count / max) * 100))
  }))
})

const agentDurationBars = computed(() => {
  const items = statistics.value?.agentCalls?.byAgentType || []
  const max = Math.max(...items.map((item) => item.averageDurationMillis), 1)
  return items.map((item) => ({
    ...item,
    percent: Math.max(8, Math.round((item.averageDurationMillis / max) * 100))
  }))
})

onMounted(() => {
  if (auth.loggedIn) {
    loadAll()
  }
})

async function run(action, successMessage = '') {
  loading.value = true
  error.value = ''
  notice.value = ''
  try {
    await action()
    if (successMessage) notice.value = successMessage
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}

async function login() {
  await run(async () => {
    await auth.login(loginForm.username, loginForm.password)
    await loadAll()
  }, '登录成功')
}

function logout() {
  auth.logout()
  prompts.value = []
  chunks.value = []
  operationLogs.value = []
  agentLogs.value = []
  statistics.value = null
}

async function loadAll() {
  await Promise.all([
    loadStatistics(),
    loadPrompts(),
    loadChunks(),
    loadOperationLogs(),
    loadAgentLogs()
  ])
}

async function loadStatistics() {
  statistics.value = await statisticsApi.dashboard()
}

async function loadPrompts() {
  prompts.value = await agentApi.listPrompts()
  if (!selectedPrompt.value && prompts.value.length) {
    selectPrompt(prompts.value[0])
  }
}

function selectPrompt(prompt) {
  selectedPrompt.value = prompt
  Object.assign(promptForm, {
    code: prompt.code,
    agentType: prompt.agentType,
    version: prompt.version,
    name: prompt.name,
    systemPrompt: prompt.systemPrompt,
    userPromptTemplate: prompt.userPromptTemplate
  })
  loadPromptVersions()
}

async function savePrompt() {
  await run(async () => {
    const saved = await agentApi.upsertPrompt(auth.token, promptForm.code, {
      agentType: promptForm.agentType,
      version: promptForm.version,
      name: promptForm.name,
      systemPrompt: promptForm.systemPrompt,
      userPromptTemplate: promptForm.userPromptTemplate
    })
    selectedPrompt.value = saved
    await loadPrompts()
    await loadPromptVersions()
    await loadOperationLogs()
  }, 'Prompt 已保存')
}

async function loadPromptVersions() {
  if (!promptForm.code || !auth.token) return
  promptVersions.value = await adminApi.listPromptVersions(auth.token, promptForm.code)
}

async function activatePromptVersion(version) {
  await run(async () => {
    const activePrompt = await adminApi.activatePromptVersion(auth.token, promptForm.code, version.versionId)
    selectPrompt(activePrompt)
    await loadPrompts()
    await loadOperationLogs()
  }, 'Prompt 版本已启用')
}

async function loadChunks() {
  chunks.value = await ragApi.listChunks()
}

function selectChunk(chunk) {
  Object.assign(chunkForm, {
    id: chunk.id,
    chapter: chunk.chapter,
    title: chunk.title,
    content: chunk.content,
    tagsText: (chunk.tags || []).join(', ')
  })
}

function clearChunkForm() {
  Object.assign(chunkForm, { id: '', chapter: '', title: '', content: '', tagsText: '' })
}

async function saveChunk() {
  await run(async () => {
    const body = chunkPayload()
    if (chunkForm.id && chunks.value.some((chunk) => chunk.id === chunkForm.id)) {
      await adminApi.updateChunk(auth.token, chunkForm.id, body)
    } else {
      await adminApi.createChunk(auth.token, body)
    }
    await loadChunks()
    await loadOperationLogs()
  }, '知识切片已保存')
}

async function deleteChunk() {
  await run(async () => {
    await adminApi.deleteChunk(auth.token, chunkForm.id)
    clearChunkForm()
    await loadChunks()
    await loadOperationLogs()
  }, '知识切片已删除')
}

async function rebuildEmbedding() {
  await run(async () => {
    await adminApi.rebuildEmbedding(auth.token, chunkForm.id)
    await loadChunks()
    await loadOperationLogs()
  }, 'Embedding 已重新生成')
}

async function retrieveChunks() {
  await run(async () => {
    retrievedChunks.value = await ragApi.retrieve(splitText(retrieveKeywords.value), 5)
  })
}

function chunkPayload() {
  return {
    id: chunkForm.id || undefined,
    chapter: chunkForm.chapter,
    title: chunkForm.title,
    content: chunkForm.content,
    tags: splitText(chunkForm.tagsText)
  }
}

async function loadOperationLogs() {
  operationLogs.value = await adminApi.listOperationLogs(auth.token, { ...logFilters, limit: 30 })
}

async function loadAgentLogs() {
  agentLogs.value = await agentApi.listCallLogs({ limit: 30 })
}

function splitText(value) {
  return String(value || '')
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

function formatTime(value) {
  return value ? new Date(value).toLocaleString() : '-'
}

function formatMillis(value) {
  return `${Math.round(Number(value || 0))} ms`
}
</script>

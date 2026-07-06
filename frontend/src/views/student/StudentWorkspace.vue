<template>
  <section class="page">
    <header class="page-header">
      <div>
        <p class="eyebrow">Student Flow</p>
        <h1>个性化高数学习闭环</h1>
      </div>
      <div class="header-actions">
        <input v-model="studentName" class="input compact" placeholder="学生姓名" />
        <button class="button primary" :disabled="loading" @click="createSession">创建会话</button>
      </div>
    </header>

    <div v-if="error" class="alert danger">{{ error }}</div>
    <div v-if="notice" class="alert success">{{ notice }}</div>

    <section class="toolbar">
      <div>
        <span class="muted">当前会话</span>
        <strong>{{ session?.sessionId || '尚未创建' }}</strong>
      </div>
      <div class="toolbar-actions">
        <input v-model="sessionIdInput" class="input compact id-input" placeholder="输入 sessionId" />
        <button class="button" :disabled="loading || !sessionIdInput" @click="loadSession">载入</button>
        <button class="button" :disabled="loading" @click="refreshSessions">最近会话</button>
      </div>
    </section>

    <section class="status-strip">
      <div>
        <span>学生</span>
        <strong>{{ session?.studentName || '-' }}</strong>
      </div>
      <div>
        <span>当前阶段</span>
        <strong>{{ currentStepMeta.label }}</strong>
      </div>
      <div>
        <span>状态</span>
        <strong>{{ session?.status || '-' }}</strong>
      </div>
      <div>
        <span>下一步</span>
        <strong>{{ session?.nextAction || '-' }}</strong>
      </div>
    </section>

    <section v-if="!session" class="empty-state hero-empty">
      <div>
        <strong>准备开始一次高数学习会话</strong>
        <span>创建会话后，诊断、计划、讲义、练习和复习结果会按阶段展示。</span>
      </div>
      <div class="tag-row">
        <span v-for="step in steps" :key="step.key" class="tag">{{ step.label }}</span>
      </div>
    </section>

    <template v-else>
      <section class="pipeline-steps" aria-label="学习流程">
        <button
          v-for="step in steps"
          :key="step.key"
          class="pipeline-step"
          :class="{ active: activeStep === step.key, done: isStepDone(step.key) }"
          @click="activeStep = step.key"
        >
          <span class="step-index">{{ step.key }}</span>
          <span>
            <strong>{{ step.label }}</strong>
            <small>{{ step.description }}</small>
          </span>
        </button>
      </section>

      <section class="learning-workbench">
        <article class="work-panel active-step-panel">
          <template v-if="activeStep === 1">
            <div class="panel-title">
              <div class="title-group">
                <span class="step-index">1</span>
                <h2>诊断题</h2>
              </div>
              <button class="button small" :disabled="loading" @click="generateDiagnosis">生成诊断题</button>
            </div>

            <div v-if="questions.length" class="question-list">
              <div v-for="question in questions" :key="question.id" class="item-card">
                <div class="item-heading">
                  <strong>{{ question.topicName }}</strong>
                  <span>难度 {{ question.difficulty }}</span>
                </div>
                <p>{{ question.stem }}</p>
                <label v-for="option in question.options" :key="option.key" class="option-row">
                  <input v-model="diagnosisAnswers[question.id]" type="radio" :name="question.id" :value="option.key" />
                  <span>{{ option.key }}. {{ option.text }}</span>
                </label>
              </div>
              <button class="button primary" :disabled="loading" @click="submitDiagnosis">提交并诊断</button>
            </div>
            <div v-else class="empty-state compact-state">
              <strong>等待生成诊断题</strong>
              <span>当前会话已就绪，可以先生成一组高数基础诊断题。</span>
            </div>

            <div v-if="diagnosisResult" class="result-block">
              <strong>诊断得分 {{ diagnosisResult.correctCount }} / {{ diagnosisResult.totalCount }}</strong>
              <div class="tag-row">
                <span v-for="weakPoint in diagnosisResult.weakPoints" :key="weakPoint.code" class="tag warn">
                  {{ weakPoint.name }}
                </span>
              </div>
            </div>
          </template>

          <template v-if="activeStep === 2">
            <div class="panel-title">
              <div class="title-group">
                <span class="step-index">2</span>
                <h2>三天学习计划</h2>
              </div>
              <button class="button small" :disabled="loading" @click="generatePlan">生成计划</button>
            </div>
            <div v-if="plan?.days?.length" class="timeline">
              <div v-for="day in plan.days" :key="day.day" class="item-card timeline-card">
                <div class="item-heading">
                  <strong>Day {{ day.day }} · {{ day.goal }}</strong>
                  <span>{{ (day.concepts || []).join(' / ') }}</span>
                </div>
                <ul class="compact-list">
                  <li v-for="suggestion in day.practiceSuggestions" :key="suggestion">{{ suggestion }}</li>
                </ul>
              </div>
            </div>
            <div v-else class="empty-state compact-state">
              <strong>暂无学习计划</strong>
              <span>提交诊断后，可以根据薄弱点生成三天计划。</span>
            </div>
          </template>

          <template v-if="activeStep === 3">
            <div class="panel-title">
              <div class="title-group">
                <span class="step-index">3</span>
                <h2>RAG 微讲义</h2>
              </div>
              <button class="button small" :disabled="loading" @click="generateLesson">生成讲义</button>
            </div>
            <div v-if="lesson" class="lesson-grid">
              <article class="markdown-view" v-html="renderedLesson"></article>
              <div class="side-list">
                <strong>召回切片</strong>
                <div v-for="chunk in lesson.retrievedChunks" :key="chunk.id" class="mini-card">
                  <span>{{ chunk.title }}</span>
                  <small>{{ chunk.chapter }} · {{ Number(chunk.score).toFixed(2) }}</small>
                </div>
              </div>
            </div>
            <div v-else class="empty-state compact-state">
              <strong>暂无微讲义</strong>
              <span>生成后会展示 Markdown 讲义、数学公式和召回知识切片。</span>
            </div>
          </template>

          <template v-if="activeStep === 4">
            <div class="panel-title">
              <div class="title-group">
                <span class="step-index">4</span>
                <h2>练习判卷</h2>
              </div>
              <div class="button-row">
                <button class="button small" :disabled="loading" @click="generateExercises">出题</button>
                <button class="button small primary" :disabled="!exercises.length || loading" @click="submitExercises">提交判卷</button>
              </div>
            </div>
            <div v-if="exercises.length" class="exercise-grid">
              <div v-for="question in exercises" :key="question.id" class="item-card">
                <div class="item-heading">
                  <strong>{{ question.knowledgePoint }}</strong>
                  <span>{{ question.id }}</span>
                </div>
                <p>{{ question.stem }}</p>
                <div class="symbol-toolbar">
                  <button
                    v-for="shortcut in mathShortcuts"
                    :key="shortcut.label"
                    class="symbol-chip"
                    type="button"
                    @click="insertExpression(question.id, shortcut.value)"
                  >
                    {{ shortcut.label }}
                  </button>
                </div>
                <input v-model="exerciseAnswers[question.id]" class="input" placeholder="输入表达式，如 2*x*cos(x^2)" />
              </div>
            </div>
            <div v-else class="empty-state compact-state">
              <strong>暂无练习题</strong>
              <span>生成后可使用快捷符号填写表达式并自动判卷。</span>
            </div>
            <div v-if="exerciseResult" class="result-block">
              <strong>正确率 {{ exerciseResult.correctCount }} / {{ exerciseResult.totalCount }}</strong>
              <span>错误率 {{ Math.round(exerciseResult.errorRate * 100) }}%</span>
              <div class="table-wrap">
                <table>
                  <thead>
                    <tr><th>题目</th><th>结果</th><th>标准答案</th><th>说明</th></tr>
                  </thead>
                  <tbody>
                    <tr v-for="result in exerciseResult.results" :key="result.questionId">
                      <td>{{ result.questionId }}</td>
                      <td>{{ result.correct ? '正确' : '需订正' }}</td>
                      <td>{{ result.standardAnswer }}</td>
                      <td>{{ result.detail }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </template>

          <template v-if="activeStep === 5">
            <div class="panel-title">
              <div class="title-group">
                <span class="step-index">5</span>
                <h2>复习或结业</h2>
              </div>
              <div class="button-row">
                <button class="button small" :disabled="loading" @click="generateReview">生成结果</button>
                <button class="button small" :disabled="loading" @click="loadAttempts">提交记录</button>
              </div>
            </div>
            <div v-if="review" class="result-block review-result">
              <strong>{{ review.status }}</strong>
              <p>{{ review.message }}</p>
              <div class="tag-row">
                <span v-for="suggestion in review.suggestions" :key="suggestion" class="tag">{{ suggestion }}</span>
              </div>
            </div>
            <div v-else class="empty-state compact-state">
              <strong>暂无复习结论</strong>
              <span>练习判卷后会形成复习建议或结业结果。</span>
            </div>
            <div v-if="attempts.length" class="table-wrap">
              <table>
                <thead>
                  <tr><th>提交时间</th><th>正确数</th><th>总题数</th><th>错误率</th></tr>
                </thead>
                <tbody>
                  <tr v-for="attempt in attempts" :key="attempt.attemptId">
                    <td>{{ formatTime(attempt.submittedAt) }}</td>
                    <td>{{ attempt.correctCount }}</td>
                    <td>{{ attempt.totalCount }}</td>
                    <td>{{ Math.round(attempt.errorRate * 100) }}%</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </template>
        </article>

        <aside class="agent-console">
          <div class="panel-title">
            <h2>多智能体工作台</h2>
            <button class="button tiny" type="button" @click="clearLogs">清空</button>
          </div>
          <div ref="consoleBody" class="agent-console-body">
            <div v-for="log in agentLogs" :key="log.id" class="agent-log" :class="log.type">
              <span>{{ log.time }}</span>
              <strong>[{{ log.role }}]</strong>
              <p>{{ log.message }}</p>
            </div>
          </div>
        </aside>
      </section>
    </template>

    <section v-if="recentSessions.length" class="section-band">
      <div class="section-heading">
        <h2>最近会话</h2>
      </div>
      <div class="session-list">
        <button v-for="item in recentSessions" :key="item.sessionId" class="session-row" @click="selectSession(item)">
          <strong>{{ item.studentName }}</strong>
          <span>{{ item.sessionId }}</span>
          <em>{{ item.status }} / Step {{ item.currentStep }}</em>
        </button>
      </div>
    </section>
  </section>
</template>

<script setup>
import { computed, nextTick, reactive, ref, watch } from 'vue'
import MarkdownIt from 'markdown-it'
import katex from 'katex'
import { learnApi } from '../../api/learn'

const markdown = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: true
})

markdown.renderer.rules.text = (tokens, idx) => renderMathText(tokens[idx].content)

const steps = [
  { key: 1, label: '诊断', description: '定位薄弱点' },
  { key: 2, label: '计划', description: '生成三天安排' },
  { key: 3, label: '讲义', description: 'RAG 微讲义' },
  { key: 4, label: '练习', description: '表达式判卷' },
  { key: 5, label: '复习', description: '复习或结业' }
]

const mathShortcuts = [
  { label: 'x^2', value: 'x^2' },
  { label: 'sin(x)', value: 'sin(x)' },
  { label: 'cos(x)', value: 'cos(x)' },
  { label: 'e^x', value: 'e^x' },
  { label: 'ln(x)', value: 'ln(x)' },
  { label: '()', value: '()' }
]

const studentName = ref('Alice')
const sessionIdInput = ref('')
const session = ref(null)
const activeStep = ref(1)
const questions = ref([])
const diagnosisAnswers = reactive({})
const diagnosisResult = ref(null)
const plan = ref(null)
const lesson = ref(null)
const exercises = ref([])
const exerciseAnswers = reactive({})
const exerciseResult = ref(null)
const review = ref(null)
const attempts = ref([])
const recentSessions = ref([])
const loading = ref(false)
const error = ref('')
const notice = ref('')
const agentLogs = ref([
  buildLog('系统', '前端学习流程已就绪，等待创建或载入会话。', 'system')
])
const consoleBody = ref(null)

const currentStepMeta = computed(() => {
  return steps.find((step) => step.key === activeStep.value) || steps[0]
})

const renderedLesson = computed(() => {
  return lesson.value?.lessonMarkdown ? markdown.render(lesson.value.lessonMarkdown) : ''
})

watch(
  () => agentLogs.value.length,
  async () => {
    await nextTick()
    if (consoleBody.value) {
      consoleBody.value.scrollTop = consoleBody.value.scrollHeight
    }
  }
)

async function run(action, successMessage = '') {
  loading.value = true
  error.value = ''
  notice.value = ''
  try {
    await action()
    if (successMessage) notice.value = successMessage
  } catch (err) {
    error.value = err.message
    addAgentLog('系统', err.message, 'error')
  } finally {
    loading.value = false
  }
}

async function createSession() {
  await run(async () => {
    addAgentLog('系统', `正在为 ${studentName.value || 'anonymous'} 创建学习会话。`, 'system')
    session.value = await learnApi.createSession(studentName.value)
    sessionIdInput.value = session.value.sessionId
    resetFlow()
    activeStep.value = 1
    addAgentLog('系统', `会话创建完成：${session.value.sessionId}`, 'success')
  }, '学习会话已创建')
}

async function loadSession() {
  await run(async () => {
    addAgentLog('系统', `正在载入会话：${sessionIdInput.value}`, 'system')
    session.value = await learnApi.getSession(sessionIdInput.value)
    resetFlow()
    syncActiveStep()
    addAgentLog('系统', `会话已载入，当前状态：${session.value.status}`, 'success')
  }, '会话已载入')
}

async function refreshSessions() {
  await run(async () => {
    recentSessions.value = await learnApi.listSessions({ limit: 10 })
    addAgentLog('系统', `已刷新最近 ${recentSessions.value.length} 条学习会话。`, 'system')
  })
}

function selectSession(item) {
  sessionIdInput.value = item.sessionId
  loadSession()
}

async function generateDiagnosis() {
  await run(async () => {
    activeStep.value = 1
    addAgentLog('诊断官', '正在生成高数基础诊断题。')
    const data = await learnApi.generateDiagnosis(session.value.sessionId)
    questions.value = data.questions || []
    addAgentLog('诊断官', `已生成 ${questions.value.length} 道诊断题。`, 'success')
  }, '诊断题已生成')
}

async function submitDiagnosis() {
  await run(async () => {
    addAgentLog('诊断官', '正在分析答题结果并识别薄弱点。')
    const answers = questions.value.map((question) => ({
      questionId: question.id,
      selectedOption: diagnosisAnswers[question.id]
    }))
    diagnosisResult.value = await learnApi.submitDiagnosis(session.value.sessionId, answers)
    session.value = await learnApi.getSession(session.value.sessionId)
    const weakNames = (diagnosisResult.value.weakPoints || []).map((item) => item.name).join('、') || '暂无明显薄弱点'
    addAgentLog('诊断官', `诊断完成，薄弱点：${weakNames}。`, 'success')
    activeStep.value = 2
  }, '诊断已提交')
}

async function generatePlan() {
  await run(async () => {
    activeStep.value = 2
    addAgentLog('规划官', '正在根据薄弱点生成三天学习计划。')
    const data = await learnApi.generatePlan(session.value.sessionId)
    plan.value = data.plan
    session.value = await learnApi.getSession(session.value.sessionId)
    addAgentLog('规划官', `已生成 ${(plan.value?.days || []).length} 天学习安排。`, 'success')
  }, '学习计划已生成')
}

async function generateLesson() {
  await run(async () => {
    activeStep.value = 3
    addAgentLog('RAG', '正在检索知识库并组织微讲义。')
    lesson.value = await learnApi.generateLesson(session.value.sessionId)
    session.value = await learnApi.getSession(session.value.sessionId)
    addAgentLog('生成官', `微讲义已生成，召回 ${(lesson.value.retrievedChunks || []).length} 条知识切片。`, 'success')
  }, '微讲义已生成')
}

async function generateExercises() {
  await run(async () => {
    activeStep.value = 4
    addAgentLog('练习官', '正在围绕讲义生成表达式练习题。')
    const data = await learnApi.generateExercises(session.value.sessionId)
    exercises.value = data.questions || []
    session.value = await learnApi.getSession(session.value.sessionId)
    addAgentLog('练习官', `已生成 ${exercises.value.length} 道练习题。`, 'success')
  }, '练习题已生成')
}

async function submitExercises() {
  await run(async () => {
    addAgentLog('判卷引擎', '正在调用表达式判卷逻辑。')
    const answers = exercises.value.map((question) => ({
      questionId: question.id,
      answerExpression: exerciseAnswers[question.id] || ''
    }))
    exerciseResult.value = await learnApi.submitExercises(session.value.sessionId, answers)
    session.value = await learnApi.getSession(session.value.sessionId)
    addAgentLog('判卷引擎', `判卷完成，错误率 ${Math.round(exerciseResult.value.errorRate * 100)}%。`, 'success')
  }, '练习已判卷')
}

async function generateReview() {
  await run(async () => {
    activeStep.value = 5
    addAgentLog('复习官', '正在生成复习建议或结业结论。')
    review.value = await learnApi.generateReview(session.value.sessionId)
    session.value = await learnApi.getSession(session.value.sessionId)
    addAgentLog('复习官', `复习结果：${review.value.status}。`, 'success')
  }, '复习结果已生成')
}

async function loadAttempts() {
  await run(async () => {
    attempts.value = await learnApi.listExerciseAttempts(session.value.sessionId)
    addAgentLog('系统', `已载入 ${attempts.value.length} 条练习提交记录。`, 'system')
  })
}

function resetFlow() {
  questions.value = []
  diagnosisResult.value = null
  plan.value = null
  lesson.value = null
  exercises.value = []
  exerciseResult.value = null
  review.value = null
  attempts.value = []
  Object.keys(diagnosisAnswers).forEach((key) => delete diagnosisAnswers[key])
  Object.keys(exerciseAnswers).forEach((key) => delete exerciseAnswers[key])
}

function syncActiveStep() {
  const nextStep = Number(session.value?.currentStep || 1)
  activeStep.value = Math.min(Math.max(nextStep, 1), 5)
}

function isStepDone(stepKey) {
  return Number(session.value?.currentStep || 1) > stepKey
}

function insertExpression(questionId, value) {
  const current = exerciseAnswers[questionId] || ''
  exerciseAnswers[questionId] = `${current}${value}`
}

function addAgentLog(role, message, type = 'info') {
  agentLogs.value.push(buildLog(role, message, type))
}

function buildLog(role, message, type = 'info') {
  return {
    id: `${Date.now()}-${Math.random().toString(16).slice(2)}`,
    role,
    message,
    type,
    time: new Date().toLocaleTimeString()
  }
}

function clearLogs() {
  agentLogs.value = [buildLog('系统', '日志已清空，继续等待学习流程事件。', 'system')]
}

function renderMathText(text) {
  const pattern = /\$\$([\s\S]+?)\$\$|\$([^$\n]+?)\$|\\\(([\s\S]+?)\\\)|\\\[([\s\S]+?)\\\]/g
  let result = ''
  let lastIndex = 0
  let match

  while ((match = pattern.exec(text)) !== null) {
    result += markdown.utils.escapeHtml(text.slice(lastIndex, match.index))
    const expression = match[1] || match[2] || match[3] || match[4]
    const displayMode = Boolean(match[1] || match[4])
    result += katex.renderToString(expression.trim(), {
      throwOnError: false,
      displayMode
    })
    lastIndex = match.index + match[0].length
  }

  result += markdown.utils.escapeHtml(text.slice(lastIndex))
  return result
}

function formatTime(value) {
  return value ? new Date(value).toLocaleString() : '-'
}
</script>

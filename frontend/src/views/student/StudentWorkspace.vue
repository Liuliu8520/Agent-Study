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
        <span>步骤</span>
        <strong>{{ session?.currentStep || '-' }}</strong>
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

    <section class="workflow">
      <article class="work-panel">
        <div class="panel-title">
          <span class="step-index">1</span>
          <h2>诊断题</h2>
          <button class="button small" :disabled="!session || loading" @click="generateDiagnosis">生成</button>
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
          <button class="button primary" :disabled="loading" @click="submitDiagnosis">提交诊断</button>
        </div>

        <div v-if="diagnosisResult" class="result-block">
          <strong>{{ diagnosisResult.correctCount }} / {{ diagnosisResult.totalCount }}</strong>
          <div class="tag-row">
            <span v-for="weakPoint in diagnosisResult.weakPoints" :key="weakPoint.code" class="tag warn">
              {{ weakPoint.name }}
            </span>
          </div>
        </div>
      </article>

      <article class="work-panel">
        <div class="panel-title">
          <span class="step-index">2</span>
          <h2>三天计划</h2>
          <button class="button small" :disabled="!session || loading" @click="generatePlan">生成</button>
        </div>
        <div v-if="plan?.days?.length" class="timeline">
          <div v-for="day in plan.days" :key="day.day" class="item-card">
            <div class="item-heading">
              <strong>Day {{ day.day }} · {{ day.goal }}</strong>
              <span>{{ (day.concepts || []).join(' / ') }}</span>
            </div>
            <ul class="compact-list">
              <li v-for="suggestion in day.practiceSuggestions" :key="suggestion">{{ suggestion }}</li>
            </ul>
          </div>
        </div>
      </article>

      <article class="work-panel wide">
        <div class="panel-title">
          <span class="step-index">3</span>
          <h2>RAG 微讲义</h2>
          <button class="button small" :disabled="!session || loading" @click="generateLesson">生成</button>
        </div>
        <div v-if="lesson" class="lesson-grid">
          <pre class="markdown-view">{{ lesson.lessonMarkdown }}</pre>
          <div class="side-list">
            <strong>召回切片</strong>
            <div v-for="chunk in lesson.retrievedChunks" :key="chunk.id" class="mini-card">
              <span>{{ chunk.title }}</span>
              <small>{{ chunk.chapter }} · {{ Number(chunk.score).toFixed(2) }}</small>
            </div>
          </div>
        </div>
      </article>

      <article class="work-panel wide">
        <div class="panel-title">
          <span class="step-index">4</span>
          <h2>练习判卷</h2>
          <div class="button-row">
            <button class="button small" :disabled="!session || loading" @click="generateExercises">出题</button>
            <button class="button small" :disabled="!exercises.length || loading" @click="submitExercises">提交</button>
          </div>
        </div>
        <div v-if="exercises.length" class="exercise-grid">
          <div v-for="question in exercises" :key="question.id" class="item-card">
            <div class="item-heading">
              <strong>{{ question.knowledgePoint }}</strong>
              <span>{{ question.id }}</span>
            </div>
            <p>{{ question.stem }}</p>
            <input v-model="exerciseAnswers[question.id]" class="input" placeholder="输入表达式，如 2*x*cos(x^2)" />
          </div>
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
      </article>

      <article class="work-panel wide">
        <div class="panel-title">
          <span class="step-index">5</span>
          <h2>复习或结业</h2>
          <div class="button-row">
            <button class="button small" :disabled="!session || loading" @click="generateReview">生成结果</button>
            <button class="button small" :disabled="!session || loading" @click="loadAttempts">提交记录</button>
          </div>
        </div>
        <div v-if="review" class="result-block">
          <strong>{{ review.status }}</strong>
          <p>{{ review.message }}</p>
          <div class="tag-row">
            <span v-for="suggestion in review.suggestions" :key="suggestion" class="tag">{{ suggestion }}</span>
          </div>
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
      </article>
    </section>

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
import { reactive, ref } from 'vue'
import { learnApi } from '../../api/learn'

const studentName = ref('Alice')
const sessionIdInput = ref('')
const session = ref(null)
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

async function createSession() {
  await run(async () => {
    session.value = await learnApi.createSession(studentName.value)
    sessionIdInput.value = session.value.sessionId
    resetFlow()
  }, '学习会话已创建')
}

async function loadSession() {
  await run(async () => {
    session.value = await learnApi.getSession(sessionIdInput.value)
    resetFlow()
  }, '会话已载入')
}

async function refreshSessions() {
  await run(async () => {
    recentSessions.value = await learnApi.listSessions({ limit: 10 })
  })
}

function selectSession(item) {
  sessionIdInput.value = item.sessionId
  loadSession()
}

async function generateDiagnosis() {
  await run(async () => {
    const data = await learnApi.generateDiagnosis(session.value.sessionId)
    questions.value = data.questions || []
  }, '诊断题已生成')
}

async function submitDiagnosis() {
  await run(async () => {
    const answers = questions.value.map((question) => ({
      questionId: question.id,
      selectedOption: diagnosisAnswers[question.id]
    }))
    diagnosisResult.value = await learnApi.submitDiagnosis(session.value.sessionId, answers)
    session.value = await learnApi.getSession(session.value.sessionId)
  }, '诊断已提交')
}

async function generatePlan() {
  await run(async () => {
    const data = await learnApi.generatePlan(session.value.sessionId)
    plan.value = data.plan
    session.value = await learnApi.getSession(session.value.sessionId)
  }, '学习计划已生成')
}

async function generateLesson() {
  await run(async () => {
    lesson.value = await learnApi.generateLesson(session.value.sessionId)
    session.value = await learnApi.getSession(session.value.sessionId)
  }, '微讲义已生成')
}

async function generateExercises() {
  await run(async () => {
    const data = await learnApi.generateExercises(session.value.sessionId)
    exercises.value = data.questions || []
    session.value = await learnApi.getSession(session.value.sessionId)
  }, '练习题已生成')
}

async function submitExercises() {
  await run(async () => {
    const answers = exercises.value.map((question) => ({
      questionId: question.id,
      answerExpression: exerciseAnswers[question.id] || ''
    }))
    exerciseResult.value = await learnApi.submitExercises(session.value.sessionId, answers)
    session.value = await learnApi.getSession(session.value.sessionId)
  }, '练习已判卷')
}

async function generateReview() {
  await run(async () => {
    review.value = await learnApi.generateReview(session.value.sessionId)
    session.value = await learnApi.getSession(session.value.sessionId)
  }, '复习结果已生成')
}

async function loadAttempts() {
  await run(async () => {
    attempts.value = await learnApi.listExerciseAttempts(session.value.sessionId)
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

function formatTime(value) {
  return value ? new Date(value).toLocaleString() : '-'
}
</script>

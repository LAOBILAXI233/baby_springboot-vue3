<template>
  <div class="cu-container">
    <div v-if="!previewUrl" class="cu-preview" @click="triggerUpload">
      <div class="cu-placeholder">
        <div class="cu-camera-icon-wrap">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#0C84FF" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
            <circle cx="12" cy="13" r="4"/>
          </svg>
        </div>
        <span class="cu-text">拍照或上传食物图片</span>
        <span class="cu-hint">点击进行 AI 膳食分析</span>
      </div>
    </div>

    <div v-else class="cu-preview-image">
      <img :src="previewUrl" alt="preview" />
      <button class="cu-retake-btn" @click="resetUpload">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="23 4 23 10 17 10"/>
          <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>
        </svg>
        重新选择
      </button>
    </div>

    <input
      ref="fileInput"
      type="file"
      accept="image/*"
      capture="environment"
      class="sr-only"
      @change="handleFileChange"
    />

    <button
      v-if="previewUrl && !analyzing && !result"
      class="cu-analyze-btn"
      @click="handleAnalyze"
    >
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/>
      </svg>
      AI 分析食物
    </button>

    <div v-if="analyzing || finishing" class="cu-analyzing">
      <div class="cu-analyzing-header">
        <span class="cu-analyzing-title">
          <svg v-if="progressValue < 100" class="cu-spin-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
          </svg>
          <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#4CAF50" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="20 6 9 17 4 12"/>
          </svg>
          {{ progressValue >= 100 ? '分析结束' : 'AI 正在分析🔎' }}
        </span>
      </div>

      <div v-if="barStyle === 'gradient'" class="cu-progress-wrap">
        <div class="cu-progress-bar">
          <div
            class="cu-progress-fill"
            :class="{ 'cu-progress-fill-done': progressValue >= 100 }"
            :style="{ width: progressValue + '%' }"
          ></div>
        </div>
        <span class="cu-progress-pct">{{ Math.round(progressValue) }}%</span>
      </div>

      <div v-else ref="asciiWrapRef" class="cu-progress-ascii-wrap">
        <span class="cu-progress-ascii" :class="{ 'cu-progress-ascii-done': progressValue >= 100 }">
          {{ asciiBar }}
        </span>
        <span v-if="progressValue < 100" class="cu-progress-spinner">{{ spinnerFrame }}</span>
        <span class="cu-progress-pct">{{ Math.round(progressValue) }}%</span>
      </div>

      <span class="cu-analyzing-tip">{{ progressValue >= 100 ? currentTip.reveal : currentTip.fake }}</span>
    </div>

    <div v-if="result" class="cu-result">
      <div class="cu-result-header">
        <div class="cu-result-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#0C84FF" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M18 8h1a4 4 0 0 1 0 8h-1"/><path d="M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8z"/><line x1="6" y1="1" x2="6" y2="4"/><line x1="10" y1="1" x2="10" y2="4"/><line x1="14" y1="1" x2="14" y2="4"/>
          </svg>
        </div>
        <div class="cu-result-info">
          <span class="cu-food-name">{{ result.foodName }}</span>
          <span class="cu-meal-type">{{ result.mealType }}</span>
        </div>
        <div class="cu-score" :class="scoreClass">{{ result.healthScore }}分</div>
      </div>

      <div class="cu-score-bar">
        <div class="cu-score-fill" :class="scoreClass" :style="{ width: result.healthScore + '%' }"></div>
      </div>

      <div class="cu-nutrients" v-if="result.nutrients && result.nutrients.length">
        <div class="cu-nutrients-header">
          <span class="cu-nutrients-title">📊 营养成分</span>
        </div>
        <div class="cu-nutrients-grid">
          <div
            v-for="n in result.nutrients"
            :key="n.name"
            class="cu-nutrient-card"
            :class="'nutrient-' + n.level"
          >
            <span class="cu-nutrient-name">{{ n.name }}</span>
            <span class="cu-nutrient-level-badge">{{ n.level }}</span>
          </div>
        </div>
      </div>

      <div class="cu-result-body">
        <p class="cu-ai-result">{{ result.aiResult }}</p>
        <p class="cu-suggestion">{{ result.suggestion }}</p>
      </div>

      <div class="cu-confidence-bold" v-if="result.confidence !== undefined">
        <span class="confidence-label-bold">AI 置信度：{{ result.confidence }}%</span>
      </div>

      <div v-if="result.feedback" class="ai-feedback" :class="feedbackClass">
        <span class="feedback-icon">{{ feedbackIcon }}</span>
        <span class="feedback-text">{{ result.feedback }}</span>
      </div>

      <div class="cu-result-actions">
        <button class="cu-result-btn cu-result-btn-primary" @click="resetUpload">再来一张</button>
        <button class="cu-result-btn cu-result-btn-community" @click="emit('postToCommunity', result)">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
          </svg>
          发到社区
        </button>
        <span class="cu-saved-hint">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#4CAF50" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="20 6 9 17 4 12"/>
          </svg>
          已自动保存
        </span>
      </div>
    </div>

    <div v-if="error" class="cu-error">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/>
      </svg>
      {{ error }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useUserStore } from '../stores/user'
import { analyzeFoodImage } from '../services/aiAnalyze'
import { getAiTips } from '../api/index'

const DEFAULT_TIPS = [
  { fake: '坐和放宽 正在尽力分析', reveal: '好了好了，不装了' },
  { fake: '海内存知己', reveal: '天涯共此时，但饭还是得自己吃' },
  { fake: '正在加载光线追踪', reveal: '醒醒，分析个饭哪用光线追踪' },
  { fake: '正在编译着色器', reveal: '想多了，食物又不发光' },
  { fake: '正在训练 4090 亿参数模型', reveal: '别做梦了，我们可没那么多参数' },
  { fake: '正在启动量子计算引擎', reveal: '认真的吗，量子计算机还没普及呢' },
  { fake: '正在连接国际空间站数据', reveal: '说实话，空间站可不管你吃了啥' },
  { fake: '正在部署核聚变反应堆', reveal: '不好意思，核聚变还没商用呢' },
  { fake: '正在调用暗物质分析模块', reveal: '实不相瞒，暗物质还没被分析明白呢' },
  { fake: '正在激活赛博朋克滤镜', reveal: '醒醒，现实世界没有滤镜' },
  { fake: '正在加载平行宇宙数据集', reveal: '别闹了，我们只活在当前宇宙' },
  { fake: '正在同步银河系营养数据库', reveal: '说真的，银河系还没建营养库' },
  { fake: '正在渲染 8K 食物全息投影', reveal: '想得美，全息投影还没发明呢' },
  { fake: '正在启动戴森球能源供应', reveal: '别做梦了，戴森球还在图纸上' },
  { fake: '千里之行，始于足下', reveal: '好了，分析完了，别感动了' },
]

const userStore = useUserStore()
const emit = defineEmits(['analyzed', 'postToCommunity'])

const fileInput = ref(null)
const previewUrl = ref('')
const selectedFile = ref(null)
const analyzing = ref(false)
const finishing = ref(false)
const result = ref(null)
const error = ref('')

const progressValue = ref(0)
const currentTip = ref(DEFAULT_TIPS[0])
const tips = ref([...DEFAULT_TIPS])

const BAR_STYLES = ['gradient', 'ascii', 'hash', 'block', 'dots']
const BAR_CHARS = {
  ascii: { fill: '█', empty: '░', left: '[', right: ']' },
  hash:  { fill: '#', empty: '-', left: '[', right: ']' },
  block: { fill: '■', empty: '□', left: '', right: '' },
  dots:  { fill: '●', empty: '○', left: '', right: '' },
}

const barStyle = ref('gradient')

const asciiWrapRef = ref(null)
const asciiCharCount = ref(30)

const CHAR_WIDTH_PX = 10

let resizeObserver = null

function updateCharCount() {
  if (!asciiWrapRef.value) return
  const wrapWidth = asciiWrapRef.value.clientWidth
  const pctWidth = 50
  const spinnerWidth = 24
  const gaps = 20
  const available = wrapWidth - pctWidth - spinnerWidth - gaps
  if (available > 0) {
    const chars = BAR_CHARS[barStyle.value] || BAR_CHARS.ascii
    const bracketExtra = (chars.left.length + chars.right.length) * CHAR_WIDTH_PX
    const count = Math.floor((available - bracketExtra) / CHAR_WIDTH_PX)
    asciiCharCount.value = Math.max(count, 10)
  }
}

onMounted(() => {
  resizeObserver = new ResizeObserver(updateCharCount)
})

onUnmounted(() => {
  stopProgress()
  resizeObserver?.disconnect()
})

watch(asciiWrapRef, (el) => {
  if (el) {
    resizeObserver?.observe(el)
    updateCharCount()
  }
})

const SPINNER_FRAMES = ['-', '\\', '|', '/']
const spinnerIndex = ref(0)
const spinnerFrame = computed(() => SPINNER_FRAMES[spinnerIndex.value])

let spinnerTimer = null

function startSpinner() {
  spinnerIndex.value = 0
  spinnerTimer = setInterval(() => {
    spinnerIndex.value = (spinnerIndex.value + 1) % SPINNER_FRAMES.length
  }, 150)
}

function stopSpinner() {
  clearInterval(spinnerTimer)
  spinnerTimer = null
}

const asciiBar = computed(() => {
  const chars = BAR_CHARS[barStyle.value] || BAR_CHARS.ascii
  const total = asciiCharCount.value
  const filled = Math.round(progressValue.value / 100 * total)
  const empty = total - filled
  return chars.left + chars.fill.repeat(filled) + chars.empty.repeat(empty) + chars.right
})

let progressTimer = null
let tipTimer = null
let tipIndex = 0
let stage = 'idle'

function startProgress() {
  stage = 'start'
  progressValue.value = 0
  currentTip.value = tips.value[0]
  tipIndex = 0
  barStyle.value = BAR_STYLES[Math.floor(Math.random() * BAR_STYLES.length)]
  if (barStyle.value !== 'gradient') startSpinner()

  const startTime = Date.now()
  progressTimer = setInterval(() => {
    if (stage === 'start') {
      const elapsed = Date.now() - startTime
      const t = Math.min(elapsed / 3000, 1)
      progressValue.value = t * 5
      if (t >= 1) {
        stage = 'middle'
        runMiddleStage()
      }
    }
  }, 50)

  tipTimer = setInterval(() => {
    tipIndex = (tipIndex + 1) % tips.value.length
    currentTip.value = tips.value[tipIndex]
  }, 4000)
}

function runMiddleStage() {
  clearInterval(progressTimer)
  progressTimer = setInterval(() => {
    if (stage !== 'middle') return
    const min = 5
    const max = 85
    const target = min + Math.random() * (max - min)
    progressValue.value = target
  }, 800)
}

function finishProgress() {
  stage = 'end'
  clearInterval(progressTimer)
  clearInterval(tipTimer)
  stopSpinner()
  progressValue.value = 100
  currentTip.value = { fake: '', reveal: currentTip.value.reveal || '分析完成！' }

  setTimeout(() => {
    finishing.value = false
    progressValue.value = 0
  }, 5000)
}

function stopProgress() {
  stage = 'idle'
  clearInterval(progressTimer)
  clearInterval(tipTimer)
  stopSpinner()
  progressTimer = null
  tipTimer = null
}

const scoreClass = computed(() => {
  if (!result.value) return ''
  const s = result.value.healthScore
  if (s >= 80) return 'score-high'
  if (s >= 60) return 'score-mid'
  return 'score-low'
})

const confidenceClass = computed(() => {
  if (!result.value) return ''
  const c = result.value.confidence
  if (c >= 70) return 'confidence-high'
  if (c >= 40) return 'confidence-mid'
  return 'confidence-low'
})

const feedbackClass = computed(() => {
  if (!result.value) return ''
  const c = result.value.confidence
  if (c < 40) return 'feedback-low'
  if (c < 70) return 'feedback-mid'
  return ''
})

const feedbackIcon = computed(() => {
  if (!result.value) return ''
  const c = result.value.confidence
  if (c < 40) return '🤖'
  if (c < 70) return '⚠️'
  return ''
})

function triggerUpload() {
  fileInput.value?.click()
}

function handleFileChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  selectedFile.value = file
  previewUrl.value = URL.createObjectURL(file)
  result.value = null
  error.value = ''
  e.target.value = ''
}

function resetUpload() {
  stopProgress()
  previewUrl.value = ''
  selectedFile.value = null
  result.value = null
  error.value = ''
  analyzing.value = false
  finishing.value = false
  progressValue.value = 0
}

async function loadDynamicTips() {
  try {
    const res = await getAiTips()
    const dynamic = res.data
    if (Array.isArray(dynamic) && dynamic.length > 0) {
      tips.value = dynamic
    }
  } catch { /* ignore, keep current */ }
}

async function handleAnalyze() {
  if (!selectedFile.value) return
  const userId = userStore.getUserId()
  if (!userId) {
    error.value = '请先登录后再使用 AI 分析'
    return
  }

  analyzing.value = true
  error.value = ''
  startProgress()
  loadDynamicTips()

  try {
    const res = await analyzeFoodImage(selectedFile.value, userId)
    result.value = res
    analyzing.value = false
    finishing.value = true
    finishProgress()
    emit('analyzed', res)
  } catch (err) {
    analyzing.value = false
    stopProgress()
    error.value = err.response?.data?.message || err.message || '分析失败，请重试'
  }
}
</script>

<style scoped>
.cu-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cu-preview {
  width: 100%;
  aspect-ratio: 1 / 1;
  max-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: var(--radius-sm);
  background: var(--bg-card);
  border: 2px dashed var(--border-medium);
  transition: border-color 0.2s, background 0.2s;
}

.cu-preview:active {
  border-color: #0C84FF;
  background: #F0F7FF;
}

.cu-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.cu-camera-icon-wrap {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: #E3F2FD;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cu-text {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.cu-hint {
  font-size: 12px;
  color: var(--text-secondary);
}

.cu-preview-image {
  width: 100%;
  position: relative;
  border-radius: var(--radius-sm);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.cu-preview-image img {
  width: 100%;
  display: block;
}

.cu-retake-btn {
  position: absolute;
  bottom: 12px;
  right: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: var(--radius-full);
  background: rgba(0, 0, 0, 0.6);
  color: #FFFFFF;
  font-size: 13px;
  font-weight: 600;
  backdrop-filter: blur(8px);
  transition: background 0.2s;
}

.cu-retake-btn:active {
  background: rgba(0, 0, 0, 0.8);
}

.cu-analyze-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 14px;
  border-radius: 15px;
  background: linear-gradient(135deg, #0C84FF, #0D7AEC);
  color: #FFFFFF;
  font-size: 16px;
  font-weight: 700;
  transition: background 0.15s, transform 0.1s;
}

.cu-analyze-btn:active {
  background: #0D7AEC;
  transform: scale(0.97);
}

.cu-analyzing {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 20px 16px;
  background: #E3F2FD;
  border-radius: var(--radius-sm);
  border: 1px solid #BBDEFB;
}

.cu-analyzing-header {
  display: flex;
  align-items: center;
  justify-content: center;
}

.cu-analyzing-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 700;
  color: #0D47A1;
}

.cu-spin-icon {
  animation: cu-spin 1.2s linear infinite;
}

@keyframes cu-spin {
  to { transform: rotate(360deg); }
}

.cu-progress-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}

.cu-progress-bar {
  flex: 1;
  height: 8px;
  background: #BBDEFB;
  border-radius: 4px;
  overflow: hidden;
}

.cu-progress-fill {
  height: 100%;
  border-radius: 4px;
  background: linear-gradient(90deg, #0C84FF, #42A5F5);
  transition: width 0.7s cubic-bezier(0.4, 0, 0.2, 1);
}

.cu-progress-fill-done {
  background: linear-gradient(90deg, #4CAF50, #66BB6A);
  transition: width 0.15s ease-out;
}

.cu-progress-pct {
  font-size: 14px;
  font-weight: 800;
  color: #0D47A1;
  min-width: 38px;
  text-align: right;
  font-variant-numeric: tabular-nums;
}

.cu-progress-ascii-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}

.cu-progress-ascii {
  flex: 1;
  font-family: 'Courier New', Consolas, monospace;
  font-size: 14px;
  font-weight: 700;
  color: #0C84FF;
  letter-spacing: 1px;
  white-space: pre;
  line-height: 1;
}

.cu-progress-ascii-done {
  color: #4CAF50;
}

.cu-progress-spinner {
  font-family: 'Courier New', Consolas, monospace;
  font-size: 16px;
  font-weight: 900;
  color: #0C84FF;
  min-width: 14px;
  text-align: center;
}

.cu-analyzing-tip {
  font-size: 13px;
  color: #1565C0;
  font-weight: 500;
  text-align: center;
  min-height: 20px;
  animation: tipFade 0.4s ease;
}

@keyframes tipFade {
  from { opacity: 0; transform: translateY(4px); }
  to { opacity: 1; transform: translateY(0); }
}

.cu-result {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
}

.cu-result-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 16px 12px;
}

.cu-result-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-sm);
  background: #E3F2FD;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.cu-result-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.cu-food-name {
  font-size: 17px;
  font-weight: 700;
  color: var(--text-primary);
}

.cu-meal-type {
  font-size: 11px;
  color: var(--text-muted);
  font-weight: 500;
}

.cu-score {
  font-size: 28px;
  font-weight: 800;
  flex-shrink: 0;
  letter-spacing: -1px;
}

.score-high { color: #4CAF50; }
.score-mid { color: #FF9800; }
.score-low { color: #E53935; }

.cu-score-bar {
  height: 4px;
  background: #E0E0E0;
  margin: 0 16px;
  border-radius: 2px;
  overflow: hidden;
}

.cu-score-fill {
  height: 100%;
  border-radius: 2px;
  transition: width 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.score-high.cu-score-fill { background: #4CAF50; }
.score-mid.cu-score-fill { background: #FF9800; }
.score-low.cu-score-fill { background: #E53935; }

.cu-nutrients {
  padding: 14px 16px;
  background: linear-gradient(135deg, #F8F9FA 0%, #FFFFFF 100%);
  border-top: 1px solid var(--border-light);
}

.cu-nutrients-header {
  margin-bottom: 10px;
}

.cu-nutrients-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-primary);
}

.cu-nutrients-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 8px;
}

.cu-nutrient-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 10px 8px;
  border-radius: var(--radius-md);
  background: #FFFFFF;
  border: 1.5px solid var(--border-light);
  transition: transform 0.15s, box-shadow 0.15s;
}

.cu-nutrient-card:active {
  transform: scale(0.96);
}

.nutrient-高 {
  border-color: #A5D6A7;
  background: linear-gradient(135deg, #E8F5E9 0%, #F1F8E9 100%);
}

.nutrient-中 {
  border-color: #FFE0B2;
  background: linear-gradient(135deg, #FFF3E0 0%, #FFF8E1 100%);
}

.nutrient-低 {
  border-color: #CFD8DC;
  background: linear-gradient(135deg, #F5F5F5 0%, #FAFAFA 100%);
}

.cu-nutrient-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  text-align: center;
}

.cu-nutrient-level-badge {
  font-size: 11px;
  font-weight: 800;
  padding: 2px 10px;
  border-radius: var(--radius-full);
  text-transform: uppercase;
}

.nutrient-高 .cu-nutrient-level-badge {
  color: #2E7D32;
  background: #C8E6C9;
}

.nutrient-中 .cu-nutrient-level-badge {
  color: #E65100;
  background: #FFE0B2;
}

.nutrient-低 .cu-nutrient-level-badge {
  color: #546E7A;
  background: #CFD8DC;
}

.cu-result-body {
  padding: 12px 16px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.cu-ai-result {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.5;
}

.cu-suggestion {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
  padding: 10px 12px;
  background: #FFF8E1;
  border-radius: var(--radius-sm);
  border-left: 3px solid #FFC107;
}

.cu-result-actions {
  display: flex;
  gap: 8px;
  padding: 0 16px 16px;
}

.cu-result-btn {
  flex: 1;
  padding: 12px;
  border-radius: 15px;
  font-size: 14px;
  font-weight: 600;
  transition: background 0.15s, transform 0.1s;
}

.cu-result-btn-primary {
  background: #0C84FF;
  color: #FFFFFF;
}

.cu-result-btn-primary:active {
  background: #0D7AEC;
  transform: scale(0.97);
}

.cu-result-btn-community {
  background: #FF9800;
  color: #FFFFFF;
}

.cu-result-btn-community:active {
  background: #F57C00;
  transform: scale(0.97);
}

.cu-confidence {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px 0;
}

.cu-confidence-bold {
  padding: 10px 16px 4px;
}

.confidence-label-bold {
  font-size: 15px;
  font-weight: 800;
  color: var(--text-primary);
}

.confidence-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  white-space: nowrap;
}

.confidence-bar {
  flex: 1;
  height: 8px;
  background: #E0E0E0;
  border-radius: 4px;
  overflow: hidden;
}

.confidence-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.confidence-high { background: #4CAF50; }
.confidence-mid { background: #FF9800; }
.confidence-low { background: #E53935; }

.confidence-value {
  font-size: 12px;
  font-weight: 800;
  color: var(--text-primary);
  min-width: 36px;
  text-align: right;
  font-variant-numeric: tabular-nums;
}

.ai-feedback {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  margin: 8px 16px 0;
  border-radius: var(--radius-md);
  font-size: 13px;
  font-weight: 600;
  line-height: 1.4;
}

.feedback-icon { font-size: 16px; }

.feedback-low {
  background: #FFEBEE;
  color: #C62828;
  border: 1px solid #EF9A9A;
}

.feedback-mid {
  background: #FFF8E1;
  color: #F57C00;
  border: 1px solid #FFE082;
}

.cu-saved-hint {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px;
  border-radius: 15px;
  font-size: 14px;
  font-weight: 600;
  color: #4CAF50;
  background: #E8F5E9;
}

.cu-error {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 14px;
  background: #FFEBEE;
  border-radius: var(--radius-sm);
  font-size: 13px;
  color: #C62828;
  font-weight: 500;
}
</style>

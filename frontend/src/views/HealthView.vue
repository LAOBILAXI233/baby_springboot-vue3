<template>
  <div class="page-container">
    <h1 class="page-title">🍎 健康分析</h1>
    <p class="page-subtitle">拍摄食物照片，获取专业营养分析</p>

    <section class="health-analyze">
      <CameraUpload @analyzed="loadRecords" @post-to-community="onPostToCommunity" />
    </section>

    <section class="health-records" v-if="records.length > 0">
      <div class="record-header">
        <h2 class="section-title">历史记录</h2>
        <div class="batch-actions" v-if="selectedIds.length > 0">
          <span class="batch-count">已选 {{ selectedIds.length }} 条</span>
          <button class="batch-delete-btn" @click="handleBatchDelete">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="3 6 5 6 21 6"/>
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
            </svg>
            批量删除
          </button>
        </div>
      </div>
      <div class="record-list">
        <div v-for="r in records" :key="r.id" class="record-card lg-card" @click="openDetail(r)">
          <input
            type="checkbox"
            class="record-checkbox"
            :checked="selectedIds.includes(r.id)"
            @click.stop
            @change="toggleSelect(r.id)"
          />
          <img v-if="r.imageUrl" :src="r.imageUrl" class="record-img" />
          <div class="record-info">
            <div class="record-top">
              <span class="record-name">{{ r.foodName }}</span>
              <span class="record-score" :class="scoreClass(r.healthScore)">
                {{ r.healthScore }}分
              </span>
            </div>
            <span class="record-type bmi-badge bmi-badge-yellow">{{ r.mealType }}</span>
            <span class="record-time">{{ formatTime(r.createTime) }}</span>
          </div>
          <button class="delete-btn noselect" @click.stop="handleDelete(r.id)">
            <span class="text">删除</span>
            <span class="icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path d="M24 20.188l-8.315-8.209 8.2-8.282-3.697-3.697-8.212 8.318-8.31-8.203-3.666 3.666 8.321 8.24-8.206 8.313 3.666 3.666 8.237-8.318 8.285 8.203z"></path></svg>
            </span>
          </button>
        </div>
      </div>
    </section>

    <!-- 详情弹窗 -->
    <Teleport to="body">
      <Transition name="modal-fade">
        <div v-if="detailRecord" class="modal-backdrop" @click.self="detailRecord = null">
          <div class="modal-panel detail-panel">
            <div class="detail-header">
              <h3 class="detail-title">{{ detailRecord.foodName }}</h3>
              <button class="detail-close" @click="detailRecord = null">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                </svg>
              </button>
            </div>
            <img v-if="detailRecord.imageUrl" :src="detailRecord.imageUrl" class="detail-img" />
            <div class="detail-meta">
              <span class="detail-badge" :class="scoreClass(detailRecord.healthScore)">{{ detailRecord.healthScore }}分</span>
              <span class="detail-badge bmi-badge-yellow">{{ detailRecord.mealType }}</span>
              <span class="detail-time">{{ formatTime(detailRecord.createTime) }}</span>
            </div>
            <div class="detail-confidence-section" v-if="detailRecord.confidence !== undefined && detailRecord.confidence !== null">
              <span class="detail-confidence-label-bold">AI 置信度：{{ detailRecord.confidence }}%</span>
              <div class="detail-feedback" :class="feedbackClass(detailRecord.confidence)">
                <span class="detail-feedback-icon">{{ feedbackIcon(detailRecord.confidence) }}</span>
                <span>{{ detailRecord.feedback }}</span>
              </div>
            </div>
            <div class="detail-section">
              <span class="detail-section-title">分析结果</span>
              <p class="detail-text">{{ detailRecord.aiResult }}</p>
            </div>
            <div class="detail-section">
              <span class="detail-section-title">建议</span>
              <p class="detail-text detail-suggestion">{{ detailRecord.suggestion }}</p>
            </div>
            <div class="detail-nutrients" v-if="detailRecord.nutrients && detailRecord.nutrients.length">
              <span class="detail-section-title">📊 营养成分</span>
              <div class="detail-nutrient-grid">
                <div v-for="n in detailRecord.nutrients" :key="n.name" class="detail-nutrient-card" :class="'nutrient-' + n.level">
                  <span class="detail-nutrient-name">{{ n.name }}</span>
                  <span class="detail-nutrient-level-badge">{{ n.level }}</span>
                </div>
              </div>
            </div>
            <div class="detail-actions">
              <button class="detail-btn detail-btn-community" @click="confirmPostFromDetail">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                </svg>
                发布到社区
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- 发布确认弹窗（MIUI样式） -->
    <Teleport to="body">
      <Transition name="miui-modal">
        <div v-if="showConfirmDialog" class="miui-overlay" @click.self="closeConfirmDialog">
          <div class="miui-dialog">
            <div class="miui-dialog-header">
              <span class="miui-dialog-title">发布到社区</span>
              <button class="miui-dialog-close" @click="closeConfirmDialog">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                </svg>
              </button>
            </div>
            <div class="miui-dialog-body">
              <div class="miui-preview-card" v-if="pendingPostResult?.imageUrl">
                <img :src="pendingPostResult.imageUrl" class="miui-preview-img" />
              </div>
              <div class="miui-post-info">
                <div class="miui-info-row">
                  <span class="miui-info-label">食物名称</span>
                  <span class="miui-info-value">{{ pendingPostResult?.foodName }}</span>
                </div>
                <div class="miui-info-row">
                  <span class="miui-info-label">健康评分</span>
                  <span class="miui-info-score" :class="scoreClass(pendingPostResult?.healthScore || 0)">{{ pendingPostResult?.healthScore }}分</span>
                </div>
                <div class="miui-info-row" v-if="pendingPostResult?.confidence !== undefined">
                  <span class="miui-info-label">AI置信度</span>
                  <span class="miui-info-value">{{ pendingPostResult.confidence }}%</span>
                </div>
              </div>
              <div class="miui-post-content-preview">
                <span class="miui-preview-label">发布内容预览</span>
                <p class="miui-preview-text">AI识别结果：{{ pendingPostResult?.foodName }}
健康评分：{{ pendingPostResult?.healthScore }}分
{{ pendingPostResult?.aiResult }}
{{ pendingPostResult?.suggestion }}</p>
              </div>
            </div>
            <div class="miui-dialog-footer">
              <button class="miui-btn miui-btn-cancel" @click="closeConfirmDialog">取消</button>
              <button class="miui-btn miui-btn-confirm" @click="executePostToCommunity" :disabled="isPosting">
                {{ isPosting ? '发布中...' : '确认发布' }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <div v-if="records.length === 0 && userStore.isLoggedIn" class="empty-state lg-glass">
      <span class="empty-icon">📋</span>
      <span class="empty-text">暂无分析记录</span>
      <span class="empty-hint">拍摄食物照片开始分析吧</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'
import { getFoodRecords, deleteFoodRecord, batchDeleteFoodRecords, createCommunityPost } from '../api/index'
import CameraUpload from '../components/CameraUpload.vue'

const router = useRouter()

const userStore = useUserStore()
const records = ref([])
const selectedIds = ref([])
const detailRecord = ref(null)
const showConfirmDialog = ref(false)
const pendingPostResult = ref(null)
const isPosting = ref(false)

function scoreClass(s) {
  if (s >= 80) return 'score-high'
  if (s >= 60) return 'score-mid'
  return 'score-low'
}

function formatTime(t) {
  if (!t) return ''
  return new Date(t).toLocaleString('zh-CN')
}

function toggleSelect(id) {
  const idx = selectedIds.value.indexOf(id)
  if (idx >= 0) {
    selectedIds.value.splice(idx, 1)
  } else {
    selectedIds.value.push(id)
  }
}

async function loadRecords() {
  if (!userStore.isLoggedIn) return
  try {
    const res = await getFoodRecords({ userId: userStore.getUserId(), page: 0, size: 20 })
    records.value = res.data?.content || []
    selectedIds.value = []
  } catch { /* ignore */ }
}

async function handleDelete(recordId) {
  if (!confirm('确定要删除这条记录吗？')) return
  try {
    await deleteFoodRecord(recordId, userStore.getUserId())
    records.value = records.value.filter(r => r.id !== recordId)
    selectedIds.value = selectedIds.value.filter(id => id !== recordId)
  } catch (err) {
    alert(err.response?.data?.message || '删除失败')
  }
}

async function handleBatchDelete() {
  if (selectedIds.value.length === 0) return
  if (!confirm(`确定要删除选中的 ${selectedIds.value.length} 条记录吗？`)) return
  try {
    await batchDeleteFoodRecords(selectedIds.value, userStore.getUserId())
    records.value = records.value.filter(r => !selectedIds.value.includes(r.id))
    selectedIds.value = []
  } catch (err) {
    alert(err.response?.data?.message || '批量删除失败')
  }
}

function onPostToCommunity(result) {
  const userId = userStore.getUserId()
  if (!userId) { alert('请先登录'); return }
  pendingPostResult.value = result
  showConfirmDialog.value = true
}

function confirmPostFromDetail() {
  if (!detailRecord.value) return
  const userId = userStore.getUserId()
  if (!userId) { alert('请先登录'); return }
  pendingPostResult.value = detailRecord.value
  showConfirmDialog.value = true
}

function closeConfirmDialog() {
  showConfirmDialog.value = false
  pendingPostResult.value = null
}

async function executePostToCommunity() {
  if (!pendingPostResult.value || isPosting.value) return
  const userId = userStore.getUserId()
  if (!userId) { alert('请先登录'); closeConfirmDialog(); return }
  const result = pendingPostResult.value
  const content = `AI识别结果：${result.foodName}\n健康评分：${result.healthScore}分\n${result.aiResult}\n${result.suggestion}`
  isPosting.value = true
  try {
    await createCommunityPost({
      userId,
      content,
      images: JSON.stringify([result.imageUrl || ''])
    })
    isPosting.value = false
    closeConfirmDialog()
    detailRecord.value = null
    if (confirm('发布成功！是否跳转到社区查看？')) {
      router.push('/community')
    }
  } catch (err) {
    isPosting.value = false
    alert(err.response?.data?.message || '发布失败')
  }
}

function openDetail(r) {
  detailRecord.value = r
}

function confidenceClass(c) {
  if (c >= 70) return 'confidence-high'
  if (c >= 40) return 'confidence-mid'
  return 'confidence-low'
}

function feedbackClass(c) {
  if (c < 40) return 'feedback-low'
  if (c < 70) return 'feedback-mid'
  return ''
}

function feedbackIcon(c) {
  if (c < 40) return '🤖'
  if (c < 70) return '⚠️'
  return ''
}

onMounted(loadRecords)
</script>

<style scoped>
.health-analyze {
  margin-bottom: 28px;
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.batch-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.batch-count {
  font-size: 13px;
  color: var(--text-muted);
}

.batch-delete-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border-radius: var(--radius-md);
  font-size: 13px;
  font-weight: 600;
  color: #E53935;
  background: #FFEBEE;
  transition: background 0.15s;
}

.batch-delete-btn:active {
  background: #EF9A9A;
}

.record-card {
  display: flex;
  gap: 12px;
  align-items: center;
}

.record-checkbox {
  height: 2rem;
  width: 2rem;
  margin: 5px;
  display: inline-block;
  appearance: none;
  position: relative;
  background-color: #F2ECFF;
  border-radius: 15%;
  cursor: pointer;
  overflow: hidden;
  flex-shrink: 0;
}

.record-checkbox::after {
  content: '';
  display: block;
  height: 1rem;
  width: .5rem;
  border-bottom: .31rem solid #a0ffe7;
  border-right: .31rem solid #a0ffe7;
  opacity: 0;
  transform: rotate(45deg) translate(-50%, -50%);
  position: absolute;
  top: 50%;
  left: 20%;
  transition: .25s ease;
}

.record-checkbox::before {
  content: '';
  display: block;
  height: 0;
  width: 0;
  background-color: #00C896;
  border-radius: 50%;
  opacity: .5;
  transform: translate(-50%, -50%);
  position: absolute;
  top: 50%;
  left: 50%;
  transition: .3s ease;
}

.record-checkbox:checked::before {
  height: 130%;
  width: 130%;
  opacity: 100%;
}

.record-checkbox:checked::after {
  opacity: 100%;
}

.record-img {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-sm);
  object-fit: cover;
  flex-shrink: 0;
}

.record-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.record-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.record-name {
  font-size: 15px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.record-score {
  font-size: 16px;
  font-weight: 800;
  flex-shrink: 0;
}

.score-high { color: #4CAF50; }
.score-mid { color: var(--yellow-primary); }
.score-low { color: var(--red-primary); }

.record-time {
  font-size: 11px;
  color: var(--text-muted);
}

/* ===== Delete button (hover expand) ===== */
.delete-btn {
  width: 150px;
  height: 50px;
  cursor: pointer;
  display: flex;
  align-items: center;
  background: #e62222;
  border: none;
  border-radius: 5px;
  box-shadow: 1px 1px 3px rgba(0,0,0,0.15);
  transition: background 200ms;
  flex-shrink: 0;
}

.delete-btn,
.delete-btn span {
  transition: 200ms;
}

.delete-btn .text {
  transform: translateX(35px);
  color: white;
  font-weight: bold;
}

.delete-btn .icon {
  position: absolute;
  border-left: 1px solid #c41b1b;
  transform: translateX(110px);
  height: 40px;
  width: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.delete-btn svg {
  width: 15px;
  fill: #eee;
}

.delete-btn:hover {
  background: #ff3636;
}

.delete-btn:hover .text {
  color: transparent;
}

.delete-btn:hover .icon {
  width: 150px;
  border-left: none;
  transform: translateX(0);
}

.delete-btn:focus {
  outline: none;
}

.delete-btn:active .icon svg {
  transform: scale(0.8);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 40px;
  border-radius: var(--radius-lg);
}

.empty-icon { font-size: 48px; }
.empty-text { font-size: 16px; font-weight: 600; }
.empty-hint { font-size: 12px; color: var(--text-muted); }

.record-card { cursor: pointer; }

.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  z-index: 200;
}

.modal-panel {
  width: 100%;
  max-width: 420px;
  max-height: 85vh;
  overflow-y: auto;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-title {
  font-size: 18px;
  font-weight: 700;
}

.detail-close {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  background: var(--bg-secondary);
}

.detail-close:active {
  background: var(--divider);
}

.detail-img {
  width: 100%;
  max-height: 200px;
  object-fit: cover;
  border-radius: var(--radius-md);
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.detail-badge {
  padding: 4px 10px;
  border-radius: var(--radius-full);
  font-size: 12px;
  font-weight: 700;
}

.detail-time {
  font-size: 12px;
  color: var(--text-muted);
}

.detail-confidence-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-confidence-label-bold {
  font-size: 14px;
  font-weight: 800;
  color: var(--text-primary);
}

.detail-feedback {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: var(--radius-md);
  font-size: 13px;
  font-weight: 600;
  line-height: 1.4;
}

.detail-feedback-icon { font-size: 16px; }

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

.detail-section {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.detail-section-title {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-secondary);
}

.detail-text {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.6;
}

.detail-suggestion {
  padding: 10px 12px;
  background: #FFF8E1;
  border-radius: var(--radius-sm);
  border-left: 3px solid #FFC107;
}

.detail-nutrients {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px;
  background: linear-gradient(135deg, #F8F9FA 0%, #FFFFFF 100%);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
}

.detail-nutrient-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(110px, 1fr));
  gap: 8px;
}

.detail-nutrient-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 10px 8px;
  border-radius: var(--radius-md);
  background: #FFFFFF;
  border: 1.5px solid var(--border-light);
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

.detail-nutrient-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  text-align: center;
}

.detail-nutrient-level-badge {
  font-size: 11px;
  font-weight: 800;
  padding: 2px 10px;
  border-radius: var(--radius-full);
  text-transform: uppercase;
}

.nutrient-高 .detail-nutrient-level-badge {
  color: #2E7D32;
  background: #C8E6C9;
}

.nutrient-中 .detail-nutrient-level-badge {
  color: #E65100;
  background: #FFE0B2;
}

.nutrient-低 .detail-nutrient-level-badge {
  color: #546E7A;
  background: #CFD8DC;
}

.detail-actions {
  display: flex;
  gap: 10px;
  padding-top: 6px;
}

.detail-btn {
  flex: 1;
  padding: 12px;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: background 0.15s, transform 0.1s;
}

.detail-btn-community {
  background: #FF9800;
  color: #FFFFFF;
}

.detail-btn-community:active {
  background: #F57C00;
  transform: scale(0.97);
}

/* MIUI确认弹窗样式 */
.miui-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  z-index: 300;
}

.miui-dialog {
  width: 100%;
  max-width: 380px;
  background: #FFFFFF;
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.2), 0 2px 8px rgba(0, 0, 0, 0.1);
}

.miui-dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 20px 14px;
  border-bottom: 0.5px solid #EEEEEE;
}

.miui-dialog-title {
  font-size: 17px;
  font-weight: 700;
  color: #212121;
  letter-spacing: 0.3px;
}

.miui-dialog-close {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9E9E9E;
  background: transparent;
  transition: background 0.2s, color 0.2s;
}

.miui-dialog-close:active {
  background: #F5F5F5;
  color: #616161;
}

.miui-dialog-body {
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  max-height: 55vh;
  overflow-y: auto;
}

.miui-preview-card {
  width: 100%;
  border-radius: 12px;
  overflow: hidden;
  background: #F5F5F5;
}

.miui-preview-img {
  width: 100%;
  max-height: 180px;
  object-fit: cover;
  display: block;
}

.miui-post-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 12px 14px;
  background: #FAFAFA;
  border-radius: 12px;
  border: 1px solid #F0F0F0;
}

.miui-info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.miui-info-label {
  font-size: 13px;
  color: #757575;
  font-weight: 500;
}

.miui-info-value {
  font-size: 14px;
  font-weight: 600;
  color: #212121;
}

.miui-info-score {
  font-size: 16px;
  font-weight: 800;
}

.miui-post-content-preview {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.miui-preview-label {
  font-size: 12px;
  font-weight: 700;
  color: #9E9E9E;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.miui-preview-text {
  font-size: 13px;
  color: #424242;
  line-height: 1.6;
  white-space: pre-wrap;
  padding: 10px 12px;
  background: #FFFDE7;
  border-radius: 10px;
  border-left: 3px solid #FFC107;
  word-break: break-all;
}

.miui-dialog-footer {
  display: flex;
  gap: 12px;
  padding: 14px 20px 18px;
  border-top: 0.5px solid #EEEEEE;
}

.miui-btn {
  flex: 1;
  padding: 12px 0;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 700;
  transition: all 0.2s ease;
  border: none;
  cursor: pointer;
}

.miui-btn-cancel {
  background: #F5F5F5;
  color: #616161;
}

.miui-btn-cancel:active {
  background: #E0E0E0;
  transform: scale(0.97);
}

.miui-btn-confirm {
  background: linear-gradient(135deg, #0C84FF, #0D6EFD);
  color: #FFFFFF;
  box-shadow: 0 2px 8px rgba(12, 132, 255, 0.3);
}

.miui-btn-confirm:active:not(:disabled) {
  background: linear-gradient(135deg, #0D6EFD, #0B5ED7);
  transform: scale(0.97);
  box-shadow: 0 1px 4px rgba(12, 132, 255, 0.3);
}

.miui-btn-confirm:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 过渡动画 */
.miui-modal-enter-active {
  transition: opacity 0.25s ease;
}

.miui-modal-enter-active .miui-dialog {
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1), opacity 0.25s ease;
}

.miui-modal-leave-active {
  transition: opacity 0.2s ease;
}

.miui-modal-leave-active .miui-dialog {
  transition: transform 0.2s cubic-bezier(0.55, 0, 1, 0.45), opacity 0.2s ease;
}

.miui-modal-enter-from {
  opacity: 0;
}

.miui-modal-enter-from .miui-dialog {
  transform: scale(0.88) translateY(16px);
  opacity: 0;
}

.miui-modal-leave-to {
  opacity: 0;
}

.miui-modal-leave-to .miui-dialog {
  transform: scale(0.95) translateY(8px);
  opacity: 0;
}

.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.2s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}
</style>

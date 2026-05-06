<template>
  <div class="page-container">
    <h1 class="page-title">⚙️ 管理员后台</h1>
    <p class="page-subtitle">系统管理与数据分析</p>

    <div class="admin-tabs">
      <button
        class="admin-tab"
        :class="{ active: activeTab === 'records' }"
        @click="activeTab = 'records'"
      >
        📋 分析记录
      </button>
      <button
        class="admin-tab"
        :class="{ active: activeTab === 'posts' }"
        @click="activeTab = 'posts'; loadPosts()"
      >
        💬 帖子管理
      </button>
      <button
        class="admin-tab"
        :class="{ active: activeTab === 'users' }"
        @click="activeTab = 'users'"
      >
        👥 用户管理
      </button>
      <button
        class="admin-tab"
        :class="{ active: activeTab === 'stats' }"
        @click="activeTab = 'stats'; loadStatistics()"
      >
        📊 数据统计
      </button>
    </div>

    <!-- 分析记录 -->
    <section v-if="activeTab === 'records'" class="admin-section">
      <div class="record-header">
        <h2 class="section-title">全部分析记录</h2>
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
        <div v-for="r in records" :key="r.id" class="record-card lg-card">
          <input
            type="checkbox"
            class="record-checkbox"
            :checked="selectedIds.includes(r.id)"
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
            <span class="record-user">用户ID: {{ r.userId }}</span>
            <span class="record-time">{{ formatTime(r.createTime) }}</span>
          </div>
          <button class="record-delete" @click="handleDelete(r.id)" title="删除记录">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="3 6 5 6 21 6"/>
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
            </svg>
          </button>
        </div>
      </div>
      <div v-if="records.length === 0" class="empty-state lg-glass">
        <span class="empty-icon">📋</span>
        <span class="empty-text">暂无分析记录</span>
      </div>
    </section>

    <!-- 帖子管理 -->
    <section v-if="activeTab === 'posts'" class="admin-section">
      <div class="record-header">
        <h2 class="section-title">全部社区帖子</h2>
      </div>
      <div class="post-list" v-if="posts.length > 0">
        <div v-for="post in posts" :key="post.id" class="admin-post-card lg-card">
          <div class="admin-post-header">
            <div class="admin-post-avatar" v-if="post.avatar" :style="{ backgroundImage: 'url(' + post.avatar + ')' }"></div>
            <div class="admin-post-avatar" v-else>{{ (post.nickname || post.username)?.[0] }}</div>
            <div class="admin-post-meta">
              <span class="admin-post-user">{{ post.nickname || post.username }}</span>
              <span class="admin-post-time">{{ formatTime(post.createTime) }}</span>
            </div>
          </div>
          <p class="admin-post-content">{{ post.content }}</p>
          <div class="admin-post-stats">
            <span>❤️ {{ post.likeCount }} · 💬 {{ post.commentCount }}</span>
          </div>
          <button class="record-delete" @click="handleDeletePost(post.id)" title="删除帖子">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
            </svg>
          </button>
        </div>
      </div>
      <div v-if="posts.length === 0" class="empty-state lg-glass">
        <span class="empty-icon">💬</span>
        <span class="empty-text">暂无帖子</span>
      </div>
    </section>

    <!-- 数据统计 -->
    <section v-if="activeTab === 'stats'" class="admin-section">
      <h2 class="section-title">📊 用户数据统计</h2>

      <div class="stats-overview">
        <div class="stat-card stat-card-blue">
          <span class="stat-card-value">{{ statistics.totalUsers || 0 }}</span>
          <span class="stat-card-label">总用户数</span>
        </div>
        <div class="stat-card stat-card-green">
          <span class="stat-card-value">{{ statistics.regularUserCount || 0 }}</span>
          <span class="stat-card-label">普通用户</span>
        </div>
        <div class="stat-card stat-card-purple">
          <span class="stat-card-value">{{ statistics.adminCount || 0 }}</span>
          <span class="stat-card-label">管理员</span>
        </div>
      </div>

      <div class="stats-charts">
        <div class="chart-container">
          <h3 class="chart-title">👶 宝宝性别分布</h3>
          <div class="chart-bars">
            <div class="chart-bar-item" v-for="(count, label) in (statistics.genderDistribution || {})" :key="label">
              <span class="chart-bar-label">{{ label }}</span>
              <div class="chart-bar-track">
                <div
                  class="chart-bar-fill"
                  :class="'bar-' + label"
                  :style="{ width: getBarWidth(count, statistics.genderDistribution) + '%' }"
                ></div>
              </div>
              <span class="chart-bar-value">{{ count }}</span>
            </div>
          </div>
        </div>

        <div class="chart-container">
          <h3 class="chart-title">📅 宝宝月龄分布</h3>
          <div class="chart-bars">
            <div class="chart-bar-item" v-for="(count, label) in (statistics.ageDistribution || {})" :key="label">
              <span class="chart-bar-label chart-bar-label-small">{{ label }}</span>
              <div class="chart-bar-track">
                <div
                  class="chart-bar-fill bar-age"
                  :style="{ width: getBarWidth(count, statistics.ageDistribution) + '%' }"
                ></div>
              </div>
              <span class="chart-bar-value">{{ count }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 用户管理 -->
    <section v-if="activeTab === 'users'" class="admin-section">
      <h2 class="section-title">用户列表</h2>
      <div class="user-list">
        <div v-for="u in users" :key="u.id" class="user-card lg-card">
          <div class="user-avatar" v-if="u.avatar" :style="{ backgroundImage: 'url(' + u.avatar + ')' }"></div>
          <div class="user-avatar user-avatar-letter" v-else>{{ (u.nickname || u.username)?.[0] }}</div>
          <div class="user-info">
            <span class="user-name">{{ u.nickname || u.username }}</span>
            <span class="user-meta">@{{ u.username }}</span>
            <span class="user-meta" v-if="u.phone">📱 {{ u.phone }}</span>
            <span class="user-meta" v-if="u.babyAge">👶 {{ u.babyAge }}个月 · {{ u.babyGender || '未设置' }}</span>
          </div>
          <div class="user-actions">
            <span class="user-role-badge" :class="u.role === 2 ? 'role-admin' : 'role-user'">
              {{ u.role === 2 ? '管理员' : '用户' }}
            </span>
            <button class="user-btn" @click="openEditUser(u)">编辑</button>
            <button class="user-btn user-btn-danger" @click="handleDeleteUser(u.id)">删除</button>
          </div>
        </div>
      </div>
      <div v-if="users.length === 0" class="empty-state lg-glass">
        <span class="empty-icon">👥</span>
        <span class="empty-text">暂无用户</span>
      </div>
    </section>

    <!-- 编辑用户弹窗 -->
    <Teleport to="body">
      <Transition name="modal-fade">
        <div v-if="showEditUserModal" class="modal-backdrop" @click.self="closeEditUser">
          <div class="modal-panel edit-user-modal">
            <h3 class="modal-title">✏️ 编辑用户</h3>
            <div class="modal-form">
              <div class="modal-field">
                <label class="modal-label">用户名</label>
                <input class="modal-input" :value="editUser.username" disabled />
              </div>
              <div class="modal-field">
                <label class="modal-label">新密码</label>
                <input v-model="editUser.newPassword" class="modal-input" type="password" placeholder="不修改则留空" />
              </div>
              <div class="modal-field">
                <label class="modal-label">角色</label>
                <select v-model="editUser.role" class="modal-select">
                  <option :value="1">用户</option>
                  <option :value="2">管理员</option>
                </select>
              </div>

              <div class="modal-divider"></div>
              <span class="modal-section-label">👶 宝宝信息</span>

              <div class="modal-field">
                <label class="modal-label">宝宝月龄（个月）</label>
                <input v-model="editUser.babyAge" class="modal-input" type="text" placeholder="例如：6" />
              </div>
              <div class="modal-field">
                <label class="modal-label">宝宝性别</label>
                <select v-model="editUser.babyGender" class="modal-select">
                  <option value="">未设置</option>
                  <option value="男">男宝</option>
                  <option value="女">女宝</option>
                </select>
              </div>

              <div class="modal-divider"></div>
              <span class="modal-section-label">🖼️ 头像管理</span>

              <div class="avatar-preview-wrap">
                <div class="avatar-preview" v-if="editUser.avatarPreview" :style="{ backgroundImage: 'url(' + editUser.avatarPreview + ')' }"></div>
                <div class="avatar-placeholder" v-else>暂无头像</div>
                <button class="avatar-upload-btn" @click="triggerAvatarUpload">上传头像</button>
              </div>
            </div>
            <div class="modal-actions">
              <button class="modal-btn modal-btn-cancel" @click="closeEditUser">取消</button>
              <button class="modal-btn modal-btn-save" @click="handleSaveUser">保存</button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <input ref="adminAvatarInput" type="file" accept="image/*" class="sr-only" @change="onAdminAvatarFile" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import {
  getFoodRecords, deleteFoodRecord, batchDeleteFoodRecords,
  listUsers, deleteUser, updateUserPassword, updateUserRole,
  getAllCommunityPosts, deleteCommunityPost,
  getUserStatistics, updateUserAvatar
} from '../api/index'

const userStore = useUserStore()
const activeTab = ref('records')
const records = ref([])
const users = ref([])
const posts = ref([])
const statistics = ref({})
const selectedIds = ref([])
const adminAvatarInput = ref(null)

const showEditUserModal = ref(false)
const editUser = ref({
  id: null, username: '', newPassword: '', role: 1,
  babyAge: '', babyGender: '', avatarPreview: '', avatarFile: null
})

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
  try {
    const res = await getFoodRecords({ userId: userStore.getUserId(), page: 0, size: 100 })
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

async function loadUsers() {
  try {
    const res = await listUsers()
    if (res.code === 200) {
      users.value = res.data || []
    }
  } catch { /* ignore */ }
}

function openEditUser(u) {
  editUser.value = {
    id: u.id,
    username: u.username,
    newPassword: '',
    role: u.role || 1,
    babyAge: u.babyAge || '',
    babyGender: u.babyGender || '',
    avatarPreview: u.avatar || '',
    avatarFile: null
  }
  showEditUserModal.value = true
}

async function handleSaveUser() {
  const currentUserId = userStore.getUserId()
  try {
    if (editUser.value.newPassword) {
      await updateUserPassword(editUser.value.id, editUser.value.newPassword, currentUserId)
    }
    await updateUserRole(editUser.value.id, editUser.value.role, currentUserId)

    if (editUser.value.avatarFile) {
      const reader = new FileReader()
      reader.onloadend = async () => {
        const base64 = reader.result
        try { await updateUserAvatar(editUser.value.id, base64, currentUserId) } catch (e) { console.error('Avatar update failed:', e) }
      }
      reader.readAsDataURL(editUser.value.avatarFile)
    }

    showEditUserModal.value = false
    loadUsers()
  } catch (err) {
    alert(err.response?.data?.message || '保存失败')
  }
}

function triggerAvatarUpload() {
  adminAvatarInput.value?.click()
}

function onAdminAvatarFile(e) {
  const file = e.target.files?.[0]
  if (!file) return
  editUser.value.avatarFile = file
  const reader = new FileReader()
  reader.onload = () => { editUser.value.avatarPreview = reader.result }
  reader.readAsDataURL(file)
  e.target.value = ''
}

async function loadPosts() {
  try {
    const res = await getAllCommunityPosts(userStore.getUserId())
    if (res.code === 200) {
      posts.value = res.data || []
    }
  } catch (err) {
    console.error('Load posts failed:', err)
    posts.value = []
  }
}

async function loadStatistics() {
  try {
    const res = await getUserStatistics()
    console.log('Statistics response:', res)
    if (res.code === 200) {
      statistics.value = res.data || {}
      console.log('Statistics data:', statistics.value)
    } else {
      console.warn('Statistics API returned non-200:', res)
      statistics.value = {
        totalUsers: 0,
        regularUserCount: 0,
        adminCount: 0,
        genderDistribution: { '男': 0, '女': 0, '未知': 0 },
        ageDistribution: { '0-6个月': 0, '7-12个月': 0, '13-24个月': 0, '25-36个月': 0, '36个月以上': 0 }
      }
    }
  } catch (err) {
    console.error('Load statistics failed:', err)
    statistics.value = {
      totalUsers: 0,
      regularUserCount: 0,
      adminCount: 0,
      genderDistribution: { '男': 0, '女': 0, '未知': 0 },
      ageDistribution: { '0-6个月': 0, '7-12个月': 0, '13-24个月': 0, '25-36个月': 0, '36个月以上': 0 }
    }
  }
}

async function handleDeletePost(postId) {
  if (!confirm('确定要删除这条帖子吗？')) return
  try {
    await deleteCommunityPost(postId, userStore.getUserId())
    posts.value = posts.value.filter(p => p.id !== postId)
  } catch (err) {
    alert(err.response?.data?.message || '删除失败')
  }
}

function getBarWidth(count, distribution) {
  if (!distribution) return 0
  const values = Object.values(distribution)
  const max = Math.max(...values, 1)
  return Math.round((count / max) * 100)
}

onMounted(() => {
  loadRecords()
  loadUsers()
})

async function handleDeleteUser(id) {
  if (!confirm('确定要删除该用户吗？此操作不可恢复！')) return
  try {
    await deleteUser(id, userStore.getUserId())
    users.value = users.value.filter(u => u.id !== id)
  } catch (err) {
    alert(err.response?.data?.message || '删除失败')
  }
}

onMounted(() => {
  loadRecords()
  loadUsers()
})
</script>

<style scoped>
.admin-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
}

.admin-tab {
  padding: 8px 16px;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary);
  background: var(--bg-secondary);
  transition: all 0.15s;
}

.admin-tab.active {
  color: #fff;
  background: #0C84FF;
}

.admin-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.record-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
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

.record-user {
  font-size: 11px;
  color: var(--text-muted);
}

.record-time {
  font-size: 11px;
  color: var(--text-muted);
}

.record-delete {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  background: transparent;
  flex-shrink: 0;
  transition: background 0.15s, color 0.15s;
}

.record-delete:active {
  background: #FFEBEE;
  color: #E53935;
}

.user-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.user-card {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 12px;
}

.user-avatar {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-full);
  background: #0C84FF;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  flex-shrink: 0;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.user-name {
  font-size: 15px;
  font-weight: 600;
}

.user-meta {
  font-size: 12px;
  color: var(--text-muted);
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.user-role-badge {
  padding: 4px 10px;
  border-radius: var(--radius-full);
  font-size: 11px;
  font-weight: 700;
}

.role-admin {
  background: #E3F2FD;
  color: #0C84FF;
}

.role-user {
  background: #F5F5F5;
  color: var(--text-muted);
}

.user-btn {
  padding: 5px 10px;
  border-radius: var(--radius-md);
  font-size: 12px;
  font-weight: 600;
  color: #0C84FF;
  background: #E3F2FD;
  transition: background 0.15s;
}

.user-btn-danger {
  color: #E53935;
  background: #FFEBEE;
}

.user-btn:active {
  background: #BBDEFB;
}

.user-btn-danger:active {
  background: #EF9A9A;
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
  max-width: 360px;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.modal-title {
  font-size: 18px;
  font-weight: 700;
  text-align: center;
}

.modal-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.modal-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.modal-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}

.modal-input,
.modal-select {
  padding: 10px 12px;
  border-radius: var(--radius-md);
  border: 1px solid var(--divider);
  background: var(--bg-secondary);
  font-size: 14px;
  color: var(--text-primary);
}

.modal-input:disabled {
  opacity: 0.6;
}

.modal-actions {
  display: flex;
  gap: 10px;
}

.modal-btn {
  flex: 1;
  padding: 12px;
  border-radius: var(--radius-md);
  font-size: 15px;
  font-weight: 700;
  transition: background 0.15s;
}

.modal-btn-cancel {
  background: var(--bg-secondary);
  color: var(--text-secondary);
}

.modal-btn-save {
  background: #0C84FF;
  color: #fff;
}

.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.2s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

/* Post Management */
.post-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.admin-post-card {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px;
}

.admin-post-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.admin-post-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
  color: #fff;
  background-color: #0C84FF;
  flex-shrink: 0;
}

.admin-post-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.admin-post-user {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.admin-post-time {
  font-size: 11px;
  color: var(--text-muted);
}

.admin-post-content {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
  margin: 0;
  padding-right: 30px;
}

.admin-post-stats {
  font-size: 12px;
  color: var(--text-muted);
}

/* Statistics */
.stats-overview {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.stat-card {
  padding: 18px 14px;
  border-radius: var(--radius-md);
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.stat-card-blue { background: linear-gradient(135deg, #E3F2FD, #BBDEFB); }
.stat-card-green { background: linear-gradient(135deg, #E8F5E9, #C8E6C9); }
.stat-card-purple { background: linear-gradient(135deg, #F3E5F5, #E1BEE7); }

.stat-card-value {
  font-size: 28px;
  font-weight: 800;
  color: var(--text-primary);
}

.stat-card-label {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
}

.stats-charts {
  display: grid;
  gap: 16px;
}

.chart-container {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: 16px;
  border: 1px solid var(--border-light);
}

.chart-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 14px 0;
}

.chart-bars {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.chart-bar-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.chart-bar-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  min-width: 60px;
  text-align-last: justify;
}

.chart-bar-label-small {
  font-size: 11px;
  min-width: 80px;
}

.chart-bar-track {
  flex: 1;
  height: 24px;
  background: #F0F0F0;
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.chart-bar-fill {
  height: 100%;
  border-radius: var(--radius-sm);
  transition: width 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
  min-width: 4px;
}

.bar-男 { background: linear-gradient(90deg, #42A5F5, #1976D2); }
.bar-女 { background: linear-gradient(90deg, #F06292, #E91E63); }
.bar-未知 { background: linear-gradient(90deg, #9E9E9E, #757575); }
.bar-age { background: linear-gradient(90deg, #66BB6A, #43A047); }

.chart-bar-value {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-primary);
  min-width: 28px;
  text-align: right;
}

/* Edit User Modal Enhancements */
.edit-user-modal {
  max-width: 400px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-divider {
  height: 1px;
  background: var(--border-light);
  margin: 14px 0 10px;
}

.modal-section-label {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-primary);
  display: block;
  margin-bottom: 8px;
}

.avatar-preview-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar-preview {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background-size: cover;
  background-position: center;
  border: 2px solid var(--border-medium);
  flex-shrink: 0;
}

.avatar-placeholder {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: #F0F0F0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  color: var(--text-muted);
  flex-shrink: 0;
}

.avatar-upload-btn {
  padding: 8px 14px;
  border-radius: var(--radius-md);
  font-size: 13px;
  font-weight: 600;
  background: #E3F2FD;
  color: #1976D2;
  transition: background 0.15s;
}

.avatar-upload-btn:active {
  background: #BBDEFB;
}

.user-avatar-letter {
  background-color: #0C84FF !important;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  color: #fff;
}
</style>

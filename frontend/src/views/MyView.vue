<template>
  <div class="my-page">
    <template v-if="!userStore.isLoggedIn">
      <div class="welcome-area">
        <div class="welcome-hero">
          <div class="hero-badge">
            <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="#0C84FF" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
            </svg>
          </div>
          <h1 class="hero-title">宝贝健康助手</h1>
          <p class="hero-desc">智能识别 · 科学喂养 · 健康成长</p>
        </div>

        <button class="launch-btn noselect" @click="openLoginModal">
          <svg height="24" width="24" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path d="M0 0h24v24H0z" fill="none"></path><path d="M5 13c0-5.088 2.903-9.436 7-11.182C16.097 3.564 19 7.912 19 13c0 .823-.076 1.626-.22 2.403l1.94 1.832a.5.5 0 0 1 .095.603l-2.495 4.575a.5.5 0 0 1-.793.114l-2.234-2.234a1 1 0 0 0-.707-.293H9.414a1 1 0 0 0-.707.293l-2.234 2.234a.5.5 0 0 1-.793-.114l-2.495-4.575a.5.5 0 0 1 .095-.603l1.94-1.832C5.077 14.626 5 13.823 5 13zm1.476 6.696l.817-.817A3 3 0 0 1 9.414 18h5.172a3 3 0 0 1 2.121.879l.817.817.982-1.8-1.1-1.04a2 2 0 0 1-.593-1.82c.124-.664.187-1.345.187-2.036 0-3.87-1.995-7.3-5-8.96C8.995 5.7 7 9.13 7 13c0 .691.063 1.372.187 2.037a2 2 0 0 1-.593 1.82l-1.1 1.039.982 1.8zM12 13a2 2 0 1 1 0-4 2 2 0 0 1 0 4z" fill="currentColor"></path></svg>
          <span>立即登录</span>
        </button>
      </div>
    </template>

    <template v-else>
      <div class="profile-area">
        <div class="status-overview">
          <div class="status-main-card">
            <div class="avatar-wrap" @click="openEditModal">
              <div class="avatar-img" v-if="displayAvatar" :style="{ backgroundImage: 'url(' + displayAvatar + ')' }"></div>
              <div class="avatar-letter" v-else>{{ avatarLetter }}</div>
              <div class="avatar-edit-badge">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#FFFFFF" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/>
                </svg>
              </div>
            </div>
            <div class="status-main-text">
              <h2 class="status-main-title">{{ displayNickname }}</h2>
              <p class="status-main-sub">{{ userUUID }}</p>
            </div>
          </div>

          <div class="status-stats-col" @click="goAdminIfAdmin">
            <div class="status-stat-card" :class="{ 'stat-card-clickable': userStore.isAdmin() }">
              <span class="stat-label">账户权限</span>
              <span class="stat-value stat-role" :class="roleClass">{{ userRole }}</span>
              <span v-if="userStore.isAdmin()" class="stat-hint">点击进入后台</span>
            </div>
          </div>
        </div>

        <div class="miui-card" @click="openEditModal">
          <div class="miui-card-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#FFFFFF" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/>
            </svg>
          </div>
          <div class="miui-card-text">
            <span class="miui-card-title">编辑个人信息</span>
            <span class="miui-card-sub">修改昵称与头像</span>
          </div>
          <span class="miui-card-arrow">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.6)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="9 18 15 12 9 6"/>
            </svg>
          </span>
        </div>

        <div class="detail-card">
          <div class="detail-row" v-for="item in detailItems" :key="item.label">
            <span class="detail-label">{{ item.label }}</span>
            <span class="detail-value" :class="{ 'detail-value-red': item.red }">{{ item.value }}</span>
          </div>
        </div>

        <div class="section-label">常用功能</div>

        <div class="feature-showcase">
          <div
            v-for="item in menuItems"
            :key="item.label"
            class="feature-card"
            @click="item.action?.()"
          >
            <div class="feature-card-icon" :style="{ background: item.bg, color: item.color }">
              <span>{{ item.icon }}</span>
            </div>
            <div class="feature-card-info">
              <span class="feature-card-title">{{ item.label }}</span>
              <span class="feature-card-desc">{{ item.desc }}</span>
            </div>
            <span class="feature-card-arrow">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#9E9E9E" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="9 18 15 12 9 6"/>
              </svg>
            </span>
          </div>
        </div>

        <div class="logout-area">
          <button class="logout-btn" @click="handleLogout">
            <span>退出登录</span>
          </button>
        </div>
      </div>
    </template>

    <Teleport to="body">
      <Transition name="modal-fade">
        <div v-if="showLoginModal" class="modal-backdrop" @click.self="closeLoginModal">
          <div class="modal-panel">
            <template v-if="!showRegisterInModal">
              <h3 class="modal-title">登录</h3>
              <div class="modal-form">
                <input v-model="loginForm.username" class="modal-input" type="text" placeholder="请输入用户名" />
                <input v-model="loginForm.password" class="modal-input" type="password" placeholder="请输入密码" />
              </div>
              <div class="modal-actions">
                <button class="modal-btn modal-btn-cancel" @click="closeLoginModal">取消</button>
                <button class="modal-btn modal-btn-save" @click="handleLogin">登录</button>
              </div>
              <p class="modal-switch" @click="showRegisterInModal = true">没有账号？去注册</p>
            </template>
            <template v-else>
              <h3 class="modal-title">注册账号</h3>
              <div class="modal-form">
                <input v-model="regForm.username" class="modal-input" type="text" placeholder="用户名 *" />
                <input v-model="regForm.password" class="modal-input" type="password" placeholder="密码 *" />
                <input v-model="regForm.nickname" class="modal-input" type="text" placeholder="昵称" />
                <input v-model="regForm.phone" class="modal-input" type="text" placeholder="手机号" />
                <div class="form-inline">
                  <input v-model="regForm.babyAge" class="modal-input form-half" type="text" placeholder="月龄" />
                  <select v-model="regForm.babyGender" class="modal-select form-half">
                    <option value="">性别</option>
                    <option value="男">男宝</option>
                    <option value="女">女宝</option>
                  </select>
                </div>
              </div>
              <div class="modal-actions">
                <button class="modal-btn modal-btn-cancel" @click="showRegisterInModal = false">返回登录</button>
                <button class="modal-btn modal-btn-save" @click="handleRegister">注册</button>
              </div>
            </template>
          </div>
        </div>
      </Transition>
    </Teleport>

    <Teleport to="body">
      <Transition name="modal-fade">
        <div v-if="showEditModal" class="modal-backdrop" @click.self="closeEditModal">
          <div class="modal-panel">
            <h3 class="modal-title">编辑个人信息</h3>

            <div class="modal-avatar-section" @click="triggerAvatarUpload">
              <div class="modal-avatar-img" v-if="editAvatarPreview" :style="{ backgroundImage: 'url(' + editAvatarPreview + ')' }"></div>
              <div class="modal-avatar-placeholder" v-else>
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#9E9E9E" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
                  <circle cx="12" cy="13" r="4"/>
                </svg>
              </div>
              <p class="modal-avatar-hint">点击更换头像</p>
            </div>

            <div class="modal-field">
              <label class="modal-label">昵称</label>
              <input
                v-model="editForm.nickname"
                class="modal-input"
                type="text"
                placeholder="请输入昵称"
              />
            </div>

            <div class="modal-actions">
              <button class="modal-btn modal-btn-cancel" @click="closeEditModal">取消</button>
              <button class="modal-btn modal-btn-save" @click="handleSaveProfile">保存</button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- 宝宝信息弹窗 -->
    <Teleport to="body">
      <Transition name="modal-fade">
        <div v-if="showBabyInfoModal" class="modal-backdrop" @click.self="showBabyInfoModal = false">
          <div class="modal-panel">
            <h3 class="modal-title">👶 宝宝信息</h3>
            <div class="modal-form">
              <div class="modal-field">
                <label class="modal-label">宝宝月龄（个月）</label>
                <input
                  v-model="babyForm.babyAge"
                  class="modal-input"
                  type="text"
                  placeholder="例如：6"
                />
              </div>
              <div class="modal-field">
                <label class="modal-label">宝宝性别</label>
                <select v-model="babyForm.babyGender" class="modal-select">
                  <option value="">请选择</option>
                  <option value="男">男宝</option>
                  <option value="女">女宝</option>
                </select>
              </div>
            </div>
            <div class="modal-actions">
              <button class="modal-btn modal-btn-cancel" @click="showBabyInfoModal = false">取消</button>
              <button class="modal-btn modal-btn-save" @click="saveBabyInfo">保存</button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- 设置面板 -->
    <Teleport to="body">
      <Transition name="modal-fade">
        <div v-if="showSettingsPanelFlag" class="modal-backdrop" @click.self="showSettingsPanelFlag = false">
          <div class="modal-panel">
            <h3 class="modal-title">⚙️ 系统设置</h3>
            <div class="settings-content">
              <p class="settings-text">当前版本功能持续完善中，敬请期待更多设置选项。</p>
              <div class="settings-item">
                <span class="settings-label">账户安全</span>
                <span class="settings-value">已开启基础保护</span>
              </div>
              <div class="settings-item">
                <span class="settings-label">数据同步</span>
                <span class="settings-value">云端存储已启用</span>
              </div>
            </div>
            <div class="modal-actions">
              <button class="modal-btn modal-btn-cancel" @click="showSettingsPanelFlag = false">关闭</button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- 关于面板 -->
    <Teleport to="body">
      <Transition name="modal-fade">
        <div v-if="showAboutPanelFlag" class="modal-backdrop" @click.self="showAboutPanelFlag = false">
          <div class="modal-panel about-panel">
            <h3 class="modal-title">ℹ️ 关于我们</h3>
            <div class="about-content">
              <div class="about-logo">🍼</div>
              <h4 class="about-app-name">宝贝健康助手</h4>
              <p class="about-version">版本 1.0.0</p>
              <p class="about-desc">基于AI技术的婴幼儿膳食分析与健康管理应用，为宝宝提供科学的营养建议。</p>
              <div class="about-features">
                <span class="about-feature-tag">🤖 AI识别</span>
                <span class="about-feature-tag">📊 营养分析</span>
                <span class="about-feature-tag">💬 社区交流</span>
              </div>
            </div>
            <div class="modal-actions">
              <button class="modal-btn modal-btn-cancel" @click="showAboutPanelFlag = false">关闭</button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <input
      ref="avatarInputRef"
      type="file"
      accept="image/*"
      class="sr-only"
      @change="onAvatarFile"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { login, register, updateUser } from '../api/index'

const router = useRouter()

const userStore = useUserStore()
const avatarInputRef = ref(null)

const showLoginModal = ref(false)
const showRegisterInModal = ref(false)
const showEditModal = ref(false)
const currentTime = ref('')
let clockTimer = null

const loginForm = reactive({ username: '', password: '' })
const regForm = reactive({
  username: '', password: '', nickname: '',
  phone: '', babyAge: '', babyGender: ''
})

const editForm = reactive({ nickname: '' })
const editAvatarPreview = ref('')
const editAvatarFile = ref(null)

const userUUID = computed(() => {
  const id = userStore.userInfo?.userId
  if (!id) return '—'
  return 'UUID: ' + String(id).padStart(8, '0').slice(0, 12)
})

const displayNickname = computed(() => {
  return userStore.userInfo?.nickname || '宝宝家长'
})

const displayAvatar = computed(() => {
  return userStore.userInfo?.avatar || ''
})

const avatarLetter = computed(() => {
  const name = displayNickname.value
  return name ? name.charAt(0).toUpperCase() : 'U'
})

const userRole = computed(() => {
  return userStore.isAdmin() ? '管理员' : '用户'
})

const roleClass = computed(() => {
  return userStore.isAdmin() ? 'role-admin' : 'role-user'
})

const detailItems = computed(() => [
  { label: '宝宝月龄', value: userStore.userInfo?.babyAge ? `${userStore.userInfo.babyAge} 个月` : '—' },
  { label: '宝宝性别', value: userStore.userInfo?.babyGender || '—' },
  { label: '手机号', value: userStore.userInfo?.phone || '—' },
  { label: '账户状态', value: '正常', red: false }
])

const menuItems = computed(() => {
  const base = [
    {
      icon: '👶',
      label: '宝宝信息',
      desc: '管理宝宝个人资料',
      bg: '#E8F5E9',
      color: '#4CAF50',
      action: () => { openBabyInfoModal() }
    },
    {
      icon: '📊',
      label: '饮食统计',
      desc: '查看饮食记录分析',
      bg: '#FFF3E0',
      color: '#FF9800',
      action: () => { router.push('/health') }
    },
    {
      icon: '⭐',
      label: '我的收藏',
      desc: '收藏的文章和食谱',
      bg: '#FFF8E1',
      color: '#FFC107',
      action: () => { router.push('/community?tab=fav') }
    },
    {
      icon: '⚙️',
      label: '系统设置',
      desc: '偏好与通知设置',
      bg: '#ECEFF1',
      color: '#607D8B',
      action: () => { showSettingsPanel() }
    },
    {
      icon: 'ℹ️',
      label: '关于我们',
      desc: '版本信息与帮助',
      bg: '#E3F2FD',
      color: '#2196F3',
      action: () => { showAboutPanel() }
    }
  ]
  if (userStore.isAdmin()) {
    base.unshift({
      icon: '🛡️',
      label: '管理后台',
      desc: '系统管理与数据分析',
      bg: '#EDE7F6',
      color: '#673AB7',
      action: () => { router.push('/admin') }
    })
  }
  return base
})

const showBabyInfoModal = ref(false)
const babyForm = reactive({
  babyAge: '',
  babyGender: ''
})
const showSettingsPanelFlag = ref(false)
const showAboutPanelFlag = ref(false)

function openBabyInfoModal() {
  babyForm.babyAge = userStore.userInfo?.babyAge || ''
  babyForm.babyGender = userStore.userInfo?.babyGender || ''
  showBabyInfoModal.value = true
}

async function saveBabyInfo() {
  try {
    const res = await updateUser(userStore.getUserId(), {
      nickname: userStore.userInfo?.nickname,
      babyAge: babyForm.babyAge,
      babyGender: babyForm.babyGender
    })
    if (res.code === 200) {
      userStore.setUserInfo({
        ...userStore.userInfo,
        babyAge: babyForm.babyAge,
        babyGender: babyForm.babyGender
      })
      showBabyInfoModal.value = false
      alert('宝宝信息保存成功')
    }
  } catch (err) {
    alert(err.response?.data?.message || '保存失败')
  }
}

function showSettingsPanel() {
  showSettingsPanelFlag.value = !showSettingsPanelFlag.value
  showAboutPanelFlag.value = false
}

function showAboutPanel() {
  showAboutPanelFlag.value = !showAboutPanelFlag.value
  showSettingsPanelFlag.value = false
}

function updateClock() {
  const now = new Date()
  const hh = String(now.getHours()).padStart(2, '0')
  const mm = String(now.getMinutes()).padStart(2, '0')
  currentTime.value = `${hh}:${mm}`
}

function goAdminIfAdmin() {
  if (userStore.isAdmin()) {
    router.push('/admin')
  }
}

function openLoginModal() {
  showRegisterInModal.value = false
  loginForm.username = ''
  loginForm.password = ''
  showLoginModal.value = true
}

function closeLoginModal() {
  showLoginModal.value = false
}

function openEditModal() {
  editForm.nickname = userStore.userInfo?.nickname || ''
  editAvatarPreview.value = userStore.userInfo?.avatar || ''
  editAvatarFile.value = null
  showEditModal.value = true
}

function closeEditModal() {
  showEditModal.value = false
}

function triggerAvatarUpload() {
  avatarInputRef.value?.click()
}

function onAvatarFile(e) {
  const file = e.target.files?.[0]
  if (!file) return
  editAvatarFile.value = file
  const reader = new FileReader()
  reader.onload = (ev) => {
    editAvatarPreview.value = ev.target.result
  }
  reader.readAsDataURL(file)
  e.target.value = ''
}

async function handleSaveProfile() {
  const userId = userStore.getUserId()
  if (!userId) return
  try {
    const payload = { nickname: editForm.nickname }
    if (editAvatarPreview.value) {
      payload.avatar = editAvatarPreview.value
    }
    const res = await updateUser(userId, payload)
    userStore.setUserInfo(res.data)
    showEditModal.value = false
  } catch {
    userStore.setUserInfo({
      ...userStore.userInfo,
      nickname: editForm.nickname,
      avatar: editAvatarPreview.value || userStore.userInfo?.avatar
    })
    showEditModal.value = false
  }
}

async function handleLogin() {
  if (!loginForm.username || !loginForm.password) {
    alert('请输入用户名和密码')
    return
  }
  try {
    const res = await login(loginForm)
    userStore.setUserInfo(res.data)
    showLoginModal.value = false
  } catch {
    alert('登录失败，请检查用户名密码')
  }
}

async function handleRegister() {
  if (!regForm.username || !regForm.password) {
    alert('用户名和密码为必填项')
    return
  }
  try {
    const res = await register(regForm)
    userStore.setUserInfo(res.data)
    showLoginModal.value = false
  } catch {
    alert('注册失败，用户名可能已存在')
  }
}

function handleLogout() {
  userStore.logout()
}

onMounted(() => {
  updateClock()
  clockTimer = setInterval(updateClock, 1000)
})

onUnmounted(() => {
  if (clockTimer) {
    clearInterval(clockTimer)
    clockTimer = null
  }
})
</script>

<style scoped>
.my-page {
  flex: 1;
  padding: 0 16px;
  padding-bottom: calc(80px + var(--safe-bottom));
  overflow-y: auto;
}

.welcome-area,
.profile-area {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding-top: 16px;
}

.welcome-hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 0 20px;
}

.hero-badge {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: #E3F2FD;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 14px;
}

.hero-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: 0.3px;
}

.hero-desc {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 4px;
}

/* ===== Launch button (MyView login) ===== */
.launch-btn {
  display: flex;
  align-items: center;
  font-family: inherit;
  font-weight: 500;
  font-size: 17px;
  padding: 0.8em 1.3em 0.8em 0.9em;
  color: white;
  background: linear-gradient(to right, #3d29edff, #302b63, #3d3d60ff);
  border: none;
  letter-spacing: 0.05em;
  border-radius: 16px;
  cursor: pointer;
  width: 100%;
  justify-content: center;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.launch-btn:active {
  transform: scale(0.97);
}

.launch-btn svg {
  margin-right: 3px;
  transform: rotate(30deg);
  transition: transform 0.5s cubic-bezier(0.76, 0, 0.24, 1);
}

.launch-btn span {
  transition: transform 0.5s cubic-bezier(0.76, 0, 0.24, 1);
}

.launch-btn:hover svg {
  transform: translateX(5px) rotate(90deg);
}

.launch-btn:hover span {
  transform: translateX(7px);
}

/* Logged-in styles */
.status-overview {
  display: flex;
  gap: 10px;
  align-items: stretch;
}

.status-main-card {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  background: linear-gradient(135deg, #a8f6b7ff, #C8E6C9);
  border-radius: 15px;
  border: 1px solid rgba(76, 175, 80, 0.12);
}

.avatar-wrap {
  position: relative;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  flex-shrink: 0;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.6);
}

.avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background-size: cover;
  background-position: center;
}

.avatar-letter {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: 700;
  color: #0C84FF;
}

.avatar-edit-badge {
  position: absolute;
  bottom: -2px;
  right: -2px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #0C84FF;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.2);
}

.status-main-text {
  flex: 1;
  min-width: 0;
}

.status-main-title {
  font-size: 18px;
  font-weight: 700;
  color: #212121;
}

.status-main-sub {
  font-size: 11px;
  color: #757575;
  margin-top: 2px;
  font-family: monospace;
  word-break: break-all;
}

.status-stats-col {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex-shrink: 0;
  width: 110px;
  align-self: stretch;
}

.status-stat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 12px 8px;
  background: linear-gradient(135deg, #0C84FF, #fa44e2ff);
  border-radius: 15px;
  gap: 2px;
  flex: 1;
}

.status-stat-edit {
  cursor: pointer;
  transition: all 0.2s ease;
}

.status-stat-edit:active {
  background: var(--bg-surface);
  transform: scale(0.96);
}

.stat-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.75);
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #FFFFFF;
}

.stat-value-time {
  font-family: monospace;
  font-size: 20px;
  letter-spacing: 1px;
  color: #FFFFFF;
}

.stat-role {
  font-size: 18px;
  font-weight: 800;
  letter-spacing: 2px;
}

.role-user {
  background: linear-gradient(135deg, #FFD700, #FFA500);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.role-admin {
 /* 设置字体透明 */
        color: transparent;
        /* 设置线性渐变，从红色渐变到蓝色 */
        background-image: linear-gradient(45deg, #f6b6feff, #80d1fcff);
        /* 使用 -webkit-background-clip 属性将背景剪裁至文本形状 */
        -webkit-background-clip: text;
        /* 非Webkit内核浏览器需要使用标准前缀 */
        background-clip: text;
        /* 把当前元素设置为行内块，以便能够应用背景 */
        display: inline-block;
}

.stat-card-clickable {
  cursor: pointer;
  transition: transform 0.15s ease, opacity 0.15s ease;
}

.stat-card-clickable:active {
  transform: scale(0.96);
  opacity: 0.9;
}

.stat-hint {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.65);
  margin-top: 2px;
}

/* MIUI-style card (camera-spotlight style) */
.miui-card {
  display: flex;
  align-items: center;
  padding: 16px 18px;
  gap: 14px;
  background: linear-gradient(135deg, #0C84FF, #0D7AEC);
  border-radius: 15px;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.miui-card:active {
  transform: scale(0.98);
  box-shadow: 0 4px 16px rgba(12, 132, 255, 0.3);
}

.miui-card-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.miui-card-text {
  flex: 1;
  min-width: 0;
}

.miui-card-title {
  font-size: 16px;
  font-weight: 700;
  color: #FFFFFF;
}

.miui-card-sub {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.75);
  margin-top: 2px;
}

.miui-card-arrow {
  flex-shrink: 0;
}

/* Detail card */
.detail-card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: 4px 16px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-light);
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid var(--divider);
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.detail-value {
  font-size: 14px;
  color: var(--text-secondary);
  text-align: right;
  max-width: 55%;
  word-break: break-all;
}

.detail-value-red {
  background: #FFEBEE;
  color: #C62828;
  padding: 2px 10px;
  border-radius: var(--radius-xs);
  font-weight: 600;
  font-size: 13px;
}

.section-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 1px;
  padding: 8px 4px 2px;
}

.feature-showcase {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.feature-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  background: var(--bg-card);
  border-radius: var(--radius-sm);
  box-shadow: var(--shadow-xs);
  border: 1px solid var(--border-light);
  transition: all 0.2s ease;
}

.feature-card:active {
  transform: scale(0.98);
  background: var(--bg-surface);
}

.feature-card-icon {
  width: 42px;
  height: 42px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.feature-card-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.feature-card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.feature-card-desc {
  font-size: 12px;
  color: var(--text-secondary);
}

.feature-card-arrow {
  flex-shrink: 0;
}

.logout-area {
  padding: 24px 0;
}

.logout-btn {
  width: 100%;
  padding: 14px;
  border-radius: var(--radius-sm);
  background: #FFEBEE;
  color: #C62828;
  font-size: 15px;
  font-weight: 600;
  transition: all 0.2s ease;
  border: 1px solid rgba(198, 40, 40, 0.12);
}

.logout-btn:active {
  background: #FFCDD2;
  transform: scale(0.97);
}

/* Modal - shared */
.modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 999;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.modal-panel {
  width: 100%;
  max-width: 340px;
  background: #FFFFFF;
  border-radius: 18px;
  padding: 28px 24px 24px;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.modal-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  text-align: center;
}

.modal-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.modal-input {
  width: 100%;
  padding: 12px 14px;
  background: #F4F4F4;
  border: 1px solid #E6E6E6;
  border-radius: var(--radius-sm);
  color: var(--text-primary);
  font-size: 14px;
  transition: border-color 0.2s;
  outline: none;
}

.modal-input:focus {
  border-color: #0C84FF;
}

.modal-input::placeholder {
  color: #9E9E9E;
}

.modal-select {
  padding: 12px 14px;
  background: #F4F4F4;
  border: 1px solid #E6E6E6;
  border-radius: var(--radius-sm);
  color: var(--text-primary);
  font-size: 14px;
  outline: none;
}

.form-inline {
  display: flex;
  gap: 10px;
}

.form-half {
  flex: 1;
}

.modal-actions {
  display: flex;
  gap: 12px;
  padding-top: 4px;
}

.modal-btn {
  flex: 1;
  padding: 14px;
  border-radius: 15px;
  font-size: 15px;
  font-weight: 600;
  transition: background 0.15s ease, transform 0.1s ease;
}

.modal-btn-cancel {
  background: #F0F0F0;
  color: #303030;
  border: 1px solid transparent;
}

.modal-btn-cancel:active {
  background: #DDDDDD;
  transform: scale(0.97);
}

.modal-btn-save {
  background: #0C84FF;
  color: #FFFFFF;
  border: 1px solid transparent;
}

.modal-btn-save:active {
  background: #0D7AEC;
  transform: scale(0.97);
}

.modal-switch {
  font-size: 13px;
  color: #0C84FF;
  text-align: center;
  cursor: pointer;
  transition: opacity 0.2s;
}

.modal-switch:active {
  opacity: 0.7;
}

/* Edit modal specific */
.modal-avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 12px 0;
}

.modal-avatar-placeholder {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: #F4F4F4;
  border: 2px dashed #E6E6E6;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-avatar-img {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background-size: cover;
  background-position: center;
  border: 2px solid #0C84FF;
}

.modal-avatar-hint {
  font-size: 12px;
  color: var(--text-muted);
}

.modal-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.modal-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}

/* Modal transition */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.25s ease;
}

.modal-fade-enter-active .modal-panel,
.modal-fade-leave-active .modal-panel {
  transition: transform 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.modal-fade-enter-from .modal-panel {
  transform: scale(0.9) translateY(20px);
}

.modal-fade-leave-to .modal-panel {
  transform: scale(0.95) translateY(10px);
}

/* Settings Panel */
.settings-content {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 4px 0;
}

.settings-text {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
}

.settings-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--border-light);
}

.settings-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.settings-value {
  font-size: 13px;
  color: #4CAF50;
  font-weight: 500;
}

/* About Panel */
.about-panel {
  max-width: 320px;
}

.about-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
}

.about-logo {
  font-size: 56px;
}

.about-app-name {
  font-size: 20px;
  font-weight: 800;
  color: var(--text-primary);
  margin: 0;
}

.about-version {
  font-size: 13px;
  color: var(--text-muted);
  margin: 0;
}

.about-desc {
  font-size: 13px;
  color: var(--text-secondary);
  text-align: center;
  line-height: 1.6;
  margin: 0;
}

.about-features {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
  margin-top: 6px;
}

.about-feature-tag {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: var(--radius-full);
  background: #E3F2FD;
  color: #1976D2;
}
</style>

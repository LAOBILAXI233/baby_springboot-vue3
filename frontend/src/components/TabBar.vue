<template>
  <nav class="tab-bar">
    <div class="slider" :style="sliderStyle"></div>
    <router-link
      v-for="(tab, index) in tabs"
      :key="tab.name"
      :ref="el => setTabRef(el, index)"
      :to="tab.path"
      class="tab-item"
      :class="{ 'tab-active': currentIndex === index }"
    >
      <span class="tab-icon">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path v-if="tab.name === 'Home'" d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
          <circle v-if="tab.name === 'Home'" cx="12" cy="13" r=".5" fill="currentColor" stroke="none" opacity="0"/>
          <polyline v-if="tab.name === 'Home'" points="9 22 9 12 15 12 15 22"/>

          <path v-if="tab.name === 'Health'" d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>

          <path v-if="tab.name === 'Community'" d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
          <circle v-if="tab.name === 'Community'" cx="9" cy="7" r="4"/>
          <path v-if="tab.name === 'Community'" d="M23 21v-2a4 4 0 0 0-3-3.87"/>
          <path v-if="tab.name === 'Community'" d="M16 3.13a4 4 0 0 1 0 7.75"/>

          <path v-if="tab.name === 'My'" d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
          <circle v-if="tab.name === 'My'" cx="12" cy="7" r="4"/>

          <path v-if="tab.name === 'Admin'" d="M12 2L2 7l10 5 10-5-10-5z"/>
          <polyline v-if="tab.name === 'Admin'" points="2 17 12 22 22 17"/>
          <polyline v-if="tab.name === 'Admin'" points="2 12 12 17 22 12"/>
        </svg>
      </span>
      <span class="tab-label">{{ tab.label }}</span>
    </router-link>
  </nav>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'

const route = useRoute()
const userStore = useUserStore()

const tabs = computed(() => {
  const base = [
    { name: 'Home', path: '/', label: '首页' },
    { name: 'Health', path: '/health', label: '健康' },
    { name: 'Community', path: '/community', label: '社区' },
    { name: 'My', path: '/my', label: '我的' }
  ]
  if (userStore.isAdmin()) {
    base.push({ name: 'Admin', path: '/admin', label: '管理' })
  }
  return base
})

const currentIndex = ref(3)
const tabRefs = ref([])
const sliderLeft = ref(0)
const sliderWidth = ref(0)

const sliderStyle = computed(() => ({
  left: `${sliderLeft.value}px`,
  width: `${sliderWidth.value}px`
}))

function setTabRef(el, index) {
  if (el) {
    tabRefs.value[index] = el.$el || el
  }
}

function updateSlider() {
  nextTick(() => {
    const tabEl = tabRefs.value[currentIndex.value]
    if (tabEl) {
      const parent = tabEl.parentElement
      const parentRect = parent.getBoundingClientRect()
      const tabRect = tabEl.getBoundingClientRect()
      const margin = 8
      sliderLeft.value = tabRect.left - parentRect.left + margin
      sliderWidth.value = tabRect.width - margin * 2
    }
  })
}

function syncIndex() {
  const idx = tabs.value.findIndex(t => t.name === route.name)
  if (idx !== -1 && idx !== currentIndex.value) {
    currentIndex.value = idx
    updateSlider()
  }
}

watch(() => route.name, () => {
  syncIndex()
})

onMounted(() => {
  syncIndex()
  window.addEventListener('resize', updateSlider)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateSlider)
})
</script>

<style scoped>
.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-around;
  align-items: center;
  height: 64px;
  padding: 0 12px;
  padding-bottom: env(safe-area-inset-bottom, 0px);
  z-index: 100;
  background: var(--bg-card);
  border-top: 1px solid var(--divider);
  border-radius: 20px 20px 0 0;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.04);
}

.slider {
  position: absolute;
  top: 4px;
  height: calc(100% - env(safe-area-inset-bottom, 0px) - 8px);
  background: rgba(33, 150, 243, 0.08);
  border: 1px solid rgba(33, 150, 243, 0.15);
  border-radius: 14px;
  transition: left 0.35s cubic-bezier(0.34, 1.56, 0.64, 1), width 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
  z-index: 0;
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  flex: 1;
  height: 100%;
  position: relative;
  z-index: 1;
  transition: color 0.25s ease;
  color: var(--text-muted);
}

.tab-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.tab-label {
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.3px;
  transition: color 0.25s ease;
}

.tab-active .tab-icon {
  transform: translateY(-1px);
}

.tab-active {
  color: var(--blue-primary);
}
</style>

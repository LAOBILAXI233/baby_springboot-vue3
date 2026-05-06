<template>
  <div class="app">
    <div class="page-slot">
      <router-view v-slot="{ Component, route: currentRoute }">
        <transition
          :name="transitionName"
          mode="out-in"
          @before-leave="onBeforeLeave"
          @enter="onEnter"
        >
          <component :is="Component" :key="currentRoute.path" />
        </transition>
      </router-view>
    </div>
    <TabBar v-if="showTabBar" />
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import TabBar from './components/TabBar.vue'

const route = useRoute()
const router = useRouter()
const transitionName = ref('page-whip-right')
const prevPath = ref('')

watch(() => route.path, (newPath, oldPath) => {
  const old = oldPath || prevPath.value
  if (!old) {
    transitionName.value = 'page-fade'
    return
  }
  const routeMap = ['/', '/health', '/community', '/my', '/admin']
  const oldIdx = routeMap.indexOf(old)
  const newIdx = routeMap.indexOf(newPath)
  if (oldIdx === -1 || newIdx === -1) {
    transitionName.value = 'page-fade'
  } else if (newIdx > oldIdx) {
    transitionName.value = 'page-whip-right'
  } else {
    transitionName.value = 'page-whip-left'
  }
  prevPath.value = old
})

const showTabBar = computed(() => {
  return ['Home', 'Health', 'Community', 'My', 'Admin'].includes(route.name)
})

function onBeforeLeave(el) {
  const direction = transitionName.value === 'page-whip-right' ? 1 : -1
  el.style.setProperty('--whip-dir', direction)
}

function onEnter(el) {
  const direction = transitionName.value === 'page-whip-right' ? -1 : 1
  el.style.setProperty('--whip-dir', direction)
}
</script>

<style>
.app {
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  perspective: 1200px;
}

.page-slot {
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 0;
  padding-bottom: 64px;
}

/* TabBar fixed above transitions */
.tab-bar {
  z-index: 1000 !important;
}

/* ===== Whip transition (HyperOS style) - speed matches TabBar ===== */
.page-whip-right-leave-active,
.page-whip-left-leave-active {
  transition: transform 0.35s cubic-bezier(0.55, 0.0, 1, 0.45),
              opacity 0.3s ease;
  position: absolute;
  inset: 0;
  z-index: 10;
}

.page-whip-right-enter-active,
.page-whip-left-enter-active {
  transition: transform 0.35s cubic-bezier(0.34, 1.56, 0.64, 1),
              opacity 0.3s ease;
  position: absolute;
  inset: 0;
  z-index: 1;
}

/* Leaving - whip out */
.page-whip-right-leave-to {
  transform: translateX(100%) rotateY(-18deg) scale(0.92) skewX(3deg);
  opacity: 0;
  filter: blur(4px);
}

.page-whip-left-leave-to {
  transform: translateX(-100%) rotateY(18deg) scale(0.92) skewX(-3deg);
  opacity: 0;
  filter: blur(4px);
}

.page-whip-right-leave-from,
.page-whip-left-leave-from {
  transform: translateX(0) rotateY(0deg) scale(1) skewX(0deg);
  opacity: 1;
  filter: blur(0px);
}

/* Entering - whip in (from opposite direction) */
.page-whip-right-enter-from {
  transform: translateX(-40%) rotateY(12deg) scale(0.94) skewX(-2deg);
  opacity: 0;
  filter: blur(6px);
}

.page-whip-left-enter-from {
  transform: translateX(40%) rotateY(-12deg) scale(0.94) skewX(2deg);
  opacity: 0;
  filter: blur(6px);
}

.page-whip-right-enter-to,
.page-whip-left-enter-to {
  transform: translateX(0) rotateY(0deg) scale(1) skewX(0deg);
  opacity: 1;
  filter: blur(0px);
}

/* ===== Fallback fade transition ===== */
.page-fade-leave-active,
.page-fade-enter-active {
  transition: opacity 0.2s ease;
}
.page-fade-enter-from,
.page-fade-leave-to {
  opacity: 0;
}

/* ===== Base page container for 3D effect ===== */
.page-container {
  backface-visibility: hidden;
}
</style>

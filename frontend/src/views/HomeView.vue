<template>
  <div class="page-container">
    <header class="home-header">
      <div class="header-top">
        <h1 class="home-logo">
          <span class="logo-yellow">宝贝</span><span class="logo-white">健康</span>
        </h1>
        <span class="header-badge bmi-badge bmi-badge-red" v-if="userStore.isLoggedIn">
          已登录
        </span>
      </div>
      <p class="home-slogan">AI智能膳食分析，守护宝宝健康成长</p>
    </header>

    <section class="home-camera-section">
      <CameraUpload @save="onSaveRecord" @post-to-community="onPostToCommunity" />
    </section>

    <section class="home-features">
      <h2 class="page-title">✨ 功能中心</h2>
      <div class="features-grid">
        <div
          v-for="f in features"
          :key="f.title"
          class="feature-card"
          :style="{ background: f.bg }"
          @click="f.action?.()"
        >
          <div class="feature-icon-wrap" :style="{ background: f.accent }">
            <span class="feature-icon">{{ f.icon }}</span>
          </div>
          <div class="feature-content">
            <span class="feature-title">{{ f.title }}</span>
            <span class="feature-desc">{{ f.desc }}</span>
          </div>
          <span class="feature-arrow" :style="{ color: f.accent }">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="9 18 15 12 9 6"/>
            </svg>
          </span>
        </div>
      </div>
    </section>

    <section class="home-tips">
      <h2 class="page-title">育儿小贴士</h2>
      <div class="tip-card">
        <span class="tip-icon">💡</span>
        <span class="tip-text">{{ currentTip }}</span>
      </div>
    </section>
  </div>
</template>

<script setup>
import { useUserStore } from '../stores/user'
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import CameraUpload from '../components/CameraUpload.vue'
import { createCommunityPost } from '../api/index'

const userStore = useUserStore()
const router = useRouter()

const features = [
  {
    icon: '📷', title: 'AI识别', desc: '拍照识别食物营养',
    bg: '#275dffff', //accent: '#5f5f5fff',
    action: () => { router.push({ name: 'Health' }) }
  },
  {
    icon: '📊', title: '健康评分', desc: '专业膳食评估',
    bg: '#ff0101ff',// accent: '#275dffff',
    action: () => { router.push({ name: 'Health' }) }
  },
  {
    icon: '👶', title: '宝宝信息', desc: '分龄营养建议',
    bg: '#8484878b', accent: '#8484878b',
    action: () => { router.push('/my') }
  },
  {
    icon: '📝', title: '饮食记录', desc: '追踪每日饮食',
   bg: '#8484878b', accent: '#8484878b',
    action: () => { router.push({ name: 'Health' }) }
  },
  {
    icon: '💬', title: '宝妈社区', desc: '交流辅食心得',
    bg: '#16e000ff', accent: '#8484878b',
    action: () => { router.push('/community') }
  },
  {
    icon: '⭐', title: '我的收藏', desc: '收藏的食谱文章',
    bg: '#fad016', accent: '#8484878b',
    action: () => { router.push('/community?tab=fav') }
  }
]

const tips = [
  '6个月大的宝宝可以开始添加辅食，建议从单一谷物米粉开始。',
  '每次添加新食物时，观察3-5天，确认无过敏反应。',
  '1岁以下的宝宝不宜添加盐和糖。',
  '保持食物的多样性，让宝宝接触不同口味和质地。',
  '进食时保持坐姿，避免跑动喂食以防呛噎。'
]

const currentTip = ref(tips[0])

function onSaveRecord(record) {
  router.push({ name: 'Health' })
}

async function onPostToCommunity(result) {
  const userId = userStore.getUserId()
  if (!userId) {
    alert('请先登录')
    return
  }
  const content = `AI识别结果：${result.foodName}\n健康评分：${result.healthScore}分\n${result.aiResult}\n${result.suggestion}`
  try {
    await createCommunityPost({
      userId,
      content,
      images: '[]'
    })
    if (confirm('发布成功！是否跳转到社区查看？')) {
      router.push('/community')
    }
  } catch (err) {
    alert(err.response?.data?.message || '发布失败')
  }
}

onMounted(() => {
  const idx = Math.floor(Math.random() * tips.length)
  currentTip.value = tips[idx]
})
</script>

<style scoped>
.page-container {
  padding-bottom: calc(80px + var(--safe-bottom));
}

.home-header {
  margin-bottom: 20px;
}

.header-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.home-logo {
  font-size: 32px;
  font-weight: 900;
  letter-spacing: -1px;
}

.logo-yellow {
  color: #0C84FF;
}

.logo-white {
  color: var(--text-primary);
}

.home-slogan {
  font-size: 14px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.home-camera-section {
  margin-bottom: 28px;
}

.features-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.feature-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  border-radius: 15px;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.feature-card:active {
  transform: scale(0.98);
}

.feature-icon-wrap {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.feature-icon {
  font-size: 24px;
}

.feature-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.feature-title {
  font-size: 16px;
  font-weight: 700;
  color: #FFFFFF;
}

.feature-desc {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.75);
}

.feature-arrow {
  color: var(--text-muted);
  flex-shrink: 0;
}

.home-tips {
  margin-top: 24px;
  margin-bottom: 20px;
}

.tip-card {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 14px;
  border-radius: var(--radius-sm);
  background: var(--bg-card);
  box-shadow: var(--shadow-xs);
  border: 1px solid var(--border-light);
}

.tip-icon {
  font-size: 20px;
  flex-shrink: 0;
}

.tip-text {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
}
</style>

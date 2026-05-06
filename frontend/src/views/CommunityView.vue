<template>
  <div class="page-container">
    <h1 class="page-title">💬 宝妈社区</h1>
    <p class="page-subtitle">分享育儿经验，交流辅食心得</p>

    <div class="community-tabs">
      <button class="community-tab" :class="{ active: activeTab === 'all' }" @click="activeTab = 'all'">全部</button>
      <button class="community-tab" :class="{ active: activeTab === 'fav' }" @click="activeTab = 'fav'">收藏</button>
    </div>

    <section class="community-posts">
      <div v-for="post in displayPosts" :key="post.id" class="post-card lg-glass lg-refraction">
        <div class="post-header">
          <div class="post-avatar" v-if="post.avatar" :style="{ backgroundImage: 'url(' + post.avatar + ')' }"></div>
          <div class="post-avatar" v-else>{{ (post.nickname || post.username || 'U')?.[0] }}</div>
          <div class="post-meta">
            <span class="post-user">{{ post.nickname || post.username || '用户' + post.userId }}</span>
            <span class="post-time">{{ formatTime(post.createTime) }}</span>
          </div>
          <button v-if="post.userId === userStore.getUserId() || userStore.isAdmin()" class="delete-btn noselect" @click="handleDeletePost(post.id)">
            <span class="text">删除</span>
            <span class="icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path d="M24 20.188l-8.315-8.209 8.2-8.282-3.697-3.697-8.212 8.318-8.31-8.203-3.666 3.666 8.321 8.24-8.206 8.313 3.666 3.666 8.237-8.318 8.285 8.203z"></path></svg>
            </span>
          </button>
        </div>
        <p class="post-content">{{ post.content }}</p>
        <div class="post-images" v-if="post.images && post.images.length">
          <img v-for="(img, i) in post.images" :key="i" :src="img" class="post-img" @click="previewImage = img" />
        </div>
        <div class="post-actions">
          <button class="action-btn" @click="toggleLike(post)">
            <span :class="{ liked: post.liked }">❤️</span>
            <span>{{ post.likeCount }}</span>
          </button>
          <button class="action-btn" @click="toggleComment(post)">
            <span>💬</span>
            <span>{{ post.commentCount }}</span>
          </button>
          <button class="action-btn" @click="toggleFavorite(post)">
            <span :class="{ favorited: post.favorited }">⭐</span>
            <span>{{ post.favorited ? '已收藏' : '收藏' }}</span>
          </button>
        </div>

        <!-- 评论区 -->
        <div v-if="activeCommentPostId === post.id" class="comment-section">
          <div class="comment-list">
            <div v-for="c in comments[post.id] || []" :key="c.id" class="comment-item">
              <div class="comment-avatar">{{ (c.nickname || c.username || 'U')?.[0] }}</div>
              <div class="comment-body">
                <span class="comment-user">{{ c.nickname || c.username || '用户' + c.userId }}</span>
                <span class="comment-text">{{ c.content }}</span>
                <div class="comment-images" v-if="c.images && c.images.length">
                  <img v-for="(img, i) in c.images" :key="i" :src="img" class="comment-img" />
                </div>
                <span class="comment-time">{{ formatTime(c.createTime) }}</span>
              </div>
              <button v-if="c.userId === userStore.getUserId() || userStore.isAdmin()" class="comment-del-btn" @click="handleDeleteComment(c.id, post.id)">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                </svg>
              </button>
            </div>
            <div v-if="!(comments[post.id] || []).length" class="comment-empty">暂无评论，快来抢沙发吧</div>
          </div>
          <div class="comment-input-area">
            <input v-model="commentText" class="comment-input" placeholder="写评论..." @keyup.enter="submitComment(post.id)" />
            <button class="comment-img-btn" @click="triggerCommentImage">📷</button>
            <button class="comment-send-btn" @click="submitComment(post.id)">发送</button>
          </div>
          <div v-if="commentImages.length" class="comment-preview-images">
            <div v-for="(img, i) in commentImages" :key="i" class="comment-preview-wrap">
              <img :src="img" class="comment-preview-img" />
              <button class="comment-preview-remove" @click="commentImages.splice(i, 1)">×</button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <div class="empty-state lg-glass" v-if="displayPosts.length === 0">
      <span class="empty-icon">📝</span>
      <span class="empty-text">暂无社区动态</span>
      <span class="empty-hint">快来分享你的辅食经验吧</span>
    </div>

    <!-- 发帖按钮 -->
    <button class="skew-btn fab" @click="showPostModal = true">
      <span></span>
      <span></span>
      <span></span>
      <span></span>
      <span>
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
          <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
        </svg>
        发布
      </span>
    </button>

    <!-- 发帖弹窗 -->
    <Teleport to="body">
      <Transition name="modal-fade">
        <div v-if="showPostModal" class="modal-backdrop" @click.self="showPostModal = false">
          <div class="modal-panel">
            <h3 class="modal-title">发布动态</h3>
            <textarea v-model="postContent" class="modal-textarea" placeholder="分享你的育儿心得..."></textarea>
            <div class="modal-image-area">
              <div v-for="(img, i) in postImages" :key="i" class="modal-image-preview">
                <img :src="img" />
                <button @click="postImages.splice(i, 1)">×</button>
              </div>
              <button class="modal-add-img" @click="triggerPostImage">+</button>
            </div>
            <div class="modal-actions">
              <button class="modal-btn modal-btn-cancel" @click="showPostModal = false">取消</button>
              <button class="modal-btn modal-btn-save" @click="submitPost">发布</button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- 图片预览 -->
    <Teleport to="body">
      <Transition name="modal-fade">
        <div v-if="previewImage" class="image-preview-backdrop" @click.self="previewImage = null">
          <img :src="previewImage" class="image-preview-img" />
        </div>
      </Transition>
    </Teleport>

    <input ref="commentImageInput" type="file" accept="image/*" class="sr-only" @change="onCommentImage" />
    <input ref="postImageInput" type="file" accept="image/*" class="sr-only" @change="onPostImage" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useUserStore } from '../stores/user'
import {
  getCommunityPosts, createCommunityPost, likePost, unlikePost,
  addComment, getComments, deleteComment, favoritePost, unfavoritePost,
  getFavorites, deleteCommunityPost
} from '../api/index'

const userStore = useUserStore()
const activeTab = ref('all')
const posts = ref([])
const favorites = ref([])
const comments = reactive({})
const activeCommentPostId = ref(null)
const commentText = ref('')
const commentImages = ref([])
const showPostModal = ref(false)
const postContent = ref('')
const postImages = ref([])
const previewImage = ref(null)
const commentImageInput = ref(null)
const postImageInput = ref(null)

const displayPosts = computed(() => {
  if (activeTab.value === 'fav') return favorites.value
  return posts.value
})

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return d.toLocaleDateString('zh-CN')
}

async function loadPosts() {
  try {
    const res = await getCommunityPosts({ page: 0, size: 50, userId: userStore.getUserId() })
    if (res.code === 200) {
      posts.value = res.data || []
    }
  } catch { /* ignore */ }
}

async function loadFavorites() {
  try {
    const res = await getFavorites(userStore.getUserId())
    if (res.code === 200) {
      favorites.value = res.data || []
    }
  } catch { /* ignore */ }
}

async function toggleLike(post) {
  const userId = userStore.getUserId()
  if (!userId) { alert('请先登录'); return }
  try {
    if (post.liked) {
      const res = await unlikePost(post.id, userId)
      post.likeCount = res.likeCount ?? (post.likeCount > 0 ? post.likeCount - 1 : 0)
      post.liked = false
    } else {
      const res = await likePost(post.id, userId)
      post.likeCount = res.likeCount ?? (post.likeCount + 1)
      post.liked = true
    }
  } catch (err) {
    console.error('Like/unlike failed:', err)
    alert(err.response?.data?.message || '操作失败，请重试')
  }
}

async function toggleFavorite(post) {
  const userId = userStore.getUserId()
  if (!userId) { alert('请先登录'); return }
  try {
    if (post.favorited) {
      await unfavoritePost(post.id, userId)
      post.favorited = false
    } else {
      await favoritePost(post.id, userId)
      post.favorited = true
    }
    loadFavorites()
  } catch (err) {
    console.error('Favorite/unfavorite failed:', err)
    alert(err.response?.data?.message || '操作失败，请重试')
  }
}

async function toggleComment(post) {
  if (activeCommentPostId.value === post.id) {
    activeCommentPostId.value = null
    return
  }
  activeCommentPostId.value = post.id
  commentText.value = ''
  commentImages.value = []
  try {
    const res = await getComments(post.id)
    if (res.code === 200) {
      comments[post.id] = res.data || []
    }
  } catch { /* ignore */ }
}

async function submitComment(postId) {
  const userId = userStore.getUserId()
  if (!userId) { alert('请先登录'); return }
  if (!commentText.value.trim() && !commentImages.value.length) return
  try {
    const res = await addComment({
      postId,
      userId,
      content: commentText.value,
      images: JSON.stringify(commentImages.value)
    })
    if (res.code === 200) {
      commentText.value = ''
      commentImages.value = []
      const listRes = await getComments(postId)
      comments[postId] = listRes.data || []
      const post = posts.value.find(p => p.id === postId)
      if (post) post.commentCount++
    }
  } catch (err) {
    alert(err.response?.data?.message || '评论失败')
  }
}

async function handleDeleteComment(commentId, postId) {
  if (!confirm('确定删除这条评论？')) return
  try {
    await deleteComment(commentId, userStore.getUserId())
    comments[postId] = (comments[postId] || []).filter(c => c.id !== commentId)
    const post = posts.value.find(p => p.id === postId)
    if (post) post.commentCount = Math.max(0, post.commentCount - 1)
  } catch (err) {
    alert(err.response?.data?.message || '删除失败')
  }
}

async function handleDeletePost(postId) {
  if (!confirm('确定删除这条帖子？')) return
  try {
    await deleteCommunityPost(postId, userStore.getUserId())
    posts.value = posts.value.filter(p => p.id !== postId)
    favorites.value = favorites.value.filter(p => p.id !== postId)
  } catch (err) {
    alert(err.response?.data?.message || '删除失败')
  }
}

async function submitPost() {
  const userId = userStore.getUserId()
  if (!userId) { alert('请先登录'); return }
  if (!postContent.value.trim() && !postImages.value.length) return
  try {
    await createCommunityPost({
      userId,
      content: postContent.value,
      images: JSON.stringify(postImages.value)
    })
    postContent.value = ''
    postImages.value = []
    showPostModal.value = false
    loadPosts()
  } catch (err) {
    alert(err.response?.data?.message || '发布失败')
  }
}

function triggerCommentImage() {
  commentImageInput.value?.click()
}

function onCommentImage(e) {
  const file = e.target.files?.[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = (ev) => {
    commentImages.value.push(ev.target.result)
  }
  reader.readAsDataURL(file)
  e.target.value = ''
}

function triggerPostImage() {
  postImageInput.value?.click()
}

function onPostImage(e) {
  const file = e.target.files?.[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = (ev) => {
    postImages.value.push(ev.target.result)
  }
  reader.readAsDataURL(file)
  e.target.value = ''
}

onMounted(() => {
  loadPosts()
  loadFavorites()
})
</script>

<style scoped>
.community-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.community-tab {
  padding: 6px 14px;
  border-radius: var(--radius-md);
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  background: var(--bg-secondary);
  transition: all 0.15s;
}

.community-tab.active {
  color: #fff;
  background: #0C84FF;
}

.post-card {
  padding: 16px;
  margin-bottom: 12px;
  border-radius: var(--radius-lg);
}

.post-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.post-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--bg-elevated);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
  flex-shrink: 0;
  background-size: cover;
  background-position: center;
}

.post-meta {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
}

.post-user {
  font-size: 14px;
  font-weight: 600;
}

.post-time {
  font-size: 11px;
  color: var(--text-muted);
}

.post-delete {
  width: 28px;
  height: 28px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  background: transparent;
  flex-shrink: 0;
}

.post-delete:active {
  background: #FFEBEE;
  color: #E53935;
}

.post-content {
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-primary);
  margin-bottom: 10px;
  word-break: break-word;
}

.post-images {
  display: flex;
  gap: 6px;
  margin-bottom: 10px;
  overflow-x: auto;
}

.post-img {
  width: 100px;
  height: 100px;
  border-radius: var(--radius-sm);
  object-fit: cover;
  flex-shrink: 0;
  cursor: pointer;
}

.post-actions {
  display: flex;
  gap: 20px;
  padding-top: 10px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  background: none;
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 600;
  transition: color 0.2s;
}

.action-btn:active {
  color: var(--yellow-primary);
}

.liked { filter: none; color: #E53935; }
.favorited { color: #FFC107; }

.comment-section {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 10px;
  max-height: 300px;
  overflow-y: auto;
}

.comment-item {
  display: flex;
  gap: 8px;
  align-items: flex-start;
}

.comment-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #0C84FF;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
}

.comment-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.comment-user {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
}

.comment-text {
  font-size: 13px;
  color: var(--text-primary);
  word-break: break-word;
}

.comment-images {
  display: flex;
  gap: 4px;
  margin-top: 4px;
}

.comment-img {
  width: 60px;
  height: 60px;
  border-radius: var(--radius-sm);
  object-fit: cover;
}

.comment-time {
  font-size: 10px;
  color: var(--text-muted);
}

.comment-delete {
  width: 22px;
  height: 22px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  background: transparent;
  flex-shrink: 0;
}

.comment-delete:active {
  background: #FFEBEE;
  color: #E53935;
}

.comment-empty {
  font-size: 12px;
  color: var(--text-muted);
  text-align: center;
  padding: 12px;
}

.comment-input-area {
  display: flex;
  gap: 6px;
  align-items: center;
}

.comment-input {
  flex: 1;
  padding: 8px 12px;
  border-radius: var(--radius-md);
  border: 1px solid var(--divider);
  background: var(--bg-secondary);
  font-size: 13px;
}

.comment-img-btn {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-full);
  background: var(--bg-secondary);
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.comment-send-btn {
  padding: 8px 14px;
  border-radius: var(--radius-md);
  background: #0C84FF;
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  flex-shrink: 0;
}

.comment-preview-images {
  display: flex;
  gap: 6px;
  margin-top: 6px;
}

.comment-preview-wrap {
  position: relative;
  width: 50px;
  height: 50px;
}

.comment-preview-img {
  width: 100%;
  height: 100%;
  border-radius: var(--radius-sm);
  object-fit: cover;
}

.comment-preview-remove {
  position: absolute;
  top: -4px;
  right: -4px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #E53935;
  color: #fff;
  font-size: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* ===== Glow 发布按钮 ===== */
.glow-btn {
  --glow-color: rgb(217, 176, 255);
  --glow-spread-color: rgba(191, 123, 255, 0.781);
  --enhanced-glow-color: rgb(231, 206, 255);
  --btn-color: rgb(100, 61, 136);
  border: .25em solid var(--glow-color);
  padding: 0.8em 1.8em;
  color: var(--glow-color);
  font-size: 15px;
  font-weight: bold;
  background-color: var(--btn-color);
  border-radius: 1em;
  outline: none;
  box-shadow: 0 0 1em .25em var(--glow-color),
              0 0 4em 1em var(--glow-spread-color),
              inset 0 0 .75em .25em var(--glow-color);
  text-shadow: 0 0 .5em var(--glow-color);
  position: fixed;
  right: 20px;
  bottom: calc(80px + var(--safe-bottom));
  z-index: 50;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 6px;
}

.glow-btn::after {
  pointer-events: none;
  content: "";
  position: absolute;
  top: 120%;
  left: 0;
  height: 100%;
  width: 100%;
  background-color: var(--glow-spread-color);
  filter: blur(2em);
  opacity: .7;
  transform: perspective(1.5em) rotateX(35deg) scale(1, .6);
}

.glow-btn:hover {
  color: var(--btn-color);
  background-color: var(--glow-color);
  box-shadow: 0 0 1em .25em var(--glow-color),
              0 0 4em 2em var(--glow-spread-color),
              inset 0 0 .75em .25em var(--glow-color);
}

.glow-btn:active {
  box-shadow: 0 0 0.6em .25em var(--glow-color),
              0 0 2.5em 2em var(--glow-spread-color),
              inset 0 0 .5em .25em var(--glow-color);
  transform: scale(0.95);
}

.glow-btn svg {
  stroke: currentColor;
}
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 40px;
  border-radius: var(--radius-lg);
  margin-top: 40px;
}

.empty-icon { font-size: 48px; }
.empty-text { font-size: 16px; font-weight: 600; }
.empty-hint { font-size: 12px; color: var(--text-muted); }

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
  max-width: 400px;
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

.modal-textarea {
  width: 100%;
  min-height: 100px;
  padding: 12px;
  border-radius: var(--radius-md);
  border: 1px solid var(--divider);
  background: var(--bg-secondary);
  font-size: 14px;
  resize: vertical;
}

.modal-image-area {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.modal-image-preview {
  position: relative;
  width: 60px;
  height: 60px;
}

.modal-image-preview img {
  width: 100%;
  height: 100%;
  border-radius: var(--radius-sm);
  object-fit: cover;
}

.modal-image-preview button {
  position: absolute;
  top: -4px;
  right: -4px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #E53935;
  color: #fff;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-add-img {
  width: 60px;
  height: 60px;
  border-radius: var(--radius-sm);
  border: 2px dashed var(--divider);
  color: var(--text-muted);
  font-size: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
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

/* ===== Skew 3D button ===== */
.skew-btn {
  transform: rotate(-25deg) skew(25deg);
  transform-style: preserve-3d;
  position: relative;
  list-style: none;
  width: 100px;
  height: 32px;
  border: none;
  background: transparent;
  font-family: inherit;
  cursor: pointer;
}

.skew-btn.fab {
  position: fixed;
  right: 60px;
  bottom: calc(150px + var(--safe-bottom));
  z-index: 50;
}

.skew-btn:before {
  content: '';
  position: absolute;
  bottom: -10px;
  left: -5px;
  width: 100%;
  height: 10px;
  background: #2a2a2a;
  transform: skewX(-41deg);
}

.skew-btn:after {
  content: '';
  position: absolute;
  top: 5px;
  left: -9px;
  width: 9px;
  height: 100%;
  background: #2a2a2a;
  transform: skewY(-49deg);
}

.skew-btn span {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: #2a2a2a;
  color: #fff;
  font-size: 25px;
  transition: 1.1s ease-out;
  display: flex;
  align-items: center;
  justify-content: center;
}

.skew-btn:hover span {
  z-index: 1000;
  transition: .3s;
  color: #fff;
}

.skew-btn:hover span:nth-child(5) {
  transform: translate(40px, -40px);
  opacity: 1;
}

.skew-btn:hover span:nth-child(4) {
  transform: translate(30px, -30px);
  opacity: .8;
}

.skew-btn:hover span:nth-child(3) {
  transform: translate(20px, -20px);
  opacity: .6;
}

.skew-btn:hover span:nth-child(2) {
  transform: translate(10px, -10px);
  opacity: .4;
}

.skew-btn:hover span:nth-child(1) {
  transform: translate(0px, 0px);
  opacity: .2;
}

.skew-btn:active span:nth-child(5) {
  transform: translate(20px, -20px);
  opacity: 1;
}

.skew-btn:active span:nth-child(4) {
  transform: translate(15px, -15px);
}

.skew-btn:active span:nth-child(3) {
  transform: translate(10px, -10px);
}

.skew-btn:active span:nth-child(2) {
  transform: translate(5px, -5px);
}

.skew-btn:active span:nth-child(1) {
  transform: translate(0px, 0px);
}

.skew-btn:hover span {
  background: #52E19F;
}

.modal-btn-cancel {
  padding: 20px;
  background: var(--bg-secondary);
  color: var(--text-secondary);
}

.modal-btn-save {
  background: #0C84FF;
  color: #fff;
}

.image-preview-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 300;
  padding: 20px;
}

.image-preview-img {
  max-width: 100%;
  max-height: 80vh;
  border-radius: var(--radius-md);
}

.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.2s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border-width: 0;
}
</style>

import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 120000,
  headers: { 'Content-Type': 'application/json' }
})

api.interceptors.response.use(
  res => res.data,
  err => {
    const msg = err.response?.data?.message || '网络请求失败'
    console.error('[API Error]', msg, err)
    return Promise.reject(err)
  }
)

export function login(data) {
  return api.post('/auth/login', data)
}

export function register(data) {
  return api.post('/auth/register', data)
}

export function getUserInfo(id) {
  return api.get(`/user/${id}`)
}

export function updateUser(id, data) {
  return api.put(`/user/${id}`, data)
}

export function analyzeFood(formData) {
  return api.post('/food/analyze', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function getFoodRecords(params) {
  return api.get('/food/records', { params })
}

export function getFoodRecord(id) {
  return api.get(`/food/record/${id}`)
}

export function deleteFoodRecord(id, userId) {
  return api.delete(`/food/record/${id}`, { params: { userId } })
}

export function batchDeleteFoodRecords(ids, userId) {
  return api.post('/food/records/batch-delete', ids, { params: { userId } })
}

export function getAiTips() {
  return api.get('/ai/tips')
}

export function listUsers() {
  return api.get('/user/list')
}

export function deleteUser(id, currentUserId) {
  return api.delete(`/user/${id}`, { params: { currentUserId } })
}

export function updateUserPassword(id, newPassword, currentUserId) {
  return api.put(`/user/${id}/password`, { newPassword }, { params: { currentUserId } })
}

export function updateUserRole(id, role, currentUserId) {
  return api.put(`/user/${id}/role`, { role }, { params: { currentUserId } })
}

export function updateUserAvatar(id, avatar, currentUserId) {
  return api.put(`/user/${id}/avatar`, { avatar }, { params: { currentUserId } })
}

export function getUserStatistics() {
  return api.get('/user/statistics')
}

export function getCommunityPosts(params) {
  return api.get('/community/posts', { params })
}

export function createCommunityPost(data) {
  return api.post('/community/post', data)
}

export function likePost(postId, userId) {
  return api.post(`/community/like/${postId}`, null, { params: { userId } })
}

export function unlikePost(postId, userId) {
  return api.delete(`/community/like/${postId}`, { params: { userId } })
}

export function addComment(data) {
  return api.post('/community/comment', data)
}

export function getComments(postId) {
  return api.get(`/community/comments/${postId}`)
}

export function deleteComment(commentId, userId) {
  return api.delete(`/community/comment/${commentId}`, { params: { userId } })
}

export function favoritePost(postId, userId) {
  return api.post(`/community/favorite/${postId}`, null, { params: { userId } })
}

export function unfavoritePost(postId, userId) {
  return api.delete(`/community/favorite/${postId}`, { params: { userId } })
}

export function getFavorites(userId) {
  return api.get('/community/favorites', { params: { userId } })
}

export function deleteCommunityPost(postId, userId) {
  return api.delete(`/community/post/${postId}`, { params: { userId } })
}

export function getAllCommunityPosts(userId) {
  return api.get('/community/admin/posts', { params: { userId } })
}

export default api

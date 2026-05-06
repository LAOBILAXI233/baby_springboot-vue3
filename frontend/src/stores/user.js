import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(null)
  const isLoggedIn = computed(() => userInfo.value !== null)

  function setUserInfo(info) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  function loadFromStorage() {
    const stored = localStorage.getItem('userInfo')
    if (stored) {
      try {
        userInfo.value = JSON.parse(stored)
      } catch {
        userInfo.value = null
      }
    }
  }

  function logout() {
    userInfo.value = null
    localStorage.removeItem('userInfo')
  }

  function getUserId() {
    return userInfo.value?.userId || null
  }

  function isAdmin() {
    return userInfo.value?.role === 2
  }

  loadFromStorage()

  return { userInfo, isLoggedIn, setUserInfo, loadFromStorage, logout, getUserId, isAdmin }
})

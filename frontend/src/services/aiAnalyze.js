import api from '../api/index'

export async function analyzeFoodImage(imageFile, userId) {
  const formData = new FormData()
  formData.append('userId', userId)
  formData.append('file', imageFile)

  const res = await api.post('/food/analyze', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })

  const data = res.data || res

  let nutrients = []
  if (data.nutrients) {
    try {
      nutrients = typeof data.nutrients === 'string' ? JSON.parse(data.nutrients) : data.nutrients
    } catch (e) {
      nutrients = []
    }
  }

  return {
    id: data.id,
    foodName: data.foodName || '未知食物',
    mealType: data.mealType || '加餐',
    healthScore: typeof data.healthScore === 'number' ? data.healthScore : 60,
    aiResult: data.aiResult || '',
    suggestion: data.suggestion || '',
    imageUrl: data.imageUrl || '',
    nutrients,
    confidence: typeof data.confidence === 'number' ? data.confidence : 75,
    feedback: data.feedback || ''
  }
}

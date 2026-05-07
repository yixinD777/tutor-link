import { ref, computed } from 'vue'

/**
 * 通用分页列表 composable
 * @param {Function} fetchFn - 异步获取函数 (page, pageSize) => Promise<{ list, total }>
 * @param {Object} options - 配置项
 * @param {number} options.pageSize - 每页数量，默认 10
 * @param {boolean} options.immediate - 是否立即加载，默认 true
 */
export function usePageList(fetchFn, options = {}) {
  const { pageSize = 10, immediate = true } = options

  const list = ref([])
  const loading = ref(false)
  const page = ref(1)
  const hasMore = ref(true)
  const total = ref(0)

  async function loadData(reset = false) {
    if (reset) {
      page.value = 1
      list.value = []
      hasMore.value = true
    }

    if (!hasMore.value || loading.value) return

    loading.value = true
    try {
      const res = await fetchFn(page.value, pageSize)
      const items = res.list || res.records || res || []
      total.value = res.total ?? items.length

      if (reset) {
        list.value = items
      } else {
        list.value = [...list.value, ...items]
      }

      hasMore.value = list.value.length < total.value
      page.value++
    } catch (e) {
      console.error('usePageList load error:', e)
    } finally {
      loading.value = false
    }
  }

  function reset() {
    loadData(true)
  }

  function loadMore() {
    loadData(false)
  }

  const isEmpty = computed(() => !loading.value && list.value.length === 0)

  if (immediate) {
    loadData(true)
  }

  return {
    list,
    loading,
    page,
    hasMore,
    total,
    isEmpty,
    loadData,
    reset,
    loadMore,
  }
}
